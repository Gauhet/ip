package alfred.gui;

import alfred.AlfredTheButler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * Controls what the main window does; {@code view/MainWindow.fxml} describes
 * what it looks like. The two are joined by name: each {@code fx:id} in the
 * file is set on the field of the same name here.
 */
public class MainWindow extends VBox {
    private static final double SCROLL_TO_BOTTOM = 1.0;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    private AlfredTheButler alfred;

    private final Image alfredImage =
            new Image(this.getClass().getResourceAsStream("/images/DaAlfred.png"));

    /**
     * Finishes setting up the window once the loader has filled in its parts:
     * scrolls to the end as the conversation grows, and grays out the send
     * button while there is nothing to send.
     */
    @FXML
    public void initialize() {
        dialogContainer.heightProperty()
                .addListener((observable) -> scrollPane.setVvalue(SCROLL_TO_BOTTOM));
        sendButton.disableProperty().bind(userInput.textProperty().isEmpty());
    }

    /**
     * Gives the window the chatbot it asks for an answer to each line, and
     * shows the greeting. This cannot happen in {@link #initialize()}, which
     * runs while there is still no chatbot to ask.
     *
     * @param alfred the chatbot to ask.
     */
    public void setAlfred(AlfredTheButler alfred) {
        this.alfred = alfred;
        dialogContainer.getChildren().add(
                DialogBox.getAlfredDialog(alfred.getGreeting(), alfredImage));
    }

    /** Puts the keyboard focus in the text field. */
    public void focusInput() {
        userInput.requestFocus();
    }

    /**
     * Adds the line the user sent and Alfred's answer to the conversation, then
     * empties the text field. A blank line is not sent at all.
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
