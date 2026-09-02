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
 * <p>The window echoes and nothing more. A line sent with the button or the
 * Enter key appears in the conversation as the user's own dialog box, but
 * nothing answers it: the window is not connected to the part of the program
 * that reads commands and keeps the task list.
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

        // Alfred greets the user before anything is typed, as the console
        // version of the program does when it starts.
        dialogContainer.getChildren().add(DialogBox.createAlfredDialog(
                "Hello! I'm AlfredTheButler. What can I do for you?", alfredImage));

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

        // Handling user input

        // The two ways of sending a line are the button and the Enter key, and
        // both do the same thing. A button click is a mouse event, while Enter
        // in a text field is that field's own action event, so the two are set
        // separately even though they share a handler.
        sendButton.setOnMouseClicked((event) -> {
            handleUserInput();
        });
        userInput.setOnAction((event) -> {
            handleUserInput();
        });

        // A new dialog box makes the container taller, and the pane stays where
        // it was, so the newest message ends up below the bottom of the view.
        // Scrolling to the end whenever the height changes keeps it in sight.
        dialogContainer.heightProperty().addListener((observable) -> scrollPane.setVvalue(1.0));
    }

    /**
     * Shows what the user typed as a new dialog box at the end of the
     * conversation, then empties the text field ready for the next line.
     *
     * <p>Nothing answers it yet. The line is shown and no more, because the
     * window is not connected to the part of the program that reads commands.
     */
    private void handleUserInput() {
        dialogContainer.getChildren().add(DialogBox.createUserDialog(userInput.getText(), userImage));
        userInput.clear();
    }
}
