import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Alfred the Butler: a personal chatbot that keeps a list of tasks, taking one
 * command per line until the user says {@code bye} or the input runs out. The
 * list is saved to disk after every change and read back at startup, so it
 * outlives a single run.
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

    /**
     * Keyword that separates a deadline's description from its due time. The
     * surrounding spaces are not part of it, so that a missing description can
     * be told apart from a missing keyword rather than looking the same.
     */
    private static final String BY_SEPARATOR = "/by";

    /** Keyword that separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = "/from";

    /** Keyword that separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = "/to";

    /**
     * Greets the user, then handles one command per line until {@code bye} or
     * the end of the input: {@code list}, {@code mark <number>},
     * {@code unmark <number>}, {@code delete <number>}, and the three that add
     * a task, {@code todo}, {@code deadline}, and {@code event}. Any other word
     * is refused rather than guessed at.
     *
     * <p>Commands run inside a {@code try} so that a mistake in what was typed
     * becomes an ordinary reply and the loop carries on. Catching in one place
     * lets each method throw its own message without knowing how it is printed.
     *
     * <p>The saved tasks are read back before the first command, and every
     * command that changes the list is followed by a save, so that the file on
     * disk always matches what the user has just been shown.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Task> tasks = new ArrayList<>();
        Storage storage = new Storage();
        boolean isRunning = true;

        greet();
        // Loaded in its own try, because this runs before the loop below and so
        // cannot rely on the loop's catch. A file that cannot be read leaves
        // the empty list above in place rather than stopping the program.
        try {
            Storage.LoadResult loaded = storage.load();
            tasks = loaded.tasks();
            // Said only when there is something to say. On a first run there is
            // no file yet, and announcing that nothing came back would be noise.
            if (!tasks.isEmpty()) {
                reply("I've brought back " + describeCount(tasks.size(), "task") + " from last time, sir.");
            }
            // Warned about separately, and even when nothing else was restored,
            // because the damaged lines are dropped from the file as soon as the
            // list next changes.
            if (loaded.skippedLines() > 0) {
                reply("I could not make sense of " + describeCount(loaded.skippedLines(), "line")
                                + " in your saved tasks, sir.",
                        "I have left them out, and they will be gone once the list changes.");
            }
        } catch (AlfredException e) {
            reply(e.getMessage());
        }
        while (isRunning) {
            // End of input is treated as `bye`, so that a piped or redirected
            // session that simply runs out of lines finishes the same way a
            // typed one does instead of failing to read a line that is not there.
            if (!scanner.hasNextLine()) {
                break;
            }
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

                // Set by every command that changes the list, so that the save
                // below happens once, in one place, rather than in each arm.
                boolean isListChanged = false;

                // An arrow switch, so no arm can fall through into the next by
                // accident, and so `break` keeps its usual meaning in the loop.
                // No default arm is needed: an unknown keyword has already been
                // refused by fromKeyword, so every value reaching here is listed.
                switch (Command.fromKeyword(keyword)) {
                case BYE -> isRunning = false;
                case LIST -> printList(tasks);
                case UNMARK -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    tasks.get(index).unmarkDone();
                    isListChanged = true;
                    reply("OK, I've marked this task as not done yet:", SUB_INDENT + tasks.get(index));
                }
                case MARK -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    tasks.get(index).markDone();
                    isListChanged = true;
                    reply("Nice! I've marked this task as done:", SUB_INDENT + tasks.get(index));
                }
                case DELETE -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    Task removed = tasks.remove(index);
                    isListChanged = true;
                    reply("Noted. I've removed this task:",
                            SUB_INDENT + removed,
                            "Now you have " + describeCount(tasks.size(), "task") + " in the list.");
                }
                case TODO -> added = parseToDo(arguments);
                case DEADLINE -> added = parseDeadline(arguments);
                case EVENT -> added = parseEvent(arguments);
                }

                // Storing and confirming is the same for every kind of task, so
                // it is done once here rather than repeated in each arm above.
                if (added != null) {
                    tasks.add(added);
                    isListChanged = true;
                    replyAdded(added, tasks.size());
                }

                // Saved after the reply, so that a command the user has been
                // told succeeded is on disk before the next one is read.
                if (isListChanged) {
                    storage.save(tasks);
                }
            } catch (AlfredException e) {
                reply(e.getMessage());
            } catch (RuntimeException e) {
                // A safety net, not a substitute for handling: everything the
                // user can get wrong is refused above with a message of its own.
                // This one catches the mistakes in this program, so that a bug
                // in one command costs that command rather than the session and
                // the tasks typed since the last save. The class and message are
                // shown because a fault that cannot be named cannot be reported.
                reply("Something went wrong on my end, sir: " + e,
                        "Your tasks are unharmed. Do carry on.");
            }
        }
        reply("Bye. Hope to see you again soon!");
    }

    /**
     * Returns a count with its noun, made plural unless there is exactly one of
     * them, for example {@code 1 task} or {@code 2 tasks}.
     *
     * @param count how many there are
     * @param noun the singular form of what is being counted
     * @return the count and the noun, ready to drop into a sentence
     */
    private static String describeCount(int count, String noun) {
        if (count == 1) {
            return count + " " + noun;
        }
        return count + " " + noun + "s";
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
     * Builds a deadline from the {@code <description> /by <date>} part of a
     * {@code deadline} command.
     *
     * <p>The date is read as a real date rather than kept as the words that
     * were typed, so that the program knows which day is meant. A date that
     * cannot be read is refused by {@link DateParser} with a message of its
     * own, which is why nothing here catches it.
     *
     * @param arguments everything the user typed after the keyword
     * @return the deadline the arguments describe
     * @throws AlfredException if the description or the due date is missing,
     *         or the due date cannot be read
     */
    private static Deadline parseDeadline(String arguments) throws AlfredException {
        String complaint = "A deadline needs a description and a /by date, sir.";
        int separator = arguments.indexOf(BY_SEPARATOR);
        if (separator == -1) {
            throw new AlfredException(complaint);
        }
        String description = arguments.substring(0, separator).trim();
        String by = arguments.substring(separator + BY_SEPARATOR.length()).trim();
        // Checked for presence before being read, so that a date left out
        // draws the complaint about the command rather than one about the
        // format of an empty string.
        if (description.isEmpty() || by.isEmpty()) {
            throw new AlfredException(complaint);
        }
        return new Deadline(description, DateParser.parse(by));
    }

    /**
     * Builds an event from the {@code <description> /from <start> /to <end>}
     * part of an {@code event} command.
     *
     * <p>The start and the end are read as real dates, on the same terms as the
     * due date of a deadline, and the pair is then checked for being in order.
     *
     * @param arguments everything the user typed after the keyword
     * @return the event the arguments describe
     * @throws AlfredException if the description, the start, or the end is
     *         missing, if either date cannot be read, or if the event ends
     *         before it starts
     */
    private static Event parseEvent(String arguments) throws AlfredException {
        String complaint = "An event needs a description, a /from date, and a /to date, sir.";
        int fromSeparator = arguments.indexOf(FROM_SEPARATOR);
        if (fromSeparator == -1) {
            throw new AlfredException(complaint);
        }
        // Looked for after /from, so that a /to inside the description
        // is not mistaken for the one that starts the end date.
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
        LocalDate start = DateParser.parse(from);
        LocalDate end = DateParser.parse(to);
        // Checked once both are real dates, since two dates that cannot be read
        // cannot be compared. An event of a single day is allowed, so only a
        // strictly earlier end is refused.
        if (end.isBefore(start)) {
            throw new AlfredException("An event cannot end before it starts, sir.");
        }
        return new Event(description, start, end);
    }

    /**
     * Confirms that a task has been stored, showing the task itself so the
     * user can see how it was understood.
     *
     * @param task the task that was just added
     * @param taskCount how many tasks are stored now that it has been added
     */
    private static void replyAdded(Task task, int taskCount) {
        reply("Got it. I've added this task:",
                SUB_INDENT + task,
                "Now you have " + describeCount(taskCount, "task") + " in the list.");
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
