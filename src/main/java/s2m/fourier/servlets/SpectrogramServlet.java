package s2m.fourier.servlets;

import s2m.fourier.utils.ServletUtils;

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

public class SpectrogramServlet extends HttpServlet
{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        int CHUNK_SIZE = 2048;

        List<double[]> outputMatrixList = new ArrayList<>();

        byte[] buffer = new byte[CHUNK_SIZE];

        try (InputStream is = req.getInputStream())
        {
            List<Double> inputFFTList = new ArrayList<Double>();

            for (int length = is.read(buffer); length != -1; length = is.read(buffer))
            {
                ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

                short[] samplesArray = new short[shortBuffer.limit()];
                shortBuffer.get(samplesArray);

                for (short sample : samplesArray)
                {
                    inputFFTList.add((double) sample);
                }
            }

            int rowsAvailable = inputFFTList.size() / CHUNK_SIZE;

            for (int i = 0; i < rowsAvailable; i++)
            {
                List<Double> chunkList = inputFFTList.subList(i * CHUNK_SIZE, i * CHUNK_SIZE + CHUNK_SIZE);
                outputMatrixList.add(ServletUtils.calculateFFT(chunkList));
            }
        }

        buildResponse(resp, outputMatrixList);
    }

    private void buildResponse(HttpServletResponse resp, List<double[]> outputMatrixList) throws IOException
    {
        try (ServletOutputStream outputStream = resp.getOutputStream())
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            double[][] outputMatrix = new double[outputMatrixList.size()][outputMatrixList.get(0).length];
            int i = 0;
            for (double[] row : outputMatrixList)
            {
                outputMatrix[i] = row;
                i++;
            }

            objectOutputStream.writeObject(outputMatrix);

            outputStream.flush();
        }
    }
}
