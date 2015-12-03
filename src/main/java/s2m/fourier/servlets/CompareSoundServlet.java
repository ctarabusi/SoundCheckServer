package s2m.fourier.servlets;

import com.google.common.collect.Lists;
import s2m.fourier.utils.FFTUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CompareSoundServlet extends HttpServlet
{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ServletContext context = getServletContext();
        String fullPath = context.getRealPath(RecordingServlet.RECORDING_AUDIO_PATH);
        File recordingFile = new File(fullPath);

        Map<Integer, List<Integer>> recordingFrequencyPeaks;

        try (FileInputStream fileInputStream = new FileInputStream(recordingFile))
        {
            final List<double[]> recordingSpectrogram = calculateFFTFromStream(fileInputStream);
            recordingFrequencyPeaks = findFrequencyPeaks(ServletUtils.convertListToMatrix(recordingSpectrogram));
        }
        catch (IOException e)
        {
            buildResponse(resp, "File not found, please record a pattern");
            return;
        }

        try (InputStream inputStream = req.getInputStream())
        {
            // ignoring header
            final List<double[]> inputSpectrogram = calculateFFTFromStream(inputStream);

            double[][] inputSpectrogramMatrix = ServletUtils.convertListToMatrix(inputSpectrogram);

            String foundMatching = compareFrequencyPeaks(inputSpectrogramMatrix, recordingFrequencyPeaks);
            Logger.getAnonymousLogger().severe("foundMatching " + foundMatching);

            buildResponse(resp, foundMatching);
        }
    }

    static List<double[]> calculateFFTFromStream(InputStream inputStream) throws IOException
    {
        // ignoring header
        inputStream.read(new byte[44], 0, 44);

        List<Double> recordingAudioFFTList = ServletUtils.extractSamples(inputStream);

        // Calculating the FFT for every sample
        return Lists.partition(recordingAudioFFTList, 2048).stream().map(FFTUtils::calculateFFT).collect(Collectors.toList());
    }

    static Map<Integer, List<Integer>> findFrequencyPeaks(double[][] outputMatrix)
    {
        Map<Integer, List<Integer>> mapFrequencyToPositions = new HashMap<>();

        int currentIndex = 0;
        for (double[] instantFrequencies : outputMatrix)
        {
            int frequencyMaxAmplitude = FFTUtils.findFrequencyPositionWithMaxAmplitude(instantFrequencies);

            List<Integer> listPositionForFrequency = mapFrequencyToPositions.getOrDefault(frequencyMaxAmplitude, new ArrayList<>());
            listPositionForFrequency.add(currentIndex);
            mapFrequencyToPositions.put(frequencyMaxAmplitude, listPositionForFrequency);

            currentIndex++;
        }
        return mapFrequencyToPositions;
    }

    static String compareFrequencyPeaks(double[][] inputSpectrogramMatrix, Map<Integer, List<Integer>> recordingMap)
    {
        int matchesFound = 0;

        List<Integer> previousFrequencyPositions = new ArrayList<>();
        for (double[] instantFrequencies : inputSpectrogramMatrix)
        {
            // Find frequency with max amplitude for this time-sample
            int maxAmplitudeFrequency = FFTUtils.findFrequencyPositionWithMaxAmplitude(instantFrequencies);

            // Search the frequency in the spectrogram of the recorded pattern
            List<Integer> frequencyPositions = recordingMap.getOrDefault(maxAmplitudeFrequency, new ArrayList<>());
            if (!previousFrequencyPositions.isEmpty())
            {
                // It could happen that a frequency was the maximum one in several time slices.
                // A match is when two consecutives frequencies are found.
                for (Integer frequencyPosition : frequencyPositions)
                {
                    for (Integer previousFrequencyPosition : previousFrequencyPositions)
                    {
                        if (frequencyPosition == previousFrequencyPosition + 1)
                        {
                            matchesFound++;
                            break;
                        }
                    }
                }
            }
            previousFrequencyPositions = frequencyPositions;
        }
        return "Number of matches found: " + matchesFound;
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
}
