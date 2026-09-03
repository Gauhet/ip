package alfred.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * One message in the conversation: what was said, and the avatar of whoever
 * said it, side by side.
 *
 * <p>What a dialog box looks like is described in {@code view/DialogBox.fxml}.
 * This class is left with which words and which picture a box is given, and
 * which way round it faces.
 *
 * <p>The user takes the left of the conversation and Alfred the right. The FXML
 * describes a box facing right, and the ones on the left are flipped, so the
 * two sides cannot drift apart.
 */
public class DialogBox extends HBox {
    /** The message itself. Filled in by the FXML loader. */
    @FXML
    private Label dialog;

    /** The avatar of whoever said it. Filled in by the FXML loader. */
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box showing a message beside the speaker's avatar, on the
     * right of the conversation.
     *
     * <p>The box is both the root of the layout it loads and the controller for
     * it, so a caller need not know there is an FXML file behind it.
     *
     * @param message what was said.
     * @param avatar the picture of whoever said it.
     */
    private DialogBox(String message, Image avatar) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            // The layout ships with the program, so a failure to read it is a
            // fault in the program and not something the user could act on.
            throw new IllegalStateException("Cannot read the dialog box layout", e);
        }

        dialog.setText(message);
        displayPicture.setImage(avatar);
    }

    /**
     * Moves this dialog box to the left of the conversation, avatar first.
     *
     * <p>The bubble is restyled as well, because the corner it leaves square is
     * the one nearest its owner, and that corner moves with the box.
     */
    private void flip() {
        ObservableList<Node> reversedChildren = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(reversedChildren);
        this.getChildren().setAll(reversedChildren);
        this.setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }

    /**
     * Returns a dialog box for something the user said, on the left of the
     * conversation.
     *
     * @param message what the user said.
     * @param avatar the user's avatar.
     * @return the box to add to the conversation.
     */
    public static DialogBox getUserDialog(String message, Image avatar) {
        DialogBox box = new DialogBox(message, avatar);
        box.flip();
        return box;
    }

    /**
     * Colors the bubble by the kind of command the reply answers.
     *
     * <p>Marking and unmarking share a color, being two halves of the same act.
     * A reply with no command behind it is left alone.
     *
     * @param commandType the name of the command's class, or null if the line
     *     named no command.
     */
    private void changeDialogStyle(String commandType) {
        if (commandType == null) {
            return;
        }

        switch (commandType) {
            case "AddCommand":
                dialog.getStyleClass().add("add-label");
                break;
            case "MarkCommand":
                dialog.getStyleClass().add("marked-label");
                break;
            case "UnmarkCommand":
                dialog.getStyleClass().add("marked-label");
                break;
            case "DeleteCommand":
                dialog.getStyleClass().add("delete-label");
                break;
            default:
                // Do nothing
        }
    }

    /**
     * Returns a dialog box for something Alfred said, on the right of the
     * conversation.
     *
     * @param message what Alfred said.
     * @param avatar Alfred's avatar.
     * @return the box to add to the conversation.
     */
    public static DialogBox getAlfredDialog(String message, Image avatar) {
        return new DialogBox(message, avatar);
    }

    /**
     * Returns a dialog box for Alfred's answer to a command, colored by the kind
     * of command it answers.
     *
     * @param message what Alfred said.
     * @param avatar Alfred's avatar.
     * @param commandType the name of the class of the command answered.
     * @return the box to add to the conversation.
     */
    public static DialogBox getAlfredDialog(String message, Image avatar, String commandType) {
        DialogBox box = new DialogBox(message, avatar);
        box.changeDialogStyle(commandType);
        return box;
    }
}
