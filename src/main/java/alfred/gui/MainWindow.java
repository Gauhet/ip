package alfred.gui;

import alfred.AlfredTheButler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * What the main window does, as against what it looks like.
 *
 * <p>The arrangement of the window is described in
 * {@code view/MainWindow.fxml}, and this class holds what happens in it: the
 * greeting it opens with, and what becomes of a line the user sends. The two
 * are joined by name — a control given an {@code fx:id} in the file is set on
 * the field of the same name here, and a handler named in the file is this
 * class's method of that name.
 *
 * <p>The fields are private and annotated {@code @FXML} rather than made
 * public, so that the window's parts stay the window's own and are still
 * reachable by the loader.
 */
public class MainWindow extends AnchorPane {
    /** The scrolling view onto the conversation. */
    @FXML
    private ScrollPane scrollPane;

    /** The conversation so far, one dialog box per message. */
    @FXML
    private VBox dialogContainer;

    /** Where the user types. */
    @FXML
    private TextField userInput;

    /** What the user presses to send what they typed. */
    @FXML
    private Button sendButton;

    /** The chatbot the window asks for an answer to each line. */
    private AlfredTheButler alfred;

    /** The avatar shown beside what the user says. */
    private final Image userImage =
            new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));

    /** The avatar shown beside what Alfred says. */
    private final Image alfredImage =
            new Image(this.getClass().getResourceAsStream("/images/DaAlfred.png"));

    /**
     * Finishes setting up the window, once the loader has filled in its parts.
     *
     * <p>This is where the two things that cannot be written into the FXML
     * happen: the pane is told to follow the newest message down, and Alfred
     * greets the user before anything is typed.
     *
     * <p>The pane is scrolled to the end whenever the conversation grows taller,
     * rather than having its position bound to that height. A bound position
     * cannot be set by anything else, and scrolling is something else setting
     * it, so binding would leave the user unable to look back at what was said
     * earlier.
     */
    @FXML
    public void initialize() {
        dialogContainer.heightProperty().addListener((observable) -> scrollPane.setVvalue(1.0));
        dialogContainer.getChildren().add(DialogBox.getAlfredDialog(
                "Hello! I'm AlfredTheButler. What can I do for you?", alfredImage));
    }

    /**
     * Gives the window the chatbot it asks for an answer to each line.
     *
     * <p>The chatbot arrives after the window is built, because the loader
     * creates the window and knows nothing of chatbots. Whoever loads it hands
     * one over afterwards.
     *
     * @param alfred The chatbot to ask.
     */
    public void setAlfred(AlfredTheButler alfred) {
        this.alfred = alfred;
    }

    /**
     * Adds the line the user sent and Alfred's answer to it to the end of the
     * conversation, then empties the text field ready for the next line.
     *
     * <p>Both boxes are added at once, so that a line and its answer arrive
     * together as they do in a conversation.
     */
    @FXML
    private void handleUserInput() {
        String userText = userInput.getText();
        String alfredText = alfred.getResponse(userText);

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userText, userImage),
                DialogBox.getAlfredDialog(alfredText, alfredImage));
        userInput.clear();
    }
}
