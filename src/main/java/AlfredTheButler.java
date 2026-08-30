import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Alfred the Butler: a personal chatbot that keeps a list of tasks, taking one
 * command per line until the user says {@code bye} or the input runs out. The
 * list is saved to disk after every change and read back at startup, so it
 * outlives a single run.
 *
 * <p>This class holds the command loop and nothing else that can be given a
 * home of its own: what the user sees and types is {@link Ui}'s, and what is
 * written to and read from disk is {@link Storage}'s.
 */
public class AlfredTheButler {
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
     * the end of the input: {@code list}, {@code on <date>},
     * {@code mark <number>}, {@code unmark <number>}, {@code delete <number>},
     * and the three that add a task, {@code todo}, {@code deadline}, and
     * {@code event}. Any other word is refused rather than guessed at.
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
        Ui ui = new Ui();
        List<Task> tasks = new ArrayList<>();
        Storage storage = new Storage();
        boolean isRunning = true;

        ui.showWelcome();
        // Loaded in its own try, because this runs before the loop below and so
        // cannot rely on the loop's catch. A file that cannot be read leaves
        // the empty list above in place rather than stopping the program.
        try {
            Storage.LoadResult loaded = storage.load();
            tasks = loaded.tasks();
            // Said only when there is something to say. On a first run there is
            // no file yet, and announcing that nothing came back would be noise.
            if (!tasks.isEmpty()) {
                ui.showLoaded(tasks.size());
            }
            // Warned about separately, and even when nothing else was restored,
            // because the damaged lines are dropped from the file as soon as the
            // list next changes.
            if (loaded.skippedLines() > 0) {
                ui.showSkippedLines(loaded.skippedLines());
            }
        } catch (AlfredException e) {
            ui.showError(e.getMessage());
        }
        while (isRunning) {
            // End of input is treated as `bye`, so that a piped or redirected
            // session that simply runs out of lines finishes the same way a
            // typed one does instead of failing to read a line that is not there.
            if (!ui.hasNextCommand()) {
                break;
            }
            String line = ui.readCommand();
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
                case LIST -> ui.showList(tasks);
                case UNMARK -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    tasks.get(index).unmarkDone();
                    isListChanged = true;
                    ui.showUnmarked(tasks.get(index));
                }
                case MARK -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    tasks.get(index).markDone();
                    isListChanged = true;
                    ui.showMarked(tasks.get(index));
                }
                case DELETE -> {
                    int index = parseTaskIndex(arguments, tasks.size());
                    Task removed = tasks.remove(index);
                    isListChanged = true;
                    ui.showRemoved(removed, tasks.size());
                }
                case TODO -> added = parseToDo(arguments);
                case DEADLINE -> added = parseDeadline(arguments);
                case EVENT -> added = parseEvent(arguments);
                case ON -> {
                    // Checked here rather than left to the date reader, which
                    // would quote back an empty string as the thing it did not
                    // recognize and say nothing about what was actually wrong.
                    if (arguments.isEmpty()) {
                        throw new AlfredException("The on command needs a date, sir.");
                    }
                    ui.showTasksOn(tasks, Dates.parse(arguments));
                }
                }

                // Storing and confirming is the same for every kind of task, so
                // it is done once here rather than repeated in each arm above.
                if (added != null) {
                    tasks.add(added);
                    isListChanged = true;
                    ui.showAdded(added, tasks.size());
                }

                // Saved after the reply, so that a command the user has been
                // told succeeded is on disk before the next one is read.
                if (isListChanged) {
                    storage.save(tasks);
                }
            } catch (AlfredException e) {
                ui.showError(e.getMessage());
            } catch (RuntimeException e) {
                // A safety net, not a substitute for handling: everything the
                // user can get wrong is refused above with a message of its own.
                // This one catches the mistakes in this program, so that a bug
                // in one command costs that command rather than the session and
                // the tasks typed since the last save.
                ui.showInternalError(e);
            }
        }
        ui.showFarewell();
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
     * cannot be read is refused by {@link Dates} with a message of its
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
        return new Deadline(description, Dates.parse(by));
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
        LocalDate start = Dates.parse(from);
        LocalDate end = Dates.parse(to);
        // Checked once both are real dates, since two dates that cannot be read
        // cannot be compared. An event of a single day is allowed, so only a
        // strictly earlier end is refused.
        if (end.isBefore(start)) {
            throw new AlfredException("An event cannot end before it starts, sir.");
        }
        return new Event(description, start, end);
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
}
