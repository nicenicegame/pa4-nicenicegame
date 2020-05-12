package flashget;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FlashGet is a downloader app with GUI that can download a file
 * from url and can choose a path to save a file.
 *
 * @author Tatpol Samakpong.
 */
public class Main extends Application {

    /**
     * Launch the FlashGet app.
     *
     * @param args to launch app.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialize the view of FlashGet from fxml file.
     *
     * @param primaryStage to show the stage as window.
     * @throws Exception when cannot load fxml file.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("view/UI.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("FlashGet");
        primaryStage.show();
    }
}
