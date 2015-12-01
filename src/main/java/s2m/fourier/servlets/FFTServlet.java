package s2m.fourier.servlets;

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
import java.util.List;

public class FFTServlet extends HttpServlet
{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        BufferedInputStream is = null;

        byte[] buffer = new byte[2048];

        List<Double> inputFFTList = new ArrayList<>();
        try
        {
            is = new BufferedInputStream(req.getInputStream());

            for (int length = is.read(buffer); length != -1; length = is.read(buffer))
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
            if (is != null)
            {
                is.close();
            }
        }

        ServletOutputStream outputStream = resp.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        double[] magnitudeArray = ServletUtils.calculateFFT(inputFFTList);
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
