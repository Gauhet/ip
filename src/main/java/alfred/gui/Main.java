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
 * <p>This class does only what has to happen before there is a window at all:
 * it reads the layout, puts it on the stage, and hands the window the chatbot
 * to ask. What the window is made of is in {@code view/MainWindow.fxml}, and
 * what it does is {@link MainWindow}'s.
 *
 * <p>The chatbot is made here rather than in the window, because the window is
 * made by the loader, which cannot be asked to pass anything to it. Handing it
 * over afterwards also leaves the window able to work with another chatbot,
 * such as one saving somewhere else in a test.
 *
 * <p>The chatbot is the same one the console version runs, so a command typed
 * into the window is carried out and saved exactly as a typed one is, and the
 * two versions share a task list.
 */
public class Main extends Application {
    /** The chatbot the window is given. */
    private final AlfredTheButler alfred = new AlfredTheButler();

    /**
     * Builds the window and shows it.
     *
     * @param stage The primary stage JavaFX provides, which is the window
     *     itself.
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane mainLayout = fxmlLoader.load();
            stage.setScene(new Scene(mainLayout));
            fxmlLoader.<MainWindow>getController().setAlfred(alfred);

            // The layout gives the window its size but has nothing to say about
            // the window itself. The controls follow the edges they are
            // anchored to, so the window can be resized; the smallest sizes it
            // allows are the ones below which the conversation and the text
            // field stop being usable.
            stage.setTitle("AlfredTheButler");
            stage.setMinHeight(220);
            stage.setMinWidth(417);

            stage.show();
        } catch (IOException e) {
            // The layout ships with the program rather than coming from the
            // user, so a failure to read it is a fault in the program and
            // there is nothing the user could do about it.
            throw new IllegalStateException("Cannot read the main window layout", e);
        }
    }
}
