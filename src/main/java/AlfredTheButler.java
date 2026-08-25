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

    /** Extra indent for a line that belongs under the one above it, such as a marked task. */
    private static final String SUB_INDENT = "  ";

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

    /** Prefix of the command that marks a task as done, including its trailing space. */
    private static final String MARK_COMMAND = "mark ";

    /** Prefix of the command that marks a task as not done, including its trailing space. */
    private static final String UNMARK_COMMAND = "unmark ";

    /**
     * Greets the user, then stores each command as a task and confirms it,
     * listing the stored tasks on {@code list} and changing whether one is
     * done on {@code mark <number>} and {@code unmark <number>},
     * until {@code bye} is entered.
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
            // Checked before MARK_COMMAND would be, but the prefixes cannot
            // overlap anyway: "unmark 2" does not start with "mark ".
            if (command.startsWith(UNMARK_COMMAND)) {
                int index = parseTaskIndex(command, UNMARK_COMMAND);
                tasks[index].unmarkDone();
                reply("OK, I've marked this task as not done yet:", SUB_INDENT + tasks[index]);
                continue;
            }
            if (command.startsWith(MARK_COMMAND)) {
                int index = parseTaskIndex(command, MARK_COMMAND);
                tasks[index].markDone();
                reply("Nice! I've marked this task as done:", SUB_INDENT + tasks[index]);
                continue;
            }
            tasks[taskCount++] = new Task(command);
            reply("added: " + command);
        }
        reply("Bye. Hope to see you again soon!");
    }

    /**
     * Reads the task number that follows a command prefix and converts it
     * to an array index, since the user numbers tasks from 1 but the array
     * is indexed from 0.
     *
     * @param command the whole line the user typed, such as {@code mark 2}
     * @param commandPrefix the prefix to skip, such as {@code "mark "}
     * @return the index of the task the user meant
     */
    private static int parseTaskIndex(String command, String commandPrefix) {
        return Integer.parseInt(command.substring(commandPrefix.length()).trim()) - 1;
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
     * Prints one or more lines inside a divider block, each indented,
     * followed by a blank line.
     *
     * @param lines the lines to display, in order
     */
    private static void reply(String... lines) {
        System.out.println(DIVIDER);
        for (String line : lines) {
            System.out.println(INDENT + line);
        }
        System.out.println(DIVIDER);
        System.out.println();
    }

    /**
     * Prints the stored tasks as a numbered list under a heading,
     * numbered from 1.
     *
     * @param tasks the array holding the tasks, which may have unused trailing slots
     * @param count how many slots of {@code tasks} are filled, counting from index 0
     */
    private static void printList(Task[] tasks, int count) {
        // One line for the heading, then one per task.
        String[] lines = new String[count + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < count; i++) {
            lines[i + 1] = (i + 1) + "." + tasks[i];
        }
        reply(lines);
    }
}
