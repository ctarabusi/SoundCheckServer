package s2m.fourier;

import org.junit.Test;
import s2m.fourier.servlets.CompareSoundServlet;

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
        int maximumPosition = CompareSoundServlet.findFrequencyPositionWithMaxAmplitude(new double[]{7.5, -4.0, 33.0}, 0);
        assertThat(maximumPosition, is(2));

        maximumPosition = CompareSoundServlet.findFrequencyPositionWithMaxAmplitude(new double[]{-120.0, 45.0, 7.5, -4.0, 33.0}, 1000);
        assertThat(maximumPosition, is(1001));
    }
    @Test
    public void compareSound() throws IOException
    {

        FileInputStream recordingInputStream = new FileInputStream(new File("src/test/java/s2m/fourier/piano.wav"));
        Map<Integer, List<Integer>> recordingFrequencyPeakHashes = CompareSoundServlet.readFrequencyPeakFromRecording(recordingInputStream);
        recordingInputStream.close();

        FileInputStream fileInputStream = new FileInputStream(new File("src/test/java/s2m/fourier/piano.wav"));
        int inputFrequencyPeaksHashes = CompareSoundServlet.getFrequencyPeaksFromInput(fileInputStream, recordingFrequencyPeakHashes);
        fileInputStream.close();

        System.out.println(inputFrequencyPeaksHashes);
    }
}
