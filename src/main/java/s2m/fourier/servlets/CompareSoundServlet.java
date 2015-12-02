package s2m.fourier.servlets;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import s2m.fourier.utils.ServletUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CompareSoundServlet extends HttpServlet
{
    private static int FREQUENCY_CHUNK_SIZE = 2048;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ServletContext context = getServletContext();
        String fullPath = context.getRealPath(RecordingServlet.RECORDING_AUDIO_PATH);
        File recordingFile = new File(fullPath);

        Map<Integer, List<Integer>> recordingFrequencyPeakHashes;

        try (FileInputStream fileInputStream = new FileInputStream(recordingFile))
        {
            recordingFrequencyPeakHashes = readFrequencyPeakFromRecording(fileInputStream);
        }
        catch (IOException e)
        {
            buildResponse(resp, "File not found, please record a pattern");
            return;
        }

        String foundMatching;
        try (InputStream is = req.getInputStream())
        {
            foundMatching = getFrequencyPeaksFromInput(is, recordingFrequencyPeakHashes);
        }

        Logger.getAnonymousLogger().severe("foundMatching " + foundMatching);

        buildResponse(resp, foundMatching);
    }

    public static String getFrequencyPeaksFromInput(InputStream is, Map<Integer, List<Integer>> recordingMap) throws IOException
    {
        byte[] buffer = new byte[2048];

        List<Double> inputFFTList = new ArrayList<>();

        // ignoring header
        is.read(new byte[44], 0, 44);
        int length;
        while ((length = is.read(buffer, 0, buffer.length)) != -1)
        {
            ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

            short[] samplesArray = new short[shortBuffer.limit()];
            shortBuffer.get(samplesArray);

            for (short sample : samplesArray)
            {
                inputFFTList.add((double) sample);
            }
        }

        // Calculating the FFT for every sample
        List<double[]> inputSpectrogramList = Lists.partition(inputFFTList, FREQUENCY_CHUNK_SIZE).stream().map(ServletUtils::calculateFFT).collect(Collectors.toList());

        double[][] inputSpectrogramMatrix = new double[inputSpectrogramList.size()][inputSpectrogramList.get(0).length];
        int i = 0;
        for (double[] row : inputSpectrogramList)
        {
            inputSpectrogramMatrix[i] = row;
            i++;
        }

        return compareFrequencyPeaks(inputSpectrogramMatrix, recordingMap);
    }

    public static Map<Integer, List<Integer>> readFrequencyPeakFromRecording(InputStream recordingInputStream) throws IOException
    {
        byte[] buffer = new byte[2048];

        List<Double> recordingAudioFFTList = new ArrayList<>();

        // ignoring header
        recordingInputStream.read(new byte[44], 0, 44);
        int length;
        while ((length = recordingInputStream.read(buffer, 0, buffer.length)) != -1)
        {
            ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

            short[] samplesArray = new short[shortBuffer.limit()];
            shortBuffer.get(samplesArray);

            for (short sample : samplesArray)
            {
                recordingAudioFFTList.add((double) sample);
            }
        }

        // Calculating the FFT for every sample
        List<double[]> inputMatrixList = Lists.partition(recordingAudioFFTList, FREQUENCY_CHUNK_SIZE).stream().map(ServletUtils::calculateFFT).collect(Collectors.toList());

        double[][] outputMatrix = new double[inputMatrixList.size()][inputMatrixList.get(0).length];
        int i = 0;
        for (double[] row : inputMatrixList)
        {
            outputMatrix[i] = row;
            i++;
        }

        return findFrequencyPeaks(outputMatrix);
    }

    private void buildResponse(HttpServletResponse resp, String output) throws IOException
    {
        try (ServletOutputStream outputStream = resp.getOutputStream())
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(output);
            outputStream.flush();
        }
    }

    private static Map<Integer, List<Integer>> findFrequencyPeaks(double[][] outputMatrix)
    {
        Map<Integer, List<Integer>> map = new HashMap<>();

        int currentIndex = 0;
        for (double[] instantFrequencies : outputMatrix)
        {
            int firstMax = findFrequencyPositionWithMaxAmplitude(instantFrequencies);
            addFrequency(map, firstMax, currentIndex);

            currentIndex++;
        }
        return map;
    }


    private static String compareFrequencyPeaks(double[][] inputSpectrogramMatrix, Map<Integer, List<Integer>> recordingMap)
    {
        int matchesFound = 0;

        List<Integer> previousFreqBin = new ArrayList<>();
        for (double[] instantFrequencies : inputSpectrogramMatrix)
        {
            int firstMax = findFrequencyPositionWithMaxAmplitude(instantFrequencies);

            List<Integer> freqBin = recordingMap.getOrDefault(firstMax, new ArrayList<>());
            if (!previousFreqBin.isEmpty())
            {
                for (Integer freqElement : freqBin)
                {
                    for (Integer previousFreqElement : previousFreqBin)
                    {
                        if (freqElement == previousFreqElement + 1)
                        {
                            matchesFound++;
                            break;
                        }
                    }
                }
            }
            if (freqBin != null)
            {
                previousFreqBin = freqBin;
            }
        }
        double percentMatch = Math.ceil(((double) matchesFound / inputSpectrogramMatrix.length) * 100);
        return matchesFound + " of " + inputSpectrogramMatrix.length + " (" + percentMatch + "%)";
    }

    private static void addFrequency(Map<Integer, List<Integer>> map, int frequency, int position)
    {
        List<Integer> listPositionForFrequency = map.getOrDefault(frequency, new ArrayList<>());
        listPositionForFrequency.add(position);
        map.put(frequency, listPositionForFrequency);
    }

    public static int findFrequencyPositionWithMaxAmplitude(double[] array)
    {
        double max = Doubles.max(array);
        return Doubles.indexOf(array, max);
    }
}
