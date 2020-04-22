package flashget;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    private VBox vBox;

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
    private Button cancelButton;

    @FXML
    public void initialize() {
        downloadButton.setOnAction(this::download);
        browseButton.setOnAction(this::browse);
        cancelButton.setOnAction(this::cancel);
    }

    public void download(ActionEvent event) {
        String url = urlField.getText().trim();

        ChangeListener<Long> listener = new ChangeListener<Long>() {
            @Override
            public void changed(ObservableValue<? extends Long> observable, Long oldValue, Long newValue) {
                progressBar.setVisible(true);
                percentLabel.setVisible(true);
                percentLabel.setText(newValue + "%");
            }
        };

        if (!urlField.getText().isEmpty() && saveField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("File path is not set up.");
            alert.setContentText("The downloaded file will be saved in system properties.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                out = new File(System.getProperty("user.home"));
                try {
                    worker = new DownloadTask(new URL(url), out);
                } catch (MalformedURLException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        try {
            worker = new DownloadTask(new URL(url), out);
            progressBar.progressProperty().bind(worker.progressProperty());
            worker.valueProperty().addListener(listener);
            new Thread(worker).start();
        } catch (NullPointerException ignored) {
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void cancel(ActionEvent event) {
        try {
            worker.cancel();
        } catch (NullPointerException ignored) {
        }
    }

    public void browse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        out = fileChooser.showSaveDialog(new Stage());
        try {
            saveField.setText(out.getPath());
        } catch (NullPointerException ignored) {
        }
    }

}
