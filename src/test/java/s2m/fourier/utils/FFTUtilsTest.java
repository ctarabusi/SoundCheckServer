package s2m.fourier.utils;

import org.junit.Test;
import s2m.fourier.utils.FFTUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FFTUtilsTest
{
    @Test
    public void fft()
    {

        List<Double> list = new ArrayList<>();
        list.add(11.0);
        list.add(-20.0);
        list.add(-15.0);
        list.add(17.0);
        list.add(11.0);
        list.add(-20.0);
        list.add(-15.0);
        list.add(17.0);
        list.add(0.0);
        list.add(0.0);
        list.add(0.0);
        list.add(0.0);
        list.add(0.0);

        double[] firstIteration = FFTUtils.calculateFFT(list);
        double[] secondIteration = FFTUtils.calculateFFT(list);

        assertThat(firstIteration.length, is(secondIteration.length));

        int i = 0;
        for (double abs : firstIteration)
        {
            assertThat(abs, is(secondIteration[i]));
            i++;
        }
    }

    @Test
    public void testRemoveAverage()
    {
        double[] inputArray = new double[]{2.0, 5.0, 8.0};
        double[] outputArray = FFTUtils.removeAverage(inputArray);

        assertThat(outputArray[0], is(-3.0));
        assertThat(outputArray[1], is(0.0));
        assertThat(outputArray[2], is(3.0));
    }

    @Test
    public void testAddZeroPaddingToPowerTwo()
    {
        double[] inputArray = new double[]{2.0, 5.0, 8.0};
        double[] outputArray = FFTUtils.addZeroPaddingToPowerTwo(inputArray);

        assertThat(outputArray.length, is(4));
        assertThat(outputArray[0], is(2.0));
        assertThat(outputArray[1], is(5.0));
        assertThat(outputArray[2], is(8.0));
        assertThat(outputArray[3], is(0.0));
    }
}
