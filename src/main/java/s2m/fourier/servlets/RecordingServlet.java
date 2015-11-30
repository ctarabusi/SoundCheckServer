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

        InputStream is = req.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream(new File(fullPath));

        try
        {
            byte[] buf = new byte[4096];
            for (int nChunk = is.read(buf); nChunk != -1; nChunk = is.read(buf))
            {
                fileOutputStream.write(buf, 0, nChunk);
            }
        }
        finally
        {
            is.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        }
    }

}
