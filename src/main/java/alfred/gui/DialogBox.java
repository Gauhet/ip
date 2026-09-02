package alfred.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * One message in the conversation: what was said, and the avatar of whoever
 * said it, side by side.
 *
 * <p>A message and an avatar always appear together, so the pair is a control
 * of its own rather than two controls added to the window one at a time. That
 * keeps how a message looks in one place, and lets the window add a message by
 * creating a single node.
 *
 * <p>The two speakers take opposite sides of the conversation, so that who said
 * what can be seen at a glance: the user on the left, Alfred on the right, each
 * with their avatar on the outside. Which side a box takes is the only
 * difference between the two, so it is settled here, by the method that creates
 * the box, rather than by the window that holds it.
 */
public class DialogBox extends HBox {
    /** The message itself. */
    private final Label text;

    /** The avatar of whoever said it. */
    private final ImageView displayPicture;

    /**
     * Creates a dialog box showing a message beside the speaker's avatar.
     *
     * @param message What was said.
     * @param avatar The picture of whoever said it.
     * @param isFromUser Whether the user said it, which puts the box on the
     *     left of the conversation with the avatar on the outside. Anything
     *     Alfred said goes on the right, mirrored.
     */
    private DialogBox(String message, Image avatar, boolean isFromUser) {
        text = new Label(message);
        displayPicture = new ImageView(avatar);

        // A long message wraps onto further lines instead of running off the
        // side of the window, and the avatar is scaled to a fixed square so
        // that every dialog box lines up with the next.
        text.setWrapText(true);
        displayPicture.setFitWidth(100.0);
        displayPicture.setFitHeight(100.0);

        if (isFromUser) {
            this.setAlignment(Pos.TOP_LEFT);
            this.getChildren().addAll(displayPicture, text);
        } else {
            this.setAlignment(Pos.TOP_RIGHT);
            this.getChildren().addAll(text, displayPicture);
        }
    }

    /**
     * Returns a dialog box for something the user said, on the left of the
     * conversation.
     *
     * @param message What the user said.
     * @param avatar The user's avatar.
     */
    public static DialogBox createUserDialog(String message, Image avatar) {
        return new DialogBox(message, avatar, true);
    }

    /**
     * Returns a dialog box for something Alfred said, on the right of the
     * conversation.
     *
     * @param message What Alfred said.
     * @param avatar Alfred's avatar.
     */
    public static DialogBox createAlfredDialog(String message, Image avatar) {
        return new DialogBox(message, avatar, false);
    }
}
