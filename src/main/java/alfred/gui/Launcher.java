package alfred.gui;

import javafx.application.Application;

/**
 * The entry point of the graphical version of Alfred.
 *
 * <p>A class that extends {@link Application} cannot be the main class of a JAR
 * that bundles JavaFX inside it: the runtime refuses to start when its own
 * classes were loaded from the classpath rather than as modules. This launcher
 * does not extend {@code Application}, so it is not subject to that check.
 */
public class Launcher {
    /**
     * Starts the graphical application.
     *
     * @param args command line arguments, handed on to JavaFX unread
     */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
