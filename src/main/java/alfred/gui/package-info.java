/**
 * The graphical version of Alfred, built with JavaFX.
 *
 * <p>{@link alfred.gui.Launcher} starts the application,
 * {@link alfred.gui.Main} puts the window on the screen,
 * {@link alfred.gui.MainWindow} is what happens in it, and
 * {@link alfred.gui.DialogBox} is one message within it.
 *
 * <p>What the window and a message look like is described in the FXML files
 * under {@code src/main/resources/view}, and the classes here hold only what
 * those files cannot say. Each class is paired with the file of its own name.
 *
 * <p>The window asks {@link alfred.AlfredTheButler} for an answer to every
 * line the user sends, but the answer is a placeholder that echoes the line
 * back. The text version of the program is still the only one that carries a
 * command out.
 */
package alfred.gui;
