package com.gmail.aamnony.technionroomfinder.util;

import android.content.Context;

import com.gmail.aamnony.technionroomfinder.pojos.BusyRoom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public final class RepUtils
{
    private static final String COUSE_HEADER_LINE = "+------------------------------------------+\r\n";
    private static final String COURSE_GROUPS_LINE = "|               ++++++                  .סמ|";

    private static final Pattern COURSE_GROUPS_PATTERN = Pattern.compile("([\\p{L}.]+)\\s+(\\d+)\\s+(\\d+\\.\\d+)-(\\d+\\.\\d+)'(.)\\D+(\\d+)?\\s+\\|");

    private static final String REP_ZIP_FILE_URL = "http://ug3.technion.ac.il/rep/REPFILE.zip";
    private static final String REPFILE_ZIP_FILE_NAME = "REPFILE.zip";
    private static final String REPY_FILE_NAME = "REPY";

    public static List<BusyRoom> parse (String repFile)
    {
        List<BusyRoom> list = new LinkedList<>();
        int i = 0;
        while (true)
        {
            i = repFile.indexOf(COUSE_HEADER_LINE, i); // Start of course header.
            i = repFile.indexOf(COUSE_HEADER_LINE, i + COUSE_HEADER_LINE.length()); // End of course header.
            i = repFile.indexOf(COURSE_GROUPS_LINE, i + COUSE_HEADER_LINE.length()); // Start of course groups.
            if (i < 0)
            {
                break;
            }

            int j = repFile.indexOf(COUSE_HEADER_LINE, i + COURSE_GROUPS_LINE.length()); // Start of next course header.

            String courseGroups = repFile.substring(i, j);
            Matcher m1 = COURSE_GROUPS_PATTERN.matcher(courseGroups);
            while (m1.find())
            {
                list.add(new BusyRoom(m1));
            }
            i = j;
        }
        return list;
    }


    /**
     * @param repZipFile A {@link File} pointing to a REP file.
     * @return The file's content, in Unicode encoding.
     * @throws IOException                 If file does not exist, or an IO error occurred.
     * @throws UnsupportedCharsetException If IBM862 encoding is not supported on the device.
     */
    public static String extractUnicodeData (File repZipFile) throws IOException, UnsupportedCharsetException
    {
        Charset ibm862 = Charset.forName("IBM862");

        try (ZipFile zf = new ZipFile(repZipFile))
        {
            ZipEntry ze = zf.getEntry(REPY_FILE_NAME);

            try (InputStream is = zf.getInputStream(ze))
            {
                final byte[] buff = new byte[(int) ze.getSize()];
                final int readBulkSize = 4096;
                int len = 0;
                while (true)
                {
                    len = is.read(buff, len, readBulkSize);
                    if (len <= 0)
                    {
                        break;
                    }
                }
                return ibm862.decode(ByteBuffer.wrap(buff)).toString();
            }
        }
    }

    /**
     * Reads CP862/IBM862 encoded file, and converts it to Unicode.
     *
     * @param file A {@link File} that contains CP862 encoded text.
     * @return The file's content, in Unicode encoding.
     * @throws IOException If file does not exist or is not in CP862 encoding.
     */
    public static CharBuffer toUnicode (File file) throws IOException
    {
        CharBuffer cb = null;

        if (Charset.isSupported("IBM862"))
        {
            try (FileInputStream fis = new FileInputStream(file))
            {
                try (FileChannel channel = fis.getChannel())
                {
                    MappedByteBuffer bb = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                    Charset cp862 = Charset.forName("IBM862");
                    cb = cp862.decode(bb);
                }
            }
        }
        return cb;
    }

    public static void unzip (File zipFile) throws IOException
    {
        try (ZipFile zf = new ZipFile(zipFile))
        {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile)))
            {
                ZipEntry ze = zis.getNextEntry();
                byte[] buffer = new byte[1024];
                while (ze != null)
                {
                    String fileName = ze.getName();
                    File newFile = new File(zipFile.getParentFile(), fileName);

//                    //create all non exists folders
//                    //else you will hit FileNotFoundException for compressed folder
//                    if (!new File(newFile.getParent()).mkdirs())
//                    {
//                        throw new IOException("Cannot create sub-directories for Zip file");
//                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile))
                    {
                        int len;
                        while ((len = zis.read(buffer)) > 0)
                        {
                            fos.write(buffer, 0, len);
                        }
                    }
                    ze = zis.getNextEntry();
                }
            }
        }
    }

    /**
     * @return A {@link File} pointing to the downloaded REP zip file.
     * @throws IOException If an IO error occurred.
     */
    public static File downloadZip (Context context) throws IOException
    {
        URL url = new URL(REP_ZIP_FILE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");

        try (InputStream is = conn.getInputStream())
        {
            try (FileOutputStream fos = context.openFileOutput(REPFILE_ZIP_FILE_NAME, Context.MODE_PRIVATE))
            {
                final byte[] buff = new byte[4096];
                while (true)
                {
                    int len = is.read(buff);
                    if (len <= 0)
                    {
                        break;
                    }
                    fos.write(buff, 0, len);
                }
            }
        }
        return new File(context.getFilesDir(), REPFILE_ZIP_FILE_NAME);
    }
}
