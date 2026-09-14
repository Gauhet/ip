package alfred;

import java.time.LocalDate;
import java.util.regex.Pattern;

import alfred.command.AddCommand;
import alfred.command.Command;
import alfred.command.DeleteCommand;
import alfred.command.ExitCommand;
import alfred.command.FindCommand;
import alfred.command.ListCommand;
import alfred.command.MarkCommand;
import alfred.command.OnCommand;
import alfred.command.PriorityCommand;
import alfred.command.UnmarkCommand;
import alfred.task.Deadline;
import alfred.task.Event;
import alfred.task.Priority;
import alfred.task.Task;
import alfred.task.TaskList;
import alfred.task.ToDo;

/**
 * Makes sense of what the user typed: which command a line names, and what the
 * rest of that line means to it.
 *
 * <p>Everything here turns text into a {@link Command}, a {@link Task}, an
 * index, or a date. Nothing here prints, stores, or changes anything, which is
 * what lets a line be refused before anything has happened to the
 * {@link TaskList}.
 */
public class Parser {
    /**
     * Separates a deadline's description from its due time. The surrounding
     * spaces are not part of it, so that a missing description can be told
     * apart from a missing keyword.
     */
    private static final String SEPARATOR_BY = "/by";

    /** Separates an event's description from its start time. */
    private static final String SEPARATOR_FROM = "/from";

    /** Separates an event's start time from its end time. */
    private static final String SEPARATOR_TO = "/to";

    /**
     * Matches a run of one or more whitespace characters, tabs included. Every
     * such run in a line is squeezed to one space before the line is read.
     */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Prevents this class from being instantiated. */
    private Parser() {
    }

    /**
     * Reads a line of input as the command it asks for, ready to be carried out.
     *
     * <p>Spacing is forgiven before anything is read: spaces and tabs around
     * the line are dropped, and any run of them inside it counts as one space.
     * So {@code mark  1} marks a task, and a description typed with two spaces
     * in it is stored with one.
     *
     * @param line what the user typed.
     * @return the command the line asks for.
     * @throws AlfredException if the line is empty, names no known command, or
     *         is missing something the command it names needs.
     */
    static Command parse(String line) throws AlfredException {
        String normalized = normalizeSpacing(line);

        // Worth its own message: saying the command was not recognized
        // would be misleading when none was typed.
        if (normalized.isEmpty()) {
            throw new AlfredException("You'll have to give me something to work with, sir.");
        }
        // Everything up to the first space names the command; the rest
        // is that command's own input. Normalizing has already seen to it
        // that the first space is a single one.
        String[] parts = normalized.split(" ", 2);
        String keyword = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        return switch (keyword) {
        case "bye" -> new ExitCommand();
        case "list" -> new ListCommand();
        case "todo" -> new AddCommand(parseToDo(arguments));
        case "deadline" -> new AddCommand(parseDeadline(arguments));
        case "event" -> new AddCommand(parseEvent(arguments));
        case "mark" -> new MarkCommand(parseTaskIndex(arguments));
        case "unmark" -> new UnmarkCommand(parseTaskIndex(arguments));
        case "delete" -> new DeleteCommand(parseTaskIndex(arguments));
        case "priority" -> parsePriority(arguments);
        case "on" -> new OnCommand(parseOnDate(arguments));
        case "find" -> new FindCommand(parseKeyword(arguments));
        // Only the keyword is quoted back. Repeating the whole line would
        // bury the one word that was not understood.
        default -> throw new AlfredException("I'm afraid I don't know '" + keyword + "', sir.");
        };
    }

    /**
     * Builds a todo from the description part of a {@code todo} command.
     *
     * @param arguments everything the user typed after the keyword.
     * @return the todo the arguments describe.
     * @throws AlfredException if no description was given.
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
     * @param arguments everything the user typed after the keyword.
     * @return the deadline the arguments describe.
     * @throws AlfredException if the description or the due date is missing,
     *         if {@code /by} is given more than once, or if the due date cannot
     *         be read.
     */
    private static Deadline parseDeadline(String arguments) throws AlfredException {
        String complaint = "A deadline needs a description and a /by date, sir.";
        String[] parts = splitAt(arguments, SEPARATOR_BY, complaint);
        String description = parts[0];
        String by = parts[1];
        // Checked for presence before being read, so that a date left out draws
        // the complaint about the command rather than one about its format.
        if (description.isEmpty() || by.isEmpty()) {
            throw new AlfredException(complaint);
        }
        return new Deadline(description, Dates.parse(by));
    }

    /**
     * Builds an event from the {@code <description> /from <start> /to <end>}
     * part of an {@code event} command.
     *
     * @param arguments everything the user typed after the keyword.
     * @return the event the arguments describe.
     * @throws AlfredException if the description, the start, or the end is
     *         missing, if {@code /from} or {@code /to} is given more than once
     *         or in the wrong order, if either date cannot be read, or if the
     *         event ends before it starts.
     */
    private static Event parseEvent(String arguments) throws AlfredException {
        String complaint = "An event needs a description, a /from date, and a /to date, sir.";
        String[] fromParts = splitAt(arguments, SEPARATOR_FROM, complaint);
        String description = fromParts[0];
        // The end date is looked for after /from, so that a /to inside the
        // description is not mistaken for the one that starts it. A /to that
        // sits only before /from is the two keywords in the wrong order, which
        // deserves better than being told the /to is missing.
        if (!containsKeyword(fromParts[1], SEPARATOR_TO) && containsKeyword(description, SEPARATOR_TO)) {
            throw new AlfredException("The /from date has to come before the /to date, sir.");
        }
        String[] toParts = splitAt(fromParts[1], SEPARATOR_TO, complaint);
        String from = toParts[0];
        String to = toParts[1];
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new AlfredException(complaint);
        }
        LocalDate start = Dates.parse(from);
        LocalDate end = Dates.parse(to);
        // Checked once both are real dates. An event of a single day is allowed,
        // so only a strictly earlier end is refused.
        if (end.isBefore(start)) {
            throw new AlfredException("An event cannot end before it starts, sir.");
        }
        return new Event(description, start, end);
    }

    /**
     * Builds the command a {@code priority <number> <level>} line asks for.
     *
     * <p>Whether the number names a task is {@link TaskList}'s to answer, as it
     * is for {@code mark}.
     *
     * @param arguments everything the user typed after {@code priority}.
     * @return the command the arguments describe.
     * @throws AlfredException if either part is missing, if the number is not a
     *         number, or if the level names no level.
     */
    private static Command parsePriority(String arguments) throws AlfredException {
        String[] parts = arguments.split(" ", 2);
        if (parts.length < 2) {
            throw new AlfredException("The priority command needs a task number and a level, sir.");
        }
        return new PriorityCommand(parseTaskIndex(parts[0]), Priority.parse(parts[1]));
    }

    /**
     * Reads the day an {@code on} command is asking about.
     *
     * <p>A missing date is caught here, since {@link Dates} would quote back an
     * empty string.
     *
     * @param arguments everything the user typed after {@code on}.
     * @return the day being asked about.
     * @throws AlfredException if no date was given, or it cannot be read.
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
     * <p>Everything after the keyword is taken as one phrase. An empty search is
     * refused, since every description contains the empty string.
     *
     * @param arguments everything the user typed after {@code find}.
     * @return the text to search descriptions for.
     * @throws AlfredException if no keyword was given.
     */
    private static String parseKeyword(String arguments) throws AlfredException {
        if (arguments.isEmpty()) {
            throw new AlfredException("The find command needs a keyword, sir.");
        }
        return arguments;
    }

    /**
     * Converts a task number typed by the user into a list index, since the user
     * numbers tasks from 1 but the list is indexed from 0.
     *
     * <p>Only whether the text is a number is settled here. Whether it names a
     * task is {@link TaskList}'s to answer.
     *
     * @param arguments everything typed after {@code mark}, {@code unmark}
     *        or {@code delete}.
     * @return the index the number points at, which may be outside the list.
     * @throws AlfredException if the arguments are not a number.
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

    /**
     * Squeezes the spacing of a line: whitespace at either end is dropped, and
     * every run of it inside the line becomes one space.
     *
     * <p>Done once, up front, so that nothing after it has to allow for a tab
     * where a space was expected or for two spaces where one was.
     *
     * @param line what the user typed.
     * @return the same words, separated by single spaces.
     */
    private static String normalizeSpacing(String line) {
        return WHITESPACE.matcher(line.strip()).replaceAll(" ");
    }

    /**
     * Splits text into the part before a keyword and the part after it, each
     * with its spacing trimmed.
     *
     * <p>The keyword counts only as a word of its own, so {@code a/by} in a
     * description is not read as the separator. The two failures have
     * different messages: a keyword that is not there draws the caller's
     * complaint about the command, and one that is there more than once is
     * named, since the user would otherwise be told that a date such as
     * {@code 2019-10-15 /by 2019-10-16} cannot be read.
     *
     * @param text the arguments to split.
     * @param keyword the separator to split at, such as {@code /by}.
     * @param complaint what to say if the keyword is not there.
     * @return the part before the keyword and the part after it, either of
     *         which may be empty.
     * @throws AlfredException if the keyword is missing or repeated.
     */
    private static String[] splitAt(String text, String keyword, String complaint)
            throws AlfredException {
        // A limit of -1 keeps an empty last part, so that a keyword with nothing
        // after it still splits into two parts and the caller sees an empty
        // second one, rather than this method deciding what to say about it.
        String[] parts = keywordPattern(keyword).split(text, -1);
        if (parts.length < 2) {
            throw new AlfredException(complaint);
        }
        if (parts.length > 2) {
            throw new AlfredException("You've given " + keyword + " more than once, sir. Once will do.");
        }
        return new String[] {parts[0].trim(), parts[1].trim()};
    }

    /**
     * Tells whether text holds a keyword as a word of its own.
     *
     * @param text the text to look in.
     * @param keyword the separator to look for, such as {@code /to}.
     * @return true if the keyword is there, on its own.
     */
    private static boolean containsKeyword(String text, String keyword) {
        return keywordPattern(keyword).matcher(text).find();
    }

    /**
     * Returns a pattern matching a keyword only where it stands as a word of its
     * own: at the start or the end of the text, or with whitespace on both sides.
     *
     * @param keyword the separator to match, such as {@code /from}.
     * @return the pattern that finds it.
     */
    private static Pattern keywordPattern(String keyword) {
        return Pattern.compile("(?<!\\S)" + Pattern.quote(keyword) + "(?!\\S)");
    }
}
