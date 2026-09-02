/**
 * The graphical version of Alfred, built with JavaFX.
 *
 * <p>{@link alfred.gui.Launcher} starts the application,
 * {@link alfred.gui.Main} builds the window, and
 * {@link alfred.gui.DialogBox} is one message within it.
 *
 * <p>The window asks {@link alfred.AlfredTheButler} for an answer to every
 * line the user sends, but the answer is a placeholder that echoes the line
 * back. The text version of the program is still the only one that carries a
 * command out.
 */
package alfred.gui;
