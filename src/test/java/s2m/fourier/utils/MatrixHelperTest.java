package s2m.fourier.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MatrixHelperTest
{
    @Test
    public void testGetMatrixFromList()
    {
        List<double[]> inputList = new ArrayList<>();
        inputList.add(new double[]{1.0, 2.0, 3.0});
        inputList.add(new double[]{4.0, 5.0, 6.0});

        double[][] outputArray = MatrixHelper.getMatrixFromList(inputList);

        assertThat(outputArray[0][0], is(1.0));
        assertThat(outputArray[0][1], is(2.0));
        assertThat(outputArray[0][2], is(3.0));
        assertThat(outputArray[1][0], is(4.0));
        assertThat(outputArray[1][1], is(5.0));
        assertThat(outputArray[1][2], is(6.0));
    }
}
