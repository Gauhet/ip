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

    /** Maximum number of tasks that can be stored, as fixed by the requirements. */
    private static final int MAX_TASKS = 100;

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

    /**
     * Greets the user, then stores each command as a task and confirms it,
     * listing the stored tasks on {@code list}, until {@code bye} is entered.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        greet();
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                break;
            }
            if (command.equals("list")) {
                printList(tasks, taskCount);
                continue;
            }
            tasks[taskCount++] = new Task(command);
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

    /**
     * Prints the stored tasks inside a divider block, numbered from 1,
     * followed by a blank line.
     *
     * @param tasks the array holding the tasks, which may have unused trailing slots
     * @param count how many slots of {@code tasks} are filled, counting from index 0
     */
    private static void printList(Task[] tasks, int count) {
        System.out.println(DIVIDER);
        for (int i = 0; i < count; i++) {
            System.out.println(INDENT + (i + 1) + "." + tasks[i]);
        }
        System.out.println(DIVIDER);
        System.out.println();
    }
}
