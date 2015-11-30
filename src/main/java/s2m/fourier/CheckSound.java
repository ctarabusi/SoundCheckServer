package s2m.fourier;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CheckSound extends HttpServlet
{
    private static int CHUNK_SIZE = 4096;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        byte[] buffer = new byte[CHUNK_SIZE];

        long[] recorderHashArray = readRecordings(buffer);

        int rowsAvailable;
        int i;

        List<double[]> outputMatrixList = new ArrayList<double[]>();

        InputStream in = null;

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

            rowsAvailable = inputFFTList.size() / CHUNK_SIZE;

            for (i = 0; i < rowsAvailable; i++)
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

        buildResponse(resp, outputMatrixList, recorderHashArray);
    }

    private long[] readRecordings(byte[] buffer) throws IOException
    {
        ServletContext context = getServletContext();
        String fullPath = context.getRealPath("/WEB-INF/Recording.wav");

        List<Double> recordingAudioFFTList = new ArrayList<Double>();
        List<double[]> inputMatrixList = new ArrayList<double[]>();

        File recordingFile = new File(fullPath);
        FileInputStream fileInputStream = new FileInputStream(recordingFile);
        // ignoring header
        fileInputStream.read(new byte[44]);
        for (int length = 0; (length = fileInputStream.read(buffer)) > 0; )
        {
            ShortBuffer shortBuffer = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

            short[] samplesArray = new short[shortBuffer.limit()];
            shortBuffer.get(samplesArray);

            for (short sample : samplesArray)
            {
                recordingAudioFFTList.add((double) sample);
            }
        }

        int rowsAvailable = recordingAudioFFTList.size() / CHUNK_SIZE;

        for (int i = 0; i < rowsAvailable; i++)
        {
            List<Double> chunkList = recordingAudioFFTList.subList(i * CHUNK_SIZE, i * CHUNK_SIZE + CHUNK_SIZE);
            inputMatrixList.add(ServletUtils.calculateFFT(chunkList));
        }

        double[][] outputMatrix = new double[inputMatrixList.size()][inputMatrixList.get(0).length];
        int i = 0;
        for (double[] row : inputMatrixList)
        {
            outputMatrix[i] = row;
            i++;
        }

        return findFrequencyPeaks(outputMatrix);
    }

    private void buildResponse(HttpServletResponse resp, List<double[]> outputMatrixList, long[] recorderHashArray) throws IOException
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

        long[] hashArray = findFrequencyPeaks(outputMatrix);

        findMatch(hashArray, recorderHashArray);

        outputStream.flush();
        outputStream.close();
    }

    private void findMatch(long[] hashArray, long[] recorderHashArray)
    {
        Logger.getAnonymousLogger().severe("findMatch " + hashArray.length + " inputHashArray " + recorderHashArray.length);

        Logger.getAnonymousLogger().severe("posted hashArray " + Arrays.toString(hashArray));
        Logger.getAnonymousLogger().severe("recorderHashArray " + Arrays.toString(recorderHashArray));

        Map<Long, List<Integer>> sparseLongArray = new HashMap<Long, List<Integer>>();
        int index = 0;
        for (long frequencyPattern : recorderHashArray)
        {
            if (frequencyPattern != 0)
            {
                List<Integer> listPositionForFrequency = sparseLongArray.get(frequencyPattern);
                if (listPositionForFrequency == null)
                {
                    listPositionForFrequency = new ArrayList<Integer>();
                }
                listPositionForFrequency.add(index);
                sparseLongArray.put(frequencyPattern, listPositionForFrequency);
            }

            index++;
        }
        Logger.getAnonymousLogger().severe("sparseLongArray " + sparseLongArray);


        List<Integer> positionFound = new ArrayList<Integer>();
        for (long hash : hashArray)
        {
            List<Integer> currentPositions = sparseLongArray.get(hash);
            if (currentPositions != null)
            {
                positionFound.addAll(currentPositions);
            }
        }

        Logger.getAnonymousLogger().severe("positionFound " + positionFound);

        int elementsInOrder = 0;
        int currentPosition = 0;
        for (Integer position : positionFound)
        {
            if (currentPosition < position)
            {
                currentPosition = position;
                elementsInOrder++;
            }
        }
        Logger.getAnonymousLogger().severe("elementsInOrder " + elementsInOrder);
    }

    private long[] findFrequencyPeaks(double[][] outputMatrix)
    {
        long[] hashArray = new long[outputMatrix[0].length / 4];

        int currentIndex = 0;
        for (double[] instantFrequencies : outputMatrix)
        {
            int size = instantFrequencies.length;

            int sectionSize = size / 4;

            int firstMax = findFrequencyWithMaxAmplitude(Arrays.copyOfRange(instantFrequencies, 0, sectionSize), 0);
            int secondMax = findFrequencyWithMaxAmplitude(Arrays.copyOfRange(instantFrequencies, sectionSize, sectionSize * 2), sectionSize);
            int thirdMax = findFrequencyWithMaxAmplitude(Arrays.copyOfRange(instantFrequencies, sectionSize * 2, sectionSize * 3), sectionSize * 2);
            int fourthMax = findFrequencyWithMaxAmplitude(Arrays.copyOfRange(instantFrequencies, sectionSize * 3, size), sectionSize * 3);

            long hash = hash(firstMax, secondMax, thirdMax, fourthMax);
            Logger.getAnonymousLogger().severe("" + hash);
            hashArray[currentIndex] = hash;
            currentIndex++;
        }
        return hashArray;
    }


    //Using a little bit of error-correction, damping
    private static final int FUZ_FACTOR = 2;

    private long hash(int firstMax, int secondMax, int thirdMax, int fourthMax)
    {
        long p1 = firstMax;
        long p2 = secondMax;
        long p3 = thirdMax;
        long p4 = fourthMax;
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR)) * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100 + (p1 - (p1 % FUZ_FACTOR));
    }

    private int findFrequencyWithMaxAmplitude(double[] array, int baseIndex)
    {
        int max = 0;
        for (int counter = 1; counter < array.length; counter++)
        {
            if (array[counter] > max)
            {
                max = baseIndex + counter;
            }
        }
        return max;
    }
}
