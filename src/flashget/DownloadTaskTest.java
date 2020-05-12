package flashget;

import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DownloadTaskTest extends Task<Long> implements Downloader {

    private final URL url;
    private final File outfile;

    public DownloadTaskTest(URL url, File outfile) {
        this.url = url;
        this.outfile = outfile;
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
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return downloaded;
    }
}