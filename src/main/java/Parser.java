import java.time.LocalDate;

/**
 * Makes sense of what the user typed: which command a line names, and what the
 * rest of that line means to it.
 *
 * <p>Everything here turns text into something the program can act on — a
 * {@link Command}, a {@link Task}, a list index, a date — and refuses text it
 * cannot, with a message written for the person who typed it. Nothing here
 * prints, stores, or changes anything, which is what lets the command loop be
 * read as a list of decisions rather than as string handling.
 *
 * <p>The methods are static because reading a line needs nothing remembered
 * between one line and the next. An object would be the answer if the parser
 * ever had to hold state, such as a command that spans several lines, and
 * turning these into instance methods would be the change to make then.
 */
public class Parser {
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
     * One line of input, read far enough to say which command it names and what
     * is left for that command to read.
     *
     * <p>The two travel together because they come from one split of one line,
     * and returning them separately would mean splitting it twice and risking
     * the two halves being read from different lines.
     *
     * @param command the command the first word names
     * @param arguments everything after the first word, trimmed, or an empty
     *        string if the line was only a keyword
     */
    public record ParsedCommand(Command command, String arguments) { }

    /**
     * Reads a line of input as a command and its arguments.
     *
     * @param line what the user typed, already trimmed
     * @return the command named by the line, with the rest of the line
     * @throws AlfredException if the line is empty, or names no known command
     */
    public static ParsedCommand parse(String line) throws AlfredException {
        // Worth its own message: saying the command was not recognized
        // would be misleading when none was typed.
        if (line.isEmpty()) {
            throw new AlfredException("You'll have to give me something to work with, sir.");
        }
        // Everything up to the first space names the command; the rest
        // is that command's own input, which only it knows how to read.
        String[] parts = line.split(" ", 2);
        String arguments = parts.length > 1 ? parts[1].trim() : "";
        return new ParsedCommand(Command.fromKeyword(parts[0]), arguments);
    }

    /**
     * Builds a todo from the description part of a {@code todo} command.
     *
     * @param arguments everything the user typed after the keyword
     * @return the todo the arguments describe
     * @throws AlfredException if no description was given
     */
    public static ToDo parseToDo(String arguments) throws AlfredException {
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
    public static Deadline parseDeadline(String arguments) throws AlfredException {
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
    public static Event parseEvent(String arguments) throws AlfredException {
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
     * Reads the day an {@code on} command is asking about.
     *
     * <p>A missing date is caught here rather than left to the date reader,
     * which would quote back an empty string as the thing it did not recognize
     * and say nothing about what was actually wrong.
     *
     * @param arguments everything the user typed after {@code on}
     * @return the day being asked about
     * @throws AlfredException if no date was given, or it cannot be read
     */
    public static LocalDate parseOnDate(String arguments) throws AlfredException {
        if (arguments.isEmpty()) {
            throw new AlfredException("The on command needs a date, sir.");
        }
        return Dates.parse(arguments);
    }

    /**
     * Converts a task number typed by the user into a list index, since the
     * user numbers tasks from 1 but the list is indexed from 0.
     *
     * <p>Only whether the text is a number is settled here. Whether it names a
     * task is {@link TaskList}'s to answer, because that depends on how many
     * tasks there are, and asking the parser to know that would mean handing it
     * the list to read a piece of text.
     *
     * @param arguments everything typed after {@code mark}, {@code unmark}
     *        or {@code delete}
     * @return the index the number points at, which may be outside the list
     * @throws AlfredException if the arguments are not a number
     */
    public static int parseTaskIndex(String arguments) throws AlfredException {
        try {
            // Also covers a missing number, since parsing an empty string
            // fails the same way.
            return Integer.parseInt(arguments) - 1;
        } catch (NumberFormatException e) {
            throw new AlfredException("That is not a task number, sir.");
        }
    }
}
