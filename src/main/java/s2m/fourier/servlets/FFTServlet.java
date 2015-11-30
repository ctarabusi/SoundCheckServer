package s2m.fourier.servlets;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;
import s2m.fourier.utils.ServletUtils;

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

        List<Double> inputFFTList = new ArrayList<>();
        try
        {
            in = new BufferedInputStream(req.getInputStream());

            for (int length = 0; (length = in.read(buffer, 0, buffer.length)) > 0; )
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


        double[] inputArray = ServletUtils.doubleArrayToPrimitve(inputFFTList);
        double[] inputWithoutMeanFFT = ServletUtils.removeAverage(inputArray);
        double[] inputFFT = ServletUtils.addZeroPaddingToPowerTwo(inputWithoutMeanFFT);
        Complex[] outputFFT = new FastFourierTransformer().transform(inputFFT);

        ServletOutputStream outputStream = resp.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        Complex[] complexArray = Arrays.copyOfRange(outputFFT, 0, outputFFT.length / 2);
        double[] magnitudeArray = ServletUtils.getMagnitudeComponents(complexArray);
        objectOutputStream.writeObject(magnitudeArray);

        outputStream.flush();
        outputStream.close();
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
