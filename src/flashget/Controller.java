package flashget;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller class for handle the event and UI of the app.
 *
 * @author Tatpol Samakpong.
 */
public class Controller {
    /**
     * The file directory.
     */
    private File out;

    /**
     * Downloaded bytes of the file.
     */
    private long downloaded;

    /**
     * Total file size.
     */
    private long fileSize;

    /**
     * Array of download tasks.
     */
    private DownloadTask[] downloadTasks;

    /**
     * Array of progress bars.
     */
    private ProgressBar[] progressBars;

    /**
     * Show the downloading file name.
     */
    @FXML
    private Label filenameLabel;

    /**
     * Show to progression of downloading.
     */
    @FXML
    private Label progressLabel;

    /**
     * Thread label.
     */
    @FXML
    private Label threadLabel;

    /**
     * URL field for input of url.
     */
    @FXML
    private TextField urlField;

    /**
     * Input of the file path.
     */
    @FXML
    private TextField saveField;

    /**
     * Main progress bar.
     */
    @FXML
    private ProgressBar progressBar;

    /**
     * Progress bars for each download tasks.
     */
    @FXML
    private ProgressBar bar1;

    @FXML
    private ProgressBar bar2;

    @FXML
    private ProgressBar bar3;

    @FXML
    private ProgressBar bar4;

    @FXML
    private ProgressBar bar5;

    /**
     * Download button to start downloading.
     */
    @FXML
    private Button downloadButton;

    /**
     * Browse the file to selected path.
     */
    @FXML
    private Button browseButton;

    /**
     * Clear button to clear the field.
     */
    @FXML
    private Button clearButton;

    /**
     * Cancel button to cancel all downloading tasks
     */
    @FXML
    private Button cancelButton;

    /**
     * Initialize the action to each button to handle event correctly.
     */
    @FXML
    public void initialize() {
        downloadButton.setOnAction(this::download);
        browseButton.setOnAction(this::browse);
        clearButton.setOnAction(this::clear);
        cancelButton.setOnAction(this::cancel);
        urlField.setOnMouseClicked(mouseEvent -> urlField.setStyle("-fx-border-color: transparent"));
        saveField.setOnMouseClicked(mouseEvent -> saveField.setStyle("-fx-border-color: transparent"));
    }

    /**
     * Download event that start downloading the file.
     * When the save field is empty, it will set to system properties
     * with confirmation dialog.
     *
     * @param event to download.
     */
    public void download(ActionEvent event) {
        // get url from the field
        String url = urlField.getText().trim();

        // get file name from url which is usually the name after last /
        String filename = url.substring(url.lastIndexOf('/') + 1);

        // create download factory object
        DownloaderFactory downloaderFactory = DownloaderFactory.getInstance();

        URL fileUrl;
        downloaded = 0; // downloade byte is set to 0 every time you start download

        try {
            fileUrl = new URL(url);
            URLConnection connection = fileUrl.openConnection();
            fileSize = connection.getContentLengthLong();

            // if the save field is empty, there will be the confirmation dialog to ask for set path to initial
            if (!urlField.getText().isEmpty() && saveField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText("File path is not set up.");
                alert.setContentText("The path will be set to system properties. Are you OK?");

                Optional<ButtonType> result = alert.showAndWait();
                try {
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        out = new File(System.getProperty("user.home") + "\\" + filename);
                        saveField.setText(out.getPath());
                    }
                } catch (NullPointerException ignored) {
                }
                // if both field are fill up, it will start downloading
            } else if (!urlField.getText().isEmpty() && !saveField.getText().isEmpty()) {
                threadLabel.setVisible(true);
                progressBar.setVisible(true);
                cancelButton.setVisible(true);
                filenameLabel.setText(out.getName());
                filenameLabel.setVisible(true);
                progressLabel.setVisible(true);
                out = new File(saveField.getText());
                int numThread = 5;

                // create array of progress bars
                progressBars = new ProgressBar[]{bar1, bar2, bar3, bar4, bar5};

                // get download tasks form downloader factory object
                downloadTasks = downloaderFactory.getDownloadTasks(fileUrl, out, fileSize, numThread);

                // create executor service to execute all of the download task
                ExecutorService executor = Executors.newFixedThreadPool(numThread + 1);

                // set visible progress bars
                for (ProgressBar progressBar : progressBars) {
                    progressBar.setVisible(true);
                }

                // bind to progress property of the download tasks to all progress bars
                // and add listener to download tasks
                for (int i = 0; i < downloadTasks.length; i++) {
                    downloadTasks[i].valueProperty().addListener(this::valueChanged);
                    progressBars[i].progressProperty().bind(downloadTasks[i].progressProperty());
                }
                // bind progress properties of each progress bars to the main progress bar.
                progressBar.progressProperty().bind(downloadTasks[0].progressProperty().multiply(0.2).add(downloadTasks[1].progressProperty().multiply(0.2).add(downloadTasks[2].progressProperty().multiply(0.2).add(downloadTasks[3].progressProperty().multiply(0.2).add(downloadTasks[4].progressProperty().multiply(0.2))))));

                // execute each download tasks
                for (DownloadTask task : downloadTasks) {
                    executor.execute(task);
                }
                // shutdown when finished the execution
                executor.shutdown();

            } else if (urlField.getText().isEmpty() && !saveField.getText().isEmpty()) {
                urlField.setStyle("-fx-border-color: red");
            }
        } catch (IOException e) {
            urlField.setStyle("-fx-border-color: red");
            error("URL is invalid!");
        }
    }

    /**
     * Cancel all running download tasks.
     *
     * @param event to cancel.
     */
    public void cancel(ActionEvent event) {
        for (DownloadTask downloadTask : downloadTasks) {
            downloadTask.cancel();
        }
        for (ProgressBar progressBar : progressBars) {
            progressBar.setVisible(false);
        }
        threadLabel.setVisible(false);
        filenameLabel.setVisible(false);
        progressLabel.setVisible(false);
        progressBar.setVisible(false);
        cancelButton.setVisible(false);
    }

    /**
     * Browse the file to choose the path and set the filename manually.
     *
     * @param event to browse the file.
     */
    public void browse(ActionEvent event) {
        String url = urlField.getText().trim();
        String filename = url.substring(url.lastIndexOf('/') + 1);
        String fileExt = filename.substring(filename.lastIndexOf('.'));
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("All files", "*" + fileExt);
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName(filename);
        fileChooser.setTitle("Save As");
        out = fileChooser.showSaveDialog(new Stage());
        try {
            saveField.setText(out.getPath());
        } catch (NullPointerException ignored) {
        }
    }

    /**
     * Clear the fields.
     *
     * @param event to clear the fields.
     */
    public void clear(ActionEvent event) {
        filenameLabel.setVisible(false);
        progressLabel.setVisible(false);
        urlField.clear();
        saveField.clear();
        urlField.setStyle("-fx-border-color: transparent");
        saveField.setStyle("-fx-border-color: transparent");
    }

    /**
     * Update the downloaded bytes and show to UI.
     *
     * @param observableValue is the value which is observable.
     * @param oldValue        is the value before it changed.
     * @param newValue        is the changed value.
     */
    public void valueChanged(ObservableValue<? extends Long> observableValue, Long oldValue, Long newValue) {
        if (oldValue == null) oldValue = 0L;
        downloaded += newValue - oldValue;
        long percent = ((downloaded + 1) * 100) / fileSize;
        progressLabel.setText(String.format("%d/%d (%d%%)", downloaded + 1, fileSize, percent));
    }

    /**
     * Error alert with the message.
     *
     * @param message to show error.
     */
    public void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}