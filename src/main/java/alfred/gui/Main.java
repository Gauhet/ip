package alfred.gui;

import java.io.IOException;

import alfred.AlfredTheButler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The graphical version of Alfred, as a JavaFX application.
 *
 * <p>This class reads the layout, puts it on the stage, and hands the window the
 * chatbot to ask. The chatbot is made here because the window is made by the
 * loader, which cannot be asked to pass anything to it. It is the same chatbot
 * the console version runs, so the two share a task list.
 */
public class Main extends Application {
    /** The chatbot the window is given. */
    private final AlfredTheButler alfred = new AlfredTheButler();

    /**
     * Builds the window and shows it.
     *
     * @param stage the primary stage JavaFX provides, which is the window itself.
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane mainLayout = fxmlLoader.load();
            stage.setScene(new Scene(mainLayout));
            fxmlLoader.<MainWindow>getController().setAlfred(alfred);

            // The controls follow the edges they are anchored to, so the window
            // can be resized; the smallest sizes it allows are the ones below
            // which the conversation and the text field stop being usable.
            stage.setTitle("AlfredTheButler");
            stage.setMinHeight(220);
            stage.setMinWidth(417);

            stage.show();
        } catch (IOException e) {
            // The layout ships with the program, so a failure to read it is a
            // fault in the program and not something the user could act on.
            throw new IllegalStateException("Cannot read the main window layout", e);
        }
    }
}
