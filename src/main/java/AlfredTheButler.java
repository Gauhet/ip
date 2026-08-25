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

    /** Prefix of the command that adds a deadline, including its trailing space. */
    private static final String DEADLINE_COMMAND = "deadline ";

    /** Keyword that separates a deadline's description from its due time. */
    private static final String BY_SEPARATOR = " /by ";

    /** Prefix of the command that adds an event, including its trailing space. */
    private static final String EVENT_COMMAND = "event ";

    /** Keyword that separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = " /from ";

    /** Keyword that separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = " /to ";

    /**
     * Greets the user, then handles one command per line until {@code bye}:
     * {@code list}, {@code mark <number>} and {@code unmark <number>}, or
     * otherwise stores the line as a task and confirms it.
     *
     * <p>Commands run inside a {@code try} so that a mistake in what was typed
     * becomes an ordinary reply and the loop carries on. Catching in one place
     * lets each method throw its own message without knowing how it is printed.
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
            try {
                // Worth its own message: saying the command was not recognised
                // would be misleading when none was typed.
                if (command.isBlank()) {
                    throw new AlfredException("You'll have to give me something to work with, sir.");
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
                // Anything that is not a recognised command becomes a todo,
                // so a deadline or an event has to be spotted before that fallback.
                Task added;
                if (command.startsWith(DEADLINE_COMMAND)) {
                    added = parseDeadline(command);
                } else if (command.startsWith(EVENT_COMMAND)) {
                    added = parseEvent(command);
                } else {
                    added = new ToDo(command);
                }
                tasks[taskCount++] = added;
                replyAdded(added, taskCount);
            } catch (AlfredException e) {
                reply(e.getMessage());
            }
        }
        reply("Bye. Hope to see you again soon!");
    }

    /**
     * Builds a deadline from a {@code deadline <description> /by <time>} line.
     *
     * @param command the whole line the user typed
     * @return the deadline the line describes
     */
    private static Deadline parseDeadline(String command) {
        String details = command.substring(DEADLINE_COMMAND.length());
        int separator = details.indexOf(BY_SEPARATOR);
        String description = details.substring(0, separator);
        String by = details.substring(separator + BY_SEPARATOR.length());
        return new Deadline(description, by);
    }

    /**
     * Builds an event from an
     * {@code event <description> /from <start> /to <end>} line.
     *
     * @param command the whole line the user typed
     * @return the event the line describes
     */
    private static Event parseEvent(String command) {
        String details = command.substring(EVENT_COMMAND.length());
        int fromSeparator = details.indexOf(FROM_SEPARATOR);
        // Looked for after /from, so that a /to inside the description
        // is not mistaken for the one that starts the end time.
        int toSeparator = details.indexOf(TO_SEPARATOR, fromSeparator + FROM_SEPARATOR.length());
        String description = details.substring(0, fromSeparator);
        String from = details.substring(fromSeparator + FROM_SEPARATOR.length(), toSeparator);
        String to = details.substring(toSeparator + TO_SEPARATOR.length());
        return new Event(description, from, to);
    }

    /**
     * Confirms that a task has been stored, showing the task itself so the
     * user can see how it was understood.
     *
     * @param task the task that was just added
     * @param count how many tasks are stored now that it has been added
     */
    private static void replyAdded(Task task, int count) {
        reply("Got it. I've added this task:",
                SUB_INDENT + task,
                "Now you have " + count + " tasks in the list.");
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
