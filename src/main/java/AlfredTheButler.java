import java.util.ArrayList;
import java.util.List;
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

    /** Command that ends the conversation. */
    private static final String BYE_COMMAND = "bye";

    /** Command that shows every stored task. */
    private static final String LIST_COMMAND = "list";

    /** Command that adds a task with no date attached. */
    private static final String TODO_COMMAND = "todo";

    /** Command that marks a task as done. */
    private static final String MARK_COMMAND = "mark";

    /** Command that marks a task as not done. */
    private static final String UNMARK_COMMAND = "unmark";

    /** Command that removes a task from the list. */
    private static final String DELETE_COMMAND = "delete";

    /** Command that adds a deadline. */
    private static final String DEADLINE_COMMAND = "deadline";

    /**
     * Keyword that separates a deadline's description from its due time. The
     * surrounding spaces are not part of it, so that a missing description can
     * be told apart from a missing keyword rather than looking the same.
     */
    private static final String BY_SEPARATOR = "/by";

    /** Command that adds an event. */
    private static final String EVENT_COMMAND = "event";

    /** Keyword that separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = "/from";

    /** Keyword that separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = "/to";

    /**
     * Greets the user, then handles one command per line until {@code bye}:
     * {@code list}, {@code mark <number>}, {@code unmark <number>},
     * {@code delete <number>}, and the three that add a task, {@code todo},
     * {@code deadline} and {@code event}. Any other word is refused rather
     * than guessed at.
     *
     * <p>Commands run inside a {@code try} so that a mistake in what was typed
     * becomes an ordinary reply and the loop carries on. Catching in one place
     * lets each method throw its own message without knowing how it is printed.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Task> tasks = new ArrayList<>();
        boolean isRunning = true;

        greet();
        while (isRunning) {
            // Trimmed so that a stray space around a command does not stop it
            // being recognized.
            String line = scanner.nextLine().trim();
            try {
                // Worth its own message: saying the command was not recognized
                // would be misleading when none was typed.
                if (line.isEmpty()) {
                    throw new AlfredException("You'll have to give me something to work with, sir.");
                }
                // Everything up to the first space names the command; the rest
                // is that command's own input, which only it knows how to read.
                String[] parts = line.split(" ", 2);
                String keyword = parts[0];
                String arguments = parts.length > 1 ? parts[1].trim() : "";

                // Left null by the commands that do not add anything, which is
                // what tells the code below there is nothing to store.
                Task added = null;

                // An arrow switch, so no arm can fall through into the next by
                // accident, and so `break` keeps its usual meaning in the loop.
                switch (keyword) {
                case BYE_COMMAND -> isRunning = false;
                case LIST_COMMAND -> printList(tasks);
                case UNMARK_COMMAND -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    tasks.get(index).unmarkDone();
                    reply("OK, I've marked this task as not done yet:", SUB_INDENT + tasks.get(index));
                }
                case MARK_COMMAND -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    tasks.get(index).markDone();
                    reply("Nice! I've marked this task as done:", SUB_INDENT + tasks.get(index));
                }
                case DELETE_COMMAND -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    Task removed = tasks.remove(index);
                    reply("Noted. I've removed this task:",
                            SUB_INDENT + removed,
                            "Now you have " + tasks.size() + " tasks in the list.");
                }
                case TODO_COMMAND -> added = parseToDo(arguments);
                case DEADLINE_COMMAND -> added = parseDeadline(arguments);
                case EVENT_COMMAND -> added = parseEvent(arguments);
                // Only the keyword is quoted back. Repeating the whole line
                // would bury the one word that was not understood.
                default -> throw new AlfredException(
                        "I'm afraid I don't know '" + keyword + "', sir.");
                }

                // Storing and confirming is the same for every kind of task, so
                // it is done once here rather than repeated in each arm above.
                if (added != null) {
                    tasks.add(added);
                    replyAdded(added, tasks.size());
                }
            } catch (AlfredException e) {
                reply(e.getMessage());
            }
        }
        reply("Bye. Hope to see you again soon!");
    }

    /**
     * Builds a todo from the description part of a {@code todo} command.
     *
     * @param arguments everything the user typed after the keyword
     * @return the todo the arguments describe
     * @throws AlfredException if no description was given
     */
    private static ToDo parseToDo(String arguments) throws AlfredException {
        if (arguments.isEmpty()) {
            throw new AlfredException("A todo needs a description, sir.");
        }
        return new ToDo(arguments);
    }

    /**
     * Builds a deadline from the {@code <description> /by <time>} part of a
     * {@code deadline} command.
     *
     * @param arguments everything the user typed after the keyword
     * @return the deadline the arguments describe
     * @throws AlfredException if the description or the due time is missing
     */
    private static Deadline parseDeadline(String arguments) throws AlfredException {
        String complaint = "A deadline needs a description and a /by time, sir.";
        int separator = arguments.indexOf(BY_SEPARATOR);
        if (separator == -1) {
            throw new AlfredException(complaint);
        }
        String description = arguments.substring(0, separator).trim();
        String by = arguments.substring(separator + BY_SEPARATOR.length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new AlfredException(complaint);
        }
        return new Deadline(description, by);
    }

    /**
     * Builds an event from the {@code <description> /from <start> /to <end>}
     * part of an {@code event} command.
     *
     * @param arguments everything the user typed after the keyword
     * @return the event the arguments describe
     * @throws AlfredException if the description, the start or the end is missing
     */
    private static Event parseEvent(String arguments) throws AlfredException {
        String complaint = "An event needs a description, a /from time and a /to time, sir.";
        int fromSeparator = arguments.indexOf(FROM_SEPARATOR);
        if (fromSeparator == -1) {
            throw new AlfredException(complaint);
        }
        // Looked for after /from, so that a /to inside the description
        // is not mistaken for the one that starts the end time.
        int toSeparator = arguments.indexOf(TO_SEPARATOR, fromSeparator + FROM_SEPARATOR.length());
        if (toSeparator == -1) {
            throw new AlfredException(complaint);
        }
        String description = arguments.substring(0, fromSeparator).trim();
        String from = arguments.substring(fromSeparator + FROM_SEPARATOR.length(), toSeparator).trim();
        String to = arguments.substring(toSeparator + TO_SEPARATOR.length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new AlfredException(complaint);
        }
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
     * Converts a task number typed by the user into a list index, since the
     * user numbers tasks from 1 but the list is indexed from 0.
     *
     * @param arguments everything typed after {@code mark}, {@code unmark}
     *        or {@code delete}
     * @param taskCount how many tasks are stored
     * @return the index of the task the user meant
     * @throws AlfredException if the arguments are not a number, or name a
     *         task that is not in the list
     */
    private static int parseTaskIndex(String arguments, int taskCount) throws AlfredException {
        int number;
        try {
            number = Integer.parseInt(arguments);
        } catch (NumberFormatException e) {
            // Also covers a missing number, since parsing an empty string
            // fails the same way.
            throw new AlfredException("That is not a task number, sir.");
        }
        int index = number - 1;
        // Checked here because the list would answer an out-of-range index
        // with an exception, where a mistyped number deserves a reply.
        if (index < 0 || index >= taskCount) {
            throw new AlfredException("There is no such task, sir.");
        }
        return index;
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
     * @param tasks the tasks to show, in the order they are stored
     */
    private static void printList(List<Task> tasks) {
        // One line for the heading, then one per task.
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        reply(lines);
    }
}
