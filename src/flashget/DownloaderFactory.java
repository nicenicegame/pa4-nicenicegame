package flashget;

import java.io.File;
import java.net.URL;

public class DownloaderFactory {

    public static Downloader createDownloader(URL url, File outfile) {
        return new DownloadTask(url, outfile);
    }
}
