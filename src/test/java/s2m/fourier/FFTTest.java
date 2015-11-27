package s2m.fourier;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FFTTest
{
    @Test
    public void fft()
    {

        List<Double> list = new ArrayList<Double>();
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

        double[] firstIteration = ServletUtils.calculateFFT(list);
        double[] secondIteration = ServletUtils.calculateFFT(list);

        assertThat(firstIteration.length, is(secondIteration.length));

        int i = 0;
        for (double abs : firstIteration)
        {
            assertThat(abs, is(secondIteration[i]));
            i++;
        }
    }
}
