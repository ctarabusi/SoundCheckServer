package s2m.fourier;

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

public class SpectrogramServlet extends HttpServlet
{
    private static int CHUNK_SIZE = 4096;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        List<double[]> outputMatrixList = new ArrayList<double[]>();

        InputStream in = null;

        byte[] buffer = new byte[CHUNK_SIZE];

        try
        {
            in = req.getInputStream();

            List<Double> inputFFTList = new ArrayList<Double>();

            for (int length = 0; (length = in.read(buffer)) > 0; )
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

            Logger.getAnonymousLogger().severe("number of elements " + outputMatrixList.size() + " row " + outputMatrixList.get(0).length);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }

        buildResponse(resp, outputMatrixList);
    }


    private void buildResponse(HttpServletResponse resp, List<double[]> outputMatrixList) throws IOException
    {
        ServletOutputStream outputStream = resp.getOutputStream();
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
        outputStream.close();
    }
}
