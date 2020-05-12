package flashget;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

/**
 * DownloadTask that download file from given url which can
 * determined start point and size and create a file to given directory.
 *
 * @author Tatpol Samakpong.
 */
public class DownloadTask extends Task<Long> {

    /**
     * URL to download file.
     */
    private final URL url;

    /**
     * The path of the downloaded file.
     */
    private final File outfile;

    /**
     * Start point to download file.
     */
    private final long start;

    /**
     * The size of the file to download.
     */
    private final long size;

    /**
     * Constructor of DownloadTask with given url, path, start position, file size.
     *
     * @param url     of the file.
     * @param outfile is the path.
     * @param start   position of the file.
     * @param size    of the downloaded file.
     */
    public DownloadTask(URL url, File outfile, long start, long size) {
        this.url = url;
        this.outfile = outfile;
        this.start = start;
        this.size = size;
    }

    /**
     * Start the download. This method is called when start the task.
     *
     * @return downloaded file.
     */
    @Override
    public Long call() {
        // downloaded bytes
        long downloaded = 0;

        //
        try {
            // connect to the url
            URLConnection connection = url.openConnection();
            String range;

            if (size > 0) {
                range = String.format("bytes=%d-%d", start, start + size - 1);
            } else {
                range = String.format("bytes=%d-", start);
            }
            // set range to download
            connection.setRequestProperty("Range", range);

            InputStream in = connection.getInputStream();
            RandomAccessFile writer = new RandomAccessFile(outfile, "rwd");
            // seek to the start point of the file
            writer.seek(start);

            byte[] buffer = new byte[16 * 1024];
            int n;
            // read the file as buffer and write to path
            while ((n = in.read(buffer)) >= 0) {
                writer.write(buffer, 0, n);
                downloaded += n;
                // update value and progress to progress bar
                updateProgress(downloaded, size);
                updateValue(downloaded);

                // if cancel the task, it will break
                if (isCancelled()) {
                    break;
                }
            }
            in.close();
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return downloaded;
    }
}
