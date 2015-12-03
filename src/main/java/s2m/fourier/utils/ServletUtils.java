package s2m.fourier.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ServletUtils
{
    public static List<Double> extractSamples(InputStream inputStream)
    {
        List<Double> inputList = new ArrayList<>();

        byte[] buffer = new byte[2048];

        try (BufferedInputStream is = new BufferedInputStream(inputStream))
        {
            for (int length = is.read(buffer); length != -1; length = is.read(buffer))
            {
                ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                addShortBufferToListWithHamming(shortBuffer, inputList);
            }
        }
        catch (IOException exception)
        {
            Logger.getAnonymousLogger().severe(exception.getMessage());
        }

        return inputList;
    }

    public static double[][] convertListToMatrix(List<double[]> outputMatrixList)
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
