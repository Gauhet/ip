package alfred.gui;

import alfred.AlfredTheButler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * What the main window does, as against what it looks like.
 *
 * <p>The arrangement of the window is described in
 * {@code view/MainWindow.fxml}, and this class holds what happens in it. The two
 * are joined by name: a control given an {@code fx:id} in the file is set on the
 * field of the same name here, and a handler named in the file is this class's
 * method of that name.
 */
public class MainWindow extends VBox {
    /** The scroll position that shows the foot of the conversation. */
    private static final double SCROLL_TO_BOTTOM = 1.0;

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

    /** The avatar shown beside what Alfred says. */
    private final Image alfredImage =
            new Image(this.getClass().getResourceAsStream("/images/DaAlfred.png"));

    /**
     * Finishes setting up the window, once the loader has filled in its parts.
     *
     * <p>The pane is scrolled to the end whenever the conversation grows taller.
     * Binding its position to that height instead would leave the user unable to
     * scroll back.
     *
     * <p>The send button is grayed out while there is nothing to send, so that
     * it says at a glance whether pressing it would do anything.
     */
    @FXML
    public void initialize() {
        dialogContainer.heightProperty()
                .addListener((observable) -> scrollPane.setVvalue(SCROLL_TO_BOTTOM));
        sendButton.disableProperty().bind(userInput.textProperty().isEmpty());
    }

    /**
     * Gives the window the chatbot it asks for an answer to each line.
     *
     * <p>Alfred greets the user here rather than in {@link #initialize()}, which
     * runs while there is still no chatbot to ask.
     *
     * @param alfred the chatbot to ask.
     */
    public void setAlfred(AlfredTheButler alfred) {
        this.alfred = alfred;
        dialogContainer.getChildren().add(
                DialogBox.getAlfredDialog(alfred.getGreeting(), alfredImage));
    }

    /**
     * Puts the keyboard focus in the text field, so that the user can type a
     * command as soon as the window opens.
     */
    public void focusInput() {
        userInput.requestFocus();
    }

    /**
     * Adds the line the user sent and Alfred's answer to it to the end of the
     * conversation, then empties the text field ready for the next line.
     *
     * <p>Both boxes are added at once, so that a line and its answer arrive
     * together as they do in a conversation. A blank line is not sent at all:
     * in a window it is almost always a stray press of Enter, and answering it
     * with a refusal would only add noise.
     */
    @FXML
    private void handleUserInput() {
        String userText = userInput.getText();
        if (userText.isBlank()) {
            return;
        }

        String alfredText = alfred.getResponse(userText);
        DialogBox alfredDialog = alfred.isLastResponseError()
                ? DialogBox.getErrorDialog(alfredText, alfredImage)
                : DialogBox.getAlfredDialog(alfredText, alfredImage, alfred.getCommandType());

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userText.trim()),
                alfredDialog);
        userInput.clear();
    }
}
