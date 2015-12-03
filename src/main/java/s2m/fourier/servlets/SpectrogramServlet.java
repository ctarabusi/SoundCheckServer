package s2m.fourier.servlets;

import com.google.common.collect.Lists;
import s2m.fourier.utils.FFTUtils;
import s2m.fourier.utils.MatrixHelper;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpectrogramServlet extends HttpServlet
{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        int CHUNK_SIZE = 2048;

        List<Double> inputFFTList = new ArrayList<Double>();

        try (InputStream is = req.getInputStream())
        {
            byte[] buffer = new byte[CHUNK_SIZE];
            for (int length = is.read(buffer); length != -1; length = is.read(buffer))
            {
                ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                MatrixHelper.addShortBufferToList(shortBuffer, inputFFTList);
            }
        }

        // Calculating the FFT for every sample
        List<double[]> outputMatrixList = Lists.partition(inputFFTList, CHUNK_SIZE).stream().map(FFTUtils::calculateFFT).collect(Collectors.toList());

        buildResponse(resp, outputMatrixList);
    }

    private void buildResponse(HttpServletResponse resp, List<double[]> outputMatrixList)
    {
        try (ServletOutputStream outputStream = resp.getOutputStream())
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(MatrixHelper.getMatrixFromList(outputMatrixList));
            outputStream.flush();
        }
        catch (IOException e)
        {
            Logger.getAnonymousLogger().severe(e.getMessage());
        }
    }
}
