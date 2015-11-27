package s2m.fourier;

import org.apache.commons.math.complex.Complex;
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

public class SpectrogramServlet extends HttpServlet
{
    private static int CHUNK_SIZE = 4096;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {

        BufferedInputStream in = null;

        byte[] buffer = new byte[CHUNK_SIZE];

        try
        {
            List<double[]> outputMatrixList = new ArrayList<double[]>();
            in = new BufferedInputStream(req.getInputStream());

            for (int length = 0; (length = in.read(buffer)) > 0; )
            {
                ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

                short[] samplesArray = new short[shortBuffer.limit()];
                shortBuffer.get(samplesArray);

                List<Double> inputFFTList = new ArrayList<Double>();
                for (short sample : samplesArray)
                {
                    inputFFTList.add((double) sample);
                }

                double[] inputArray = ServletUtils.doubleArrayToPrimitve(inputFFTList);
                double[] inputWithoutMeanFFT = ServletUtils.removeAverage(inputArray);
                double[] inputFFT = ServletUtils.addZeroPaddingToPowerTwo(inputWithoutMeanFFT);
                Complex[] outputFFT = new FastFourierTransformer().transform(inputFFT);

                Complex[] complexArray = Arrays.copyOfRange(outputFFT, 0, outputFFT.length / 2);
                double[] magnitudeArray = ServletUtils.getMagnitudeComponents(complexArray);
                outputMatrixList.add(magnitudeArray);
            }

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
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }
}
