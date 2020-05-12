package flashget;

import java.io.File;
import java.net.URL;

/**
 * DownloaderFactory for create the download tasks.
 *
 * @author Tatpol Samakpong.
 */
public class DownloaderFactory {
    /**
     * Singleton instance.
     */
    private static DownloaderFactory instance;

    /**
     * Constructor of DownloaderFactory.
     */
    private DownloaderFactory() {
    }

    /**
     * Get a singleton instance of the DownloaderFactory.
     */
    public static DownloaderFactory getInstance() {
        if (instance == null) instance = new DownloaderFactory();
        return instance;
    }

    /**
     * Return the download tasks as an array to use for downloading.
     *
     * @param fileUrl   to download.
     * @param out       is the path to save file as.
     * @param fileSize  is the total file size.
     * @param numThread is number of the thread want to use.
     * @return download tasks as an array.
     */
    public DownloadTask[] getDownloadTasks(URL fileUrl, File out, long fileSize, int numThread) {
        DownloadTask[] downloadTasks = new DownloadTask[numThread];
        long chunk = fileSize / numThread;
        for (int i = 0; i < numThread; i++) {
            if (i == numThread - 1) {
                downloadTasks[i] = new DownloadTask(fileUrl, out, (chunk * (numThread - 1)) + 1, fileSize - (chunk * (numThread - 1)));
            } else {
                downloadTasks[i] = new DownloadTask(fileUrl, out, (chunk * i) + 1, chunk);
            }
        }
        return downloadTasks;
    }
}
