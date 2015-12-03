package s2m.fourier.servlets;

import org.junit.Test;
import s2m.fourier.utils.FFTUtils;
import s2m.fourier.utils.ServletUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CompareSoundTest
{
    @Test
    public void testFindMax() throws IOException
    {
        int maximumPosition = FFTUtils.findFrequencyPositionWithMaxAmplitude(new double[]{7.5, -4.0, 33.0});
        assertThat(maximumPosition, is(2));

        maximumPosition = FFTUtils.findFrequencyPositionWithMaxAmplitude(new double[]{-120.0, 45.0, 7.5, -4.0, 33.0});
        assertThat(maximumPosition, is(1));
    }

    @Test
    public void compareSound() throws IOException
    {
        FileInputStream recordingInputStream = new FileInputStream(new File("src/test/java/s2m/fourier/piano.wav"));
        final List<double[]> recordingSpectrogram = CompareSoundServlet.calculateFFTFromStream(recordingInputStream);
        Map<Integer, List<Integer>> recordingFrequencyPeakHashes = CompareSoundServlet.findFrequencyPeaks(ServletUtils.convertListToMatrix(recordingSpectrogram));
        recordingInputStream.close();

        FileInputStream fileInputStream = new FileInputStream(new File("src/test/java/s2m/fourier/piano.wav"));
        final List<double[]> inputSpectrogram = CompareSoundServlet.calculateFFTFromStream(fileInputStream);
        Map<Integer, List<Integer>> inputFrequencyPeakHashes = CompareSoundServlet.findFrequencyPeaks(ServletUtils.convertListToMatrix(inputSpectrogram));

        assertThat(recordingSpectrogram.size(), is(inputSpectrogram.size()));
        assertThat(recordingSpectrogram.get(100)[33], is(inputSpectrogram.get(100)[33]));

        Integer next = recordingFrequencyPeakHashes.keySet().iterator().next();
        assertThat(recordingFrequencyPeakHashes.get(next).size(), is(inputFrequencyPeakHashes.get(next).size()));

        double[][] inputSpectrogramMatrix = ServletUtils.convertListToMatrix(inputSpectrogram);
        String inputFrequencyPeaksHashes = CompareSoundServlet.compareFrequencyPeaks(inputSpectrogramMatrix, recordingFrequencyPeakHashes);
        fileInputStream.close();

        System.out.println(inputFrequencyPeaksHashes);
    }
}
