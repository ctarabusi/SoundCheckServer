package s2m.fourier.utils;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import java.util.Arrays;
import java.util.List;

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

    public static double[] doubleArrayToPrimitve(List<Double> inputFFTList)
    {
        double[] primitiveArray = new double[inputFFTList.size()];
        int i = 0;

        for (double d : inputFFTList)
        {
            primitiveArray[i++] = d;
        }

        return primitiveArray;
    }

    public static double[] getMagnitudeComponents(Complex[] complexArray)
    {
        double[] realComponents = new double[complexArray.length];
        int i = 0;
        for (Complex c : complexArray)
        {
            realComponents[i++] = c.abs();
        }
        return realComponents;
    }

    public static double[] calculateFFT(List<Double> inputFFTList)
    {
        double[] inputArray = doubleArrayToPrimitve(inputFFTList);
        double[] inputWithoutMeanFFT = removeAverage(inputArray);
        double[] inputFFT = addZeroPaddingToPowerTwo(inputWithoutMeanFFT);
        Complex[] outputFFT = new FastFourierTransformer().transform(inputFFT);

        Complex[] complexArray = Arrays.copyOfRange(outputFFT, 0, outputFFT.length / 2);
        return ServletUtils.getMagnitudeComponents(complexArray);
    }
}
