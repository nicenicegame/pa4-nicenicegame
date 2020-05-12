package flashget;

import java.io.File;
import java.net.URL;

public class DownloaderFactory {
    private static DownloaderFactory instance;

    public static DownloaderFactory getInstance() {
        if (instance == null) instance = new DownloaderFactory();
        return instance;
    }

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
