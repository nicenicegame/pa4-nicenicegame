package flashget;

import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class MultiDownloadTask extends Task<Long> implements Downloader {

    private URL url;
    private File outfile;
    private long start;
    private long size;

    public MultiDownloadTask(URL url, File outfile, long start, long size) {
        this.url = url;
        this.outfile = outfile;
        this.start = start;
        this.size = size;
    }

    @Override
    public Long call() {
        long downloaded = 0;
        long length = 0;
        long percent = 0;

        try {
            URLConnection connection = url.openConnection();
            length = connection.getContentLengthLong();
            InputStream in = connection.getInputStream();
            OutputStream out = new FileOutputStream(outfile);

            byte[] buffer = new byte[16 * 1024];
            int n = 0;
            while ((n = in.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
                downloaded += n;
                percent = (downloaded * 100 / length);
                updateProgress(downloaded, length);
                updateValue(percent);
                updateMessage(Long.toString(percent));
                System.out.println("Downloading ... " + percent + "%");

                if (isCancelled()) {
                    System.out.println("Canceling the download ...");
                    break;
                }
            }
            in.close();
            out.close();
            updateProgress(downloaded, length);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return downloaded;
    }
}
