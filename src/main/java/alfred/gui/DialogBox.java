package alfred.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * One message in the conversation.
 *
 * <p>What a dialog box looks like is described in {@code view/DialogBox.fxml}.
 * This class is left with which words a box is given, which side it sits on,
 * and how it is styled.
 *
 * <p>The two sides are not drawn alike, because the conversation is not between
 * two people. Alfred's replies sit on the left beside a small avatar and take
 * whatever width they need, since a task list can run to many lines. The
 * user's lines sit on the right in a compact bubble with no avatar at all:
 * there is only ever one user, so a picture would say nothing the side does
 * not already say.
 */
public class DialogBox extends HBox {
    /** The share of the window a user bubble may take, so that a long line still reads as the user's. */
    private static final double USER_BUBBLE_WIDTH_SHARE = 0.75;

    /** The message itself. Filled in by the FXML loader. */
    @FXML
    private Label dialog;

    /** The avatar of whoever said it. Filled in by the FXML loader. */
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box showing a message beside the speaker's avatar, on the
     * left of the conversation.
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
        cropAvatarToCircle();
    }

    /**
     * Returns a dialog box for something the user said, on the right of the
     * conversation, without an avatar.
     *
     * @param message what the user said.
     * @return the box to add to the conversation.
     */
    public static DialogBox getUserDialog(String message) {
        DialogBox box = new DialogBox(message, null);
        box.moveToUserSide();
        return box;
    }

    /**
     * Returns a dialog box for something Alfred said, on the left of the
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
     * Returns a dialog box for Alfred's answer to a command, tinted by the kind
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

    /**
     * Returns a dialog box for an error from Alfred, styled to stand out from
     * his ordinary replies so that a refused command is not read past.
     *
     * @param message what Alfred said.
     * @param avatar Alfred's avatar.
     * @return the box to add to the conversation.
     */
    public static DialogBox getErrorDialog(String message, Image avatar) {
        DialogBox box = new DialogBox(message, avatar);
        box.dialog.getStyleClass().add("error-label");
        return box;
    }

    /**
     * Clips the avatar to a circle, so that its square background does not show.
     *
     * <p>The circle is drawn around the picture's fitted size rather than the
     * image's own size, which is far larger.
     */
    private void cropAvatarToCircle() {
        double radius = displayPicture.getFitWidth() / 2;
        displayPicture.setClip(new Circle(radius, radius, radius));
    }

    /**
     * Moves this dialog box to the right of the conversation, drops the avatar,
     * and caps the bubble at a share of the window so that it keeps its shape.
     *
     * <p>The bubble is restyled as well, because the corner it leaves square is
     * the one nearest its owner, and that corner moves with the box.
     */
    private void moveToUserSide() {
        this.getChildren().remove(displayPicture);
        this.setAlignment(Pos.TOP_RIGHT);
        dialog.maxWidthProperty().bind(this.widthProperty().multiply(USER_BUBBLE_WIDTH_SHARE));
        dialog.getStyleClass().add("user-label");
    }

    /**
     * Tints the bubble by the kind of command the reply answers.
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
        case "AddCommand" -> dialog.getStyleClass().add("add-label");
        case "MarkCommand", "UnmarkCommand" -> dialog.getStyleClass().add("marked-label");
        case "DeleteCommand" -> dialog.getStyleClass().add("delete-label");
        case "PriorityCommand" -> dialog.getStyleClass().add("priority-label");
        default -> {
            // Any other command leaves the bubble its plain color.
        }
        }
    }
}
