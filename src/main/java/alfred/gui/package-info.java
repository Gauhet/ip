/**
 * The graphical version of Alfred, built with JavaFX.
 *
 * <p>{@link alfred.gui.Launcher} starts the application,
 * {@link alfred.gui.Main} puts the window on the screen,
 * {@link alfred.gui.MainWindow} is what happens in it, and
 * {@link alfred.gui.DialogBox} is one message within it. What the window and a
 * message look like is described in the FXML files under
 * {@code src/main/resources/view}, each paired with the class of its own name.
 *
 * <p>The window asks {@link alfred.AlfredTheButler} for an answer to every line
 * the user sends. The commands are the same ones the console version runs, and
 * they save to the same file.
 */
package alfred.gui;
