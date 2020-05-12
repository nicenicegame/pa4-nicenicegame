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

public class Controller {
    private File out;

    private long downloaded;

    private long fileSize;

    private DownloadTask[] downloadTasks;

    private ProgressBar[] progressBars;

    @FXML
    private Label filenameLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private Label threadLabel;

    @FXML
    private TextField urlField;

    @FXML
    private TextField saveField;

    @FXML
    private ProgressBar progressBar;

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

    @FXML
    private Button downloadButton;

    @FXML
    private Button browseButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        downloadButton.setOnAction(this::download);
        browseButton.setOnAction(this::browse);
        clearButton.setOnAction(this::clear);
        cancelButton.setOnAction(this::cancel);
        urlField.setOnMouseClicked(mouseEvent -> urlField.setStyle("-fx-border-color: transparent"));
        saveField.setOnMouseClicked(mouseEvent -> saveField.setStyle("-fx-border-color: transparent"));
    }

    public void download(ActionEvent event) {
        String url = urlField.getText().trim();
        String filename = url.substring(url.lastIndexOf('/') + 1);
        URL fileUrl = null;
        DownloaderFactory downloaderFactory = DownloaderFactory.getInstance();
        downloaded = 0;

        try {
            fileUrl = new URL(url);
            URLConnection connection = fileUrl.openConnection();
            fileSize = connection.getContentLengthLong();
            if (!urlField.getText().isEmpty() && saveField.getText().isEmpty()) {
                try {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("File path is not set up.");
                    alert.setContentText("The path will be set to system properties. Are you OK?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        out = new File(System.getProperty("user.home") + "\\" + filename);
                        saveField.setText(out.getPath());
                    }
                } catch (NullPointerException ignored) {
                }
            } else if (!urlField.getText().isEmpty() && !saveField.getText().isEmpty()) {
                try {
                    threadLabel.setVisible(true);
                    progressBar.setVisible(true);
                    cancelButton.setVisible(true);
                    filenameLabel.setText(out.getName());
                    filenameLabel.setVisible(true);
                    progressLabel.setVisible(true);
                    out = new File(saveField.getText());
                    int numThread = 5;

                    progressBars = new ProgressBar[]{bar1, bar2, bar3, bar4, bar5};
                    downloadTasks = downloaderFactory.getDownloadTasks(fileUrl, out, fileSize, numThread);
                    ExecutorService executor = Executors.newFixedThreadPool(numThread + 1);

                    for (ProgressBar progressBar : progressBars) {
                        progressBar.setVisible(true);
                    }

                    for (int i = 0; i < downloadTasks.length; i++) {
                        downloadTasks[i].valueProperty().addListener(this::valueChanged);
                        progressBars[i].progressProperty().bind(downloadTasks[i].progressProperty());
                    }
                    progressBar.progressProperty().bind(downloadTasks[0].progressProperty().multiply(0.2).add(downloadTasks[1].progressProperty().multiply(0.2).add(downloadTasks[2].progressProperty().multiply(0.2).add(downloadTasks[3].progressProperty().multiply(0.2).add(downloadTasks[4].progressProperty().multiply(0.2))))));

                    for (DownloadTask task : downloadTasks) {
                        executor.execute(task);
                    }
                    if (executor.isTerminated()) {
                        progressLabel.setText("Finished!");
                        cancelButton.setText("OK");
                    }
                    executor.shutdown();

                } catch (NullPointerException ignored) {
                }
            } else if (urlField.getText().isEmpty() && !saveField.getText().isEmpty()) {
                urlField.setStyle("-fx-border-color: red");
            }
        } catch (IOException e) {
            urlField.setStyle("-fx-border-color: red");
            error("URL is invalid!");
        }
    }

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

    public void browse(ActionEvent event) {
        String url = urlField.getText().trim();
        String filename = url.substring(url.lastIndexOf('/') + 1);
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("All files", "*" + filename);
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName(filename);
        fileChooser.setTitle("Save As");

        fileChooser.setInitialFileName(filename);
        out = fileChooser.showSaveDialog(new Stage());
        try {
            saveField.setText(out.getPath());
        } catch (NullPointerException ignored) {
        }
    }

    public void clear(ActionEvent event) {
        filenameLabel.setVisible(false);
        progressLabel.setVisible(false);
        urlField.clear();
        saveField.clear();
        urlField.setStyle("-fx-border-color: transparent");
        saveField.setStyle("-fx-border-color: transparent");
    }

    public void valueChanged(ObservableValue<? extends Long> observableValue, Long oldValue, Long newValue) {
        if (oldValue == null) oldValue = 0L;
        downloaded += newValue - oldValue;
        long percent = (downloaded * 100) / fileSize;
        progressLabel.setText(String.format("%d/%d (%d%%)", downloaded, fileSize, percent));
    }

    public void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}