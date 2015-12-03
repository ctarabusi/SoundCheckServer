package s2m.fourier.servlets;

import com.google.common.collect.Lists;
import s2m.fourier.utils.FFTUtils;
import s2m.fourier.utils.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpectrogramServlet extends HttpServlet
{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        List<Double> inputFFTList = ServletUtils.extractSamples(req.getInputStream());

        // Calculating the FFT for every sample
        List<double[]> outputMatrixList = Lists.partition(inputFFTList, 2048).stream().map(FFTUtils::calculateFFT).collect(Collectors.toList());

        buildResponse(resp, outputMatrixList);
    }

    private void buildResponse(HttpServletResponse resp, List<double[]> outputMatrixList)
    {
        try (ServletOutputStream outputStream = resp.getOutputStream())
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(ServletUtils.convertListToMatrix(outputMatrixList));
            outputStream.flush();
        }
        catch (IOException e)
        {
            Logger.getAnonymousLogger().severe(e.getMessage());
        }
    }
}
