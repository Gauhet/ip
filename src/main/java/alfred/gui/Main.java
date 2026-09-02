package alfred.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The window of the graphical version of Alfred.
 *
 * <p>The window is built out of nested containers. An {@link AnchorPane} is the
 * root, because it can pin each of its children to an edge of the window: the
 * conversation to the top, the text field and the button to the bottom. The
 * conversation itself is a {@link VBox} of {@link DialogBox}es, one below the
 * next, held inside a {@link ScrollPane} so that a conversation longer than the
 * window can be scrolled through.
 *
 * <p>The window does nothing yet. Typing into the text field and pressing the
 * button have no effect, because nothing is listening to either of them.
 */
public class Main extends Application {
    /** The avatar shown beside what the user says. */
    private final Image userImage =
            new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));

    /** The avatar shown beside what Alfred says. */
    private final Image alfredImage =
            new Image(this.getClass().getResourceAsStream("/images/DaAlfred.png"));

    /** The scrolling view onto the conversation. */
    private ScrollPane scrollPane;

    /** The conversation so far, one dialog box per message. */
    private VBox dialogContainer;

    /** Where the user types. */
    private TextField userInput;

    /** What the user presses to send what they typed. */
    private Button sendButton;

    /**
     * Builds the window and shows it.
     *
     * @param stage The primary stage JavaFX provides, which is the window
     *     itself.
     */
    @Override
    public void start(Stage stage) {
        // Setting up required components

        scrollPane = new ScrollPane();
        dialogContainer = new VBox();
        scrollPane.setContent(dialogContainer);

        userInput = new TextField();
        sendButton = new Button("Send");

        // A sample exchange, so that the layout can be seen before there is
        // anything to put in it.
        DialogBox greeting = DialogBox.createAlfredDialog(
                "Hello! I'm AlfredTheButler. What can I do for you?", alfredImage);
        DialogBox reply = DialogBox.createUserDialog("Hello!", userImage);
        dialogContainer.getChildren().addAll(greeting, reply);

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        Scene scene = new Scene(mainLayout);

        stage.setScene(scene);
        stage.show();

        // Formatting the window to look as expected

        stage.setTitle("AlfredTheButler");
        stage.setResizable(false);
        stage.setMinHeight(600.0);
        stage.setMinWidth(400.0);

        mainLayout.setPrefSize(400.0, 600.0);

        scrollPane.setPrefSize(385, 535);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Scrolled to the bottom, where the newest message is, and as wide as
        // the pane so that a dialog box does not leave a gap at the side.
        scrollPane.setVvalue(1.0);
        scrollPane.setFitToWidth(true);

        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        userInput.setPrefWidth(325.0);

        sendButton.setPrefWidth(55.0);

        AnchorPane.setTopAnchor(scrollPane, 1.0);

        AnchorPane.setBottomAnchor(sendButton, 1.0);
        AnchorPane.setRightAnchor(sendButton, 1.0);

        AnchorPane.setLeftAnchor(userInput, 1.0);
        AnchorPane.setBottomAnchor(userInput, 1.0);
    }
}
