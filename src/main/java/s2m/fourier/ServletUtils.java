package s2m.fourier;

import java.util.Arrays;

public class ServletUtils
{
    public static double[] addZeroPaddingToPowerTwo(double[] inputFFTList)
    {
        int currentSize = inputFFTList.length;
        int totalSize = 2;
        while (currentSize > totalSize)
        {
            totalSize = totalSize * 2;
        }

        double[] sampleArrayWithZero = new double[totalSize];
        Arrays.fill(sampleArrayWithZero, 0);
        int i = 0;
        for (Double sample : inputFFTList)
        {
            sampleArrayWithZero[i] = sample;
            i++;
        }

        return sampleArrayWithZero;
    }

    public static double[] removeAverage(double[] array)
    {
        int sum = 0;
        for (double d : array)
        {
            sum += d;
        }
        double average = sum / array.length;

        double[] sampleArrayWithoutAverage = new double[array.length];
        int i = 0;
        for (double sample : array)
        {
            sampleArrayWithoutAverage[i] = sample - average;
            i++;
        }
        return sampleArrayWithoutAverage;
    }
}
