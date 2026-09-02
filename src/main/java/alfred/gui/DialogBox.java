package alfred.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * <p>A message and an avatar always appear together, so the pair is a control
 * of its own rather than two controls added to the window one at a time. That
 * keeps how a message looks in one place, and lets the window add a message by
 * creating a single node.
 *
 * <p>The two speakers take opposite sides of the conversation, so that who said
 * what can be seen at a glance: the user on the left, Alfred on the right, each
 * with their avatar on the outside. A box is built facing right, and the ones
 * that belong on the left are flipped, so the two sides cannot drift apart —
 * everything but the direction is decided once, in one place.
 *
 * <p>Which side a box takes is settled by the method that creates it, so the
 * window asks for the user's box or Alfred's and does not concern itself with
 * where either goes.
 */
public class DialogBox extends HBox {
    /** The message itself. */
    private final Label text;

    /** The avatar of whoever said it. */
    private final ImageView displayPicture;

    /**
     * Creates a dialog box showing a message beside the speaker's avatar, on
     * the right of the conversation with the avatar on the outside.
     *
     * @param message What was said.
     * @param avatar The picture of whoever said it.
     */
    private DialogBox(String message, Image avatar) {
        text = new Label(message);
        displayPicture = new ImageView(avatar);

        // A long message wraps onto further lines instead of running off the
        // side of the window, and the avatar is scaled to a fixed square so
        // that every dialog box lines up with the next.
        text.setWrapText(true);
        displayPicture.setFitWidth(100.0);
        displayPicture.setFitHeight(100.0);
        this.setAlignment(Pos.TOP_RIGHT);

        this.getChildren().addAll(text, displayPicture);
    }

    /**
     * Moves this dialog box to the left of the conversation, avatar first.
     *
     * <p>The children are reversed rather than added in the other order,
     * because that keeps the flip to one line of the box's life: everything a
     * box is made of is decided by the constructor, and this changes only
     * which way it faces.
     */
    private void flip() {
        this.setAlignment(Pos.TOP_LEFT);
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        FXCollections.reverse(tmp);
        this.getChildren().setAll(tmp);
    }

    /**
     * Returns a dialog box for something the user said, on the left of the
     * conversation.
     *
     * @param message What the user said.
     * @param avatar The user's avatar.
     */
    public static DialogBox getUserDialog(String message, Image avatar) {
        DialogBox box = new DialogBox(message, avatar);
        box.flip();
        return box;
    }

    /**
     * Returns a dialog box for something Alfred said, on the right of the
     * conversation.
     *
     * @param message What Alfred said.
     * @param avatar Alfred's avatar.
     */
    public static DialogBox getAlfredDialog(String message, Image avatar) {
        return new DialogBox(message, avatar);
    }
}
