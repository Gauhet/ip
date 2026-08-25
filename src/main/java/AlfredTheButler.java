import java.util.Scanner;

/**
 * Alfred the Butler: a personal chatbot that echoes each command
 * it is given until the user types {@code bye}.
 */
public class AlfredTheButler {
    /** Horizontal rule that opens and closes every message block. */
    private static final String DIVIDER = "    " + "_".repeat(60);

    /** Indent for message text, one space deeper than the divider. */
    private static final String INDENT = "     ";

    /** Name the chatbot introduces itself by. */
    private static final String NAME = "AlfredTheButler";

    /** ASCII-art logo shown once at startup. */
    private static final String BANNER =
            "            _     _      _____  ____   _____  ____\n"
            + "           / \\   | |    |  ___||  _ \\ | ____||  _ \\\n"
            + "          / _ \\  | |    | |_   | |_) ||  _|  | | | |\n"
            + "         / ___ \\ | |___ |  _|  |  _ < | |___ | |_| |\n"
            + "        /_/   \\_\\|_____||_|    |_| \\_\\|_____||____/\n"
            + "                    P E N N Y W O R T H\n"
            + "\n"
            + "      Butler to the Wayne family  --  At your service";

    /** Greets the user, then echoes each command until {@code bye} is entered. */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] list = new String[100];
        int i = 0;

        greet();
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                break;
            }
            if (command.equals("list")) {
                printList(list, i);
                continue;
            }
            list[i++] = command;
            reply("added: " + command);
        }
        reply("Bye. Hope to see you again soon!");
    }

    /** Prints the banner and the welcome message. */
    private static void greet() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println(INDENT + "Hello! I'm " + NAME);
        System.out.println(INDENT + "What can I do for you?");
        System.out.println(DIVIDER);
        System.out.println();
    }

    /**
     * Prints a message inside a divider block, followed by a blank line.
     *
     * @param message the text to display
     */
    private static void reply(String message) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + message);
        System.out.println(DIVIDER);
        System.out.println();
    }

    private static void printList(String[] list, int len) {
        System.out.println(DIVIDER);
        for (int i = 0; i < len; i++) {
            System.out.println(INDENT + (i+1) + ". " + list[i]);
        }
        System.out.println(DIVIDER);
    }
}
