package s2m.fourier.servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RecordingServlet extends HttpServlet
{
    public static String RECORDING_AUDIO_PATH = "/WEB-INF/Recording.wav";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ServletContext context = getServletContext();
        String fullPath = context.getRealPath(RECORDING_AUDIO_PATH);

        FileOutputStream fileOutputStream = new FileOutputStream(new File(fullPath));

        try (InputStream is = req.getInputStream())
        {
            byte[] buffer = new byte[2048];
            for (int length = is.read(buffer); length != -1; length = is.read(buffer))
            {
                fileOutputStream.write(buffer, 0, length);
            }
        }
        finally
        {
            fileOutputStream.flush();
            fileOutputStream.close();
        }
    }
}
