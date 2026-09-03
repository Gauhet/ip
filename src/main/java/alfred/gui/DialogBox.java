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
 * <p>A message and an avatar always appear together, so the pair is a control
 * of its own rather than two controls added to the window one at a time. That
 * keeps how a message looks in one place, and lets the window add a message by
 * creating a single node.
 *
 * <p>What a dialog box looks like is described in {@code view/DialogBox.fxml}
 * rather than built here, so that the arrangement can be read, and changed, in
 * one file of its own. This class is left with the part that cannot be
 * written down in advance: which words and which picture a box is given, and
 * which way round it faces.
 *
 * <p>The two speakers take opposite sides of the conversation, so that who said
 * what can be seen at a glance: the user on the left, Alfred on the right, each
 * with their avatar on the outside. The FXML describes a box facing right, and
 * the ones that belong on the left are flipped, so that the two sides cannot
 * drift apart. Which side a box takes is settled by the method that creates it,
 * so the window asks for the user's box or Alfred's and does not concern itself
 * with where either goes.
 */
public class DialogBox extends HBox {
    /** The message itself. Filled in by the FXML loader. */
    @FXML
    private Label dialog;

    /** The avatar of whoever said it. Filled in by the FXML loader. */
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box showing a message beside the speaker's avatar, on
     * the right of the conversation with the avatar on the outside.
     *
     * <p>The box loads its own layout, and is both the root of what it loads
     * and the controller for it. That is what lets a dialog box be created
     * like any other object, rather than by a caller that has to know there is
     * an FXML file behind it.
     *
     * @param message What was said.
     * @param avatar The picture of whoever said it.
     */
    private DialogBox(String message, Image avatar) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            // The layout ships with the program rather than coming from the
            // user, so a failure to read it is a fault in the program and
            // there is nothing the user could do about it. Carrying on would
            // only fail again on the next line, with the fields still empty.
            throw new IllegalStateException("Cannot read the dialog box layout", e);
        }

        dialog.setText(message);
        displayPicture.setImage(avatar);
    }

    /**
     * Moves this dialog box to the left of the conversation, avatar first.
     *
     * <p>The children are reversed rather than laid out the other way round in
     * a second FXML file, so that everything the two sides share is described
     * once and only the direction differs.
     *
     * <p>The bubble is restyled as well, because the corner it leaves square is
     * the one nearest its owner, and that corner moves with the box.
     */
    private void flip() {
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(tmp);
        this.getChildren().setAll(tmp);
        this.setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
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
