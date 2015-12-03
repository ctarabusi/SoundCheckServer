package s2m.fourier.servlets;

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

public class FFTServlet extends HttpServlet
{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // Read Input Sample Stream
        List<Double> inputFFTList = ServletUtils.extractSamples(req.getInputStream());

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
