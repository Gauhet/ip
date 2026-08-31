package alfred;

import java.time.LocalDate;

import alfred.command.AddCommand;
import alfred.command.Command;
import alfred.command.DeleteCommand;
import alfred.command.ExitCommand;
import alfred.command.FindCommand;
import alfred.command.ListCommand;
import alfred.command.MarkCommand;
import alfred.command.OnCommand;
import alfred.command.UnmarkCommand;
import alfred.task.Deadline;
import alfred.task.Event;
import alfred.task.Task;
import alfred.task.TaskList;
import alfred.task.ToDo;

/**
 * Makes sense of what the user typed: which command a line names, and what the
 * rest of that line means to it.
 *
 * <p>Everything here turns text into something the program can act on — a
 * {@link Command}, a {@link Task}, a list index, a date — and refuses text it
 * cannot, with a message written for the person who typed it. Nothing here
 * prints, stores, or changes anything: a command is built, not carried out,
 * which is what lets a line be refused before anything has happened to the
 * task list.
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
     * Reads a line of input as the command it asks for, ready to be carried
     * out.
     *
     * <p>This is where a keyword becomes an object. Each arm builds the command
     * that keyword names, reading the rest of the line the way that command
     * needs it read, so everything a command requires is settled here and it
     * can be carried out without the line it came from.
     *
     * @param line what the user typed, already trimmed
     * @return the command the line asks for
     * @throws AlfredException if the line is empty, names no known command, or
     *         is missing something the command it names needs
     */
    static Command parse(String line) throws AlfredException {
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

        return switch (keyword) {
        case "bye" -> new ExitCommand();
        case "list" -> new ListCommand();
        case "todo" -> new AddCommand(parseToDo(arguments));
        case "deadline" -> new AddCommand(parseDeadline(arguments));
        case "event" -> new AddCommand(parseEvent(arguments));
        case "mark" -> new MarkCommand(parseTaskIndex(arguments));
        case "unmark" -> new UnmarkCommand(parseTaskIndex(arguments));
        case "delete" -> new DeleteCommand(parseTaskIndex(arguments));
        case "on" -> new OnCommand(parseOnDate(arguments));
        case "find" -> new FindCommand(parseKeyword(arguments));
        // Only the keyword is quoted back. Repeating the whole line would bury
        // the one word that was not understood.
        default -> throw new AlfredException("I'm afraid I don't know '" + keyword + "', sir.");
        };
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
    private static LocalDate parseOnDate(String arguments) throws AlfredException {
        if (arguments.isEmpty()) {
            throw new AlfredException("The on command needs a date, sir.");
        }
        return Dates.parse(arguments);
    }

    /**
     * Reads the keyword a {@code find} command is searching for.
     *
     * <p>Everything after the keyword is taken as one piece of text rather than
     * as a list of words, so that {@code find read book} searches for the phrase
     * {@code read book}. Treating the words as alternatives would need a rule
     * for whether a task has to contain all of them or any of them, which is a
     * decision the user has no way to state on the command line.
     *
     * <p>An empty search is refused rather than answered, because every
     * description contains the empty string, so the reply would be the whole
     * list — an answer that looks like a working search and is not one.
     *
     * @param arguments everything the user typed after {@code find}
     * @return the text to search descriptions for
     * @throws AlfredException if no keyword was given
     */
    private static String parseKeyword(String arguments) throws AlfredException {
        if (arguments.isEmpty()) {
            throw new AlfredException("The find command needs a keyword, sir.");
        }
        return arguments;
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
    private static int parseTaskIndex(String arguments) throws AlfredException {
        try {
            // Also covers a missing number, since parsing an empty string
            // fails the same way.
            return Integer.parseInt(arguments) - 1;
        } catch (NumberFormatException e) {
            throw new AlfredException("That is not a task number, sir.");
        }
    }
}
