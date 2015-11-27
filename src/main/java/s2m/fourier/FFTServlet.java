package s2m.fourier;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastCosineTransformer;
import org.apache.commons.math.transform.FastFourierTransformer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FFTServlet extends HttpServlet
{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        BufferedInputStream in = null;

        byte[] buffer = new byte[10240];

        List<Double> inputFFTList = new ArrayList<Double>();
        try
        {
            in = new BufferedInputStream(req.getInputStream());

            for (int length = 0; (length = in.read(buffer)) > 0; )
            {
                ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

                short[] samplesArray = new short[shortBuffer.limit()];
                shortBuffer.get(samplesArray);

                int index = 0;
                for (short sample : samplesArray)
                {
                    inputFFTList.add((double) (hammingWindow(samplesArray.length, index) * sample));
                    index++;
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }


        double[] inputArray = doubleArrayToPrimitve(inputFFTList);
        double[] inputWithoutMeanFFT = ServletUtils.removeAverage(inputArray);
        double[] inputFFT = ServletUtils.addZeroPaddingToPowerTwo(inputWithoutMeanFFT);
        double[] outputFFT = new FastCosineTransformer().transform(inputFFT);

        ServletOutputStream outputStream = resp.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        double[] magnitudeArray = Arrays.copyOfRange(outputFFT, 0, outputFFT.length / 2);
        objectOutputStream.writeObject(magnitudeArray);

        outputStream.flush();
        outputStream.close();
    }

    private double[] getMagnitudeComponents(Complex[] complexArray)
    {
        double[] realComponents = new double[complexArray.length];
        int i = 0;
        for (Complex c : complexArray)
        {
            realComponents[i++] = c.abs();
        }
        return realComponents;
    }

    private double[] doubleArrayToPrimitve(List<Double> inputFFTList)
    {
        double[] primitiveArray = new double[inputFFTList.size()];
        int i = 0;

        for (double d : inputFFTList)
        {
            primitiveArray[i++] = d;
        }

        return primitiveArray;
    }


    public static float hammingWindow(int length, int index)
    {
        if (index > length)
        {
            return 0;
        }
        return 0.54f - 0.46f * (float) Math.cos(Math.PI * 2 * index / (length - 1));
    }

}
