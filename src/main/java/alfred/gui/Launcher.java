package alfred.gui;

import javafx.application.Application;

/**
 * The entry point of the graphical version of Alfred.
 *
 * <p>JavaFX could start {@link Main} directly, since that is the class holding
 * the window. It is launched from here instead because a class that extends
 * {@link Application} cannot be the main class of a JAR that bundles JavaFX
 * inside it: the JavaFX runtime refuses to start when its own classes were
 * loaded from the classpath rather than as modules. A launcher that does not
 * extend {@code Application} is not subject to that check, and starting the
 * application from within it works around the problem.
 */
public class Launcher {
    /**
     * Starts the graphical application.
     *
     * @param args Command line arguments, handed on to JavaFX unread.
     */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
