package s2m.fourier.utils;

import com.google.common.primitives.Doubles;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import java.util.Arrays;
import java.util.List;

public class FFTUtils
{

    static double[] addZeroPaddingToPowerTwo(double[] inputArray)
    {
        int currentSize = inputArray.length;
        int totalSize = 2;
        while (currentSize > totalSize)
        {
            totalSize = totalSize * 2;
        }

        double[] sampleArrayWithZero = new double[totalSize];

        Arrays.fill(sampleArrayWithZero, 0);
        int i = 0;
        for (Double sample : inputArray)
        {
            sampleArrayWithZero[i] = sample;
            i++;
        }

        return sampleArrayWithZero;
    }

    static double[] removeAverage(double[] array)
    {
        // Calculate average
        double average = Arrays.stream(array).average().getAsDouble();

        // Substract average to samples
        return Arrays.stream(array).map(sample -> sample - average).toArray();
    }

    public static double[] calculateFFT(List<Double> inputFFTList)
    {
        double[] inputArray = Doubles.toArray(inputFFTList);

        // Remove Average
        double[] inputWithoutMeanFFT = removeAverage(inputArray);

        // Adding tailing zero until power of 2 length
        double[] inputFFT = addZeroPaddingToPowerTwo(inputWithoutMeanFFT);

        // Calculate FFT
        Complex[] outputFFT = new FastFourierTransformer().transform(inputFFT);

        // Retrieve just half of elements being simmetrical & Getting Magnitude of elements
        return Arrays.stream(outputFFT).limit(outputFFT.length / 2).mapToDouble(Complex::abs).toArray();
    }

    public static int findFrequencyPositionWithMaxAmplitude(double[] array)
    {
        double max = Doubles.max(array);
        return Doubles.indexOf(array, max);
    }
}
