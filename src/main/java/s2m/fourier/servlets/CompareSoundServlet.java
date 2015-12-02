package s2m.fourier.servlets;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

        int foundMatching = 0;
        try (InputStream is = req.getInputStream())
        {
            foundMatching = getFrequencyPeaksFromInput(is, recordingFrequencyPeakHashes);
        }

        Logger.getAnonymousLogger().severe("foundMatching " + foundMatching);

        buildResponse(resp, String.valueOf(foundMatching));
    }

    public static int getFrequencyPeaksFromInput(InputStream is, Map<Integer, List<Integer>> recordingMap) throws IOException
    {
        byte[] buffer = new byte[2048];

        List<double[]> outputMatrixList = new ArrayList<>();

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

        Logger.getAnonymousLogger().severe("input size: " + inputFFTList.size());

        int rowsAvailable = inputFFTList.size() / FREQUENCY_CHUNK_SIZE;

        for (int i = 0; i < rowsAvailable; i++)
        {
            List<Double> chunkList = inputFFTList.subList(i * FREQUENCY_CHUNK_SIZE, i * FREQUENCY_CHUNK_SIZE + FREQUENCY_CHUNK_SIZE);
            outputMatrixList.add(ServletUtils.calculateFFT(chunkList));
        }

        double[][] outputMatrix = new double[outputMatrixList.size()][outputMatrixList.get(0).length];
        int i = 0;
        for (double[] row : outputMatrixList)
        {
            outputMatrix[i] = row;
            i++;
        }

        return compareFrequencyPeaks(outputMatrix, recordingMap);
    }

    public static Map<Integer, List<Integer>> readFrequencyPeakFromRecording(InputStream recordingInputStream) throws IOException
    {
        byte[] buffer = new byte[2048];

        List<Double> recordingAudioFFTList = new ArrayList<>();
        List<double[]> inputMatrixList = new ArrayList<>();

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

        int rowsAvailable = recordingAudioFFTList.size() / FREQUENCY_CHUNK_SIZE;

        for (int i = 0; i < rowsAvailable; i++)
        {
            List<Double> chunkList = recordingAudioFFTList.subList(i * FREQUENCY_CHUNK_SIZE, i * FREQUENCY_CHUNK_SIZE + FREQUENCY_CHUNK_SIZE);
            inputMatrixList.add(ServletUtils.calculateFFT(chunkList));
        }

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
        Logger.getAnonymousLogger().severe("outputMatrix: " + outputMatrix.length);

        Map<Integer, List<Integer>> map = new HashMap<>();

        int currentIndex = 0;
        for (double[] instantFrequencies : outputMatrix)
        {
            int firstMax = findFrequencyPositionWithMaxAmplitude(instantFrequencies, 0);

            addFrequency(map, firstMax, currentIndex);

            currentIndex++;
        }
        return map;
    }


    private static int compareFrequencyPeaks(double[][] outputMatrix, Map<Integer, List<Integer>> recordingMap)
    {
        int consecutivesFound = 0;

        List<Integer> previousFreqBin = new ArrayList<>();
        for (double[] instantFrequencies : outputMatrix)
        {
            int firstMax = findFrequencyPositionWithMaxAmplitude(instantFrequencies, 0);

            List<Integer> freqBin = recordingMap.get(firstMax);
            if (freqBin != null && !previousFreqBin.isEmpty())
            {
                for (Integer freqElement : freqBin)
                {
                    for (Integer previousFreqElement : previousFreqBin)
                    {
                        if (freqElement == previousFreqElement + 1)
                        {
                            consecutivesFound++;
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
        return consecutivesFound;
    }

    private static void addFrequency(Map<Integer, List<Integer>> map, int frequency, int position)
    {
        List<Integer> listPositionForFrequency = map.get(frequency);
        if (listPositionForFrequency == null)
        {
            listPositionForFrequency = new ArrayList<>();
        }
        listPositionForFrequency.add(position);
        map.put(frequency, listPositionForFrequency);
    }

    public static int findFrequencyPositionWithMaxAmplitude(double[] array, int baseIndex)
    {
        double max = 0;
        int maxFreq = 0;
        for (int counter = 1; counter < array.length; counter++)
        {
            if (array[counter] > max)
            {
                max = array[counter];
                maxFreq = baseIndex + counter;
            }
        }
        return maxFreq;
    }
}
