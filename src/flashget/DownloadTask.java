package flashget;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

public class DownloadTask extends Task<Long> {

    private final URL url;
    private final File outfile;
    private final long start;
    private final long size;

    public DownloadTask(URL url, File outfile, long start, long size) {
        this.url = url;
        this.outfile = outfile;
        this.start = start;
        this.size = size;
    }

    @Override
    public Long call() {
        long downloaded = 0;
        long percent = 0;

        try {
            URLConnection connection = url.openConnection();
            String range = null;

            if (size > 0) {
                range = String.format("bytes=%d-%d", start, start + size - 1);
            } else {
                range = String.format("bytes=%d-", start);
            }
            connection.setRequestProperty("Range", range);

            InputStream in = connection.getInputStream();
            RandomAccessFile writer = new RandomAccessFile(outfile, "rwd");
            writer.seek(start);

            byte[] buffer = new byte[16 * 1024];
            int n = 0;
            while ((n = in.read(buffer)) >= 0) {
                writer.write(buffer, 0, n);
                downloaded += n;
                updateProgress(downloaded, size);
                updateValue(downloaded);

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
