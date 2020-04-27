package flashget;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class Controller {
    private File out;

    private DownloadTask worker;

    @FXML
    private Label percentLabel;

    @FXML
    private TextField urlField;

    @FXML
    private TextField saveField;

    @FXML
    private ProgressBar progressBar;

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
    }

    public void download(ActionEvent event) {
        String url = urlField.getText().trim();
        String filename = url.substring(url.lastIndexOf('/') + 1);

        ChangeListener<Long> listener = (observable, oldValue, newValue) -> {
            cancelButton.setVisible(true);
            progressBar.setVisible(true);
            percentLabel.setVisible(true);
            percentLabel.setText(newValue.toString() + "%");
        };

        if (!urlField.getText().isEmpty() && saveField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("File path is not set up.");
            alert.setContentText("The downloaded file will be saved in system properties.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                out = new File(System.getProperty("user.home") + "\\" + filename);
                saveField.setText(out.getPath());
                try {
                    worker = new DownloadTask(new URL(url), out);
                } catch (MalformedURLException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        if (!urlField.getText().isEmpty() && !saveField.getText().isEmpty()) {
            try {
                worker = new DownloadTask(new URL(url), out);
                progressBar.progressProperty().bind(worker.progressProperty());
                worker.valueProperty().addListener(listener);
                new Thread(worker).start();

                worker.setOnRunning(workerStateEvent -> {
                    downloadButton.setDisable(true);
                    clearButton.setDisable(true);
                });

                worker.setOnSucceeded(workerStateEvent -> {
                    cancelButton.setText("OK");
                    percentLabel.setText("Finished!");
                });

            } catch (NullPointerException ignored) {
            } catch (MalformedURLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void cancel(ActionEvent event) {
        try {
            worker.cancel();
        } catch (NullPointerException ignored) {
        }
        downloadButton.setDisable(false);
        clearButton.setDisable(false);
        progressBar.setVisible(false);
        percentLabel.setVisible(false);
        cancelButton.setVisible(false);
    }

    public void browse(ActionEvent event) {
        String url = urlField.getText().trim();
        String filename = url.substring(url.lastIndexOf("/") + 1);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.setInitialFileName(filename);
        out = fileChooser.showSaveDialog(new Stage());
        try {
            saveField.setText(out.getPath());
        } catch (NullPointerException ignored) {
        }
    }

    public void clear(ActionEvent event) {
        urlField.clear();
        saveField.clear();
    }
}