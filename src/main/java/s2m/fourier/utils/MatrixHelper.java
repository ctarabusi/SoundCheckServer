package s2m.fourier.utils;

import java.nio.ShortBuffer;
import java.util.List;

public class MatrixHelper
{
    public static double[][] getMatrixFromList(List<double[]> outputMatrixList)
    {
        double[][] outputMatrix = new double[outputMatrixList.size()][outputMatrixList.get(0).length];
        int i = 0;
        for (double[] row : outputMatrixList)
        {
            outputMatrix[i] = row;
            i++;
        }
        return outputMatrix;
    }

    public static void addShortBufferToList(ShortBuffer shortBuffer, List<Double> list)
    {
        short[] samplesArray = new short[shortBuffer.limit()];
        shortBuffer.get(samplesArray);

        for (short sample : samplesArray)
        {
            list.add((double) sample);
        }
    }

    public static void addShortBufferToListWithHamming(ShortBuffer shortBuffer, List<Double> list)
    {
        short[] samplesArray = new short[shortBuffer.limit()];
        shortBuffer.get(samplesArray);

        int index = 0;
        for (short sample : samplesArray)
        {
            list.add((double) (hammingWindow(samplesArray.length, index) * sample));
            index++;
        }
    }

    private static float hammingWindow(int length, int index)
    {
        if (index > length)
        {
            return 0;
        }
        return 0.54f - 0.46f * (float) Math.cos(Math.PI * 2 * index / (length - 1));
    }
}
