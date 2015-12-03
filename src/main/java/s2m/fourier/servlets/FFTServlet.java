package s2m.fourier.servlets;

import s2m.fourier.utils.FFTUtils;
import s2m.fourier.utils.MatrixHelper;

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
import java.util.logging.Logger;

public class FFTServlet extends HttpServlet
{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        byte[] buffer = new byte[2048];

        // Read Input Sample Stream
        List<Double> inputFFTList = new ArrayList<>();
        try (BufferedInputStream is = new BufferedInputStream(req.getInputStream()))
        {
            for (int length = is.read(buffer); length != -1; length = is.read(buffer))
            {
                ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                MatrixHelper.addShortBufferToListWithHamming(shortBuffer, inputFFTList);
            }
        }

        // Calculate FFT
        double[] magnitudeArray = FFTUtils.calculateFFT(inputFFTList);

        buildResponse(resp, magnitudeArray);
    }

    private void buildResponse(HttpServletResponse resp, double[] magnitudeArray)
    {
        try (ServletOutputStream outputStream = resp.getOutputStream())
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(magnitudeArray);
            outputStream.flush();
        }
        catch (IOException e)
        {
            Logger.getAnonymousLogger().severe(e.getMessage());
        }
    }
}
