package alfred;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import alfred.task.Deadline;
import alfred.task.Event;
import alfred.task.Task;
import alfred.task.ToDo;

/**
 * Keeps the task list on the hard disk, so that tasks outlive a single run of
 * the program. One line of the file describes one task, in the order the tasks
 * are stored:
 *
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | 2019-10-15
 * E | 0 | project meeting | 2019-12-02 | 2019-12-03
 * </pre>
 *
 * <p>The first field is the type letter, the second is 1 for a task that is
 * done and 0 for one that is not, and the third is the description. Any fields
 * after that belong to the type: the due date of a deadline, or the start and
 * end of an event, each written as {@code yyyy-mm-dd}. Each task says what its
 * fields are, in
 * {@link Task#toFileFields()}; joining them into a line and splitting them back
 * out is done here, so that the two halves cannot drift apart.
 *
 * <p>A description may itself contain the separator, as in {@code todo a | b}.
 * Such a character is escaped on the way out and restored on the way in, so
 * that any text the user can type survives the round trip.
 *
 * <p>The file is written after every change and read once at startup, so the
 * list a run begins with is the list the previous run ended with.
 */
public class Storage {
    /** What goes between two fields of a line, with a space on each side. */
    private static final String SEPARATOR = " | ";

    /** The character the separator is built from, and which therefore has to be escaped. */
    private static final char SEPARATOR_CHAR = '|';

    /** Marks the character after it as part of a field rather than as a separator. */
    private static final char ESCAPE_CHAR = '\\';

    /** How many fields a line has, by type letter, counting the type letter itself. */
    private static final int TODO_FIELDS = 3;

    private static final int DEADLINE_FIELDS = 4;

    private static final int EVENT_FIELDS = 5;

    /**
     * What one call to {@link Storage#load()} found: the tasks it could read,
     * and how many lines it had to give up on.
     *
     * <p>The two travel together so that the count cannot be read without the
     * tasks it belongs to, which is what a field on {@code Storage} would
     * allow.
     *
     * @param tasks the tasks that were read, in the order they were saved
     * @param skippedLines how many lines could not be understood
     */
    public record LoadResult(List<Task> tasks, int skippedLines) { }

    /** Where the tasks are kept, as given to the constructor. */
    private final Path file;

    /**
     * Prepares to keep the tasks in one named file.
     *
     * <p>Taking the path rather than deciding it is what keeps the choice of
     * where to save out of this class: a test can point it at a scratch file,
     * and nothing here has to know which file a real run uses.
     *
     * <p>The path is written with forward slashes and turned into a
     * {@link Path} here, which reads it with whatever separator the operating
     * system in use expects. A relative path, such as {@code data/alfred.txt},
     * is read against the directory the program was started from rather than
     * against one particular computer's layout.
     *
     * @param filePath where to keep the tasks, such as {@code data/alfred.txt}
     */
    public Storage(String filePath) {
        this.file = Path.of(filePath);
    }

    /**
     * Writes the whole task list to the save file, replacing whatever it held
     * before, and creating the file and its folder if they are not there yet.
     *
     * <p>Rewriting every task is more work than editing the one line that
     * changed, but the list is small and a full rewrite cannot leave the file
     * half-updated. It also means one method covers adding, deleting, marking,
     * and unmarking alike.
     *
     * @param tasks the tasks to save, in the order they are stored
     * @throws AlfredException if the file cannot be written
     */
    public void save(List<Task> tasks) throws AlfredException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(joinFields(task.toFileFields()));
        }
        try {
            Path folder = file.getParent();
            // Null when the path is a bare filename, naming no folder to put the
            // file in, so there is nothing to create. Otherwise the folder is
            // made if it is missing and left alone if it is not, so the first
            // run on a new computer needs no special case.
            if (folder != null) {
                Files.createDirectories(folder);
            }
            Files.write(file, lines);
        } catch (IOException e) {
            // Reported the same way as a mistyped command, so that a disk
            // problem becomes a reply the user can read rather than a stack
            // trace that ends the session.
            throw new AlfredException("I could not save your tasks, sir: " + describe(e));
        }
    }

    /**
     * Reads the saved tasks back, in the order they were written.
     *
     * <p>A missing file is not a problem: it is what the first ever run sees,
     * and it means there is nothing to restore rather than that something went
     * wrong. A blank line is not a problem either, and is passed over silently.
     *
     * <p>A line that cannot be understood is skipped rather than abandoning the
     * whole file, so that one damaged line costs one task instead of all of
     * them. The caller is told how many were skipped, because those lines are
     * gone from the file as soon as the list next changes.
     *
     * <p>Skipping works by catching {@link AlfredException}, so everything that
     * can refuse a line has to raise that one and not an unchecked exception.
     * A date is the case to watch: {@link Dates#parse(String)} is what makes a
     * hand-edited date cost the line it sits on, because the exception
     * {@code java.time} would otherwise throw is unchecked and would escape
     * this loop with the rest of the file unread.
     *
     * <p>The messages those refusals carry are written for someone who typed
     * the text, and none of them is shown here: a line that cannot be read is
     * only counted. It is the refusal that matters, not its wording.
     *
     * @return the tasks that could be read, and how many lines were skipped
     * @throws AlfredException if the file exists but cannot be read at all
     */
    public LoadResult load() throws AlfredException {
        List<Task> tasks = new ArrayList<>();
        if (!Files.exists(file)) {
            return new LoadResult(tasks, 0);
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            // A file that cannot be opened or decoded at all is different from
            // a file with a bad line in it: there is nothing to salvage.
            throw new AlfredException("I could not read your saved tasks, sir: " + describe(e));
        }

        int skippedLines = 0;
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            try {
                tasks.add(parseTask(line));
            } catch (AlfredException e) {
                skippedLines++;
            }
        }
        return new LoadResult(tasks, skippedLines);
    }

    /**
     * Describes a file problem in a way that says what went wrong.
     *
     * <p>The message of a file exception is often only the path, which the user
     * can already see, so the kind of failure is named as well. That kind is
     * carried by the class: {@code AccessDeniedException} and
     * {@code NoSuchFileException} both report the same message and differ in
     * nothing else.
     *
     * @param e the problem that came back from the file system
     * @return a short description naming the kind of failure and the path
     */
    private static String describe(IOException e) {
        String kind = e.getClass().getSimpleName();
        if (e.getMessage() == null) {
            return kind;
        }
        return kind + " on " + e.getMessage();
    }

    /**
     * Joins a task's fields into one line, escaping any separator character
     * inside a field so that splitting the line again cannot mistake it for a
     * separator.
     *
     * @param fields the task's fields, in the order they are saved
     * @return the line to write to the file
     */
    private static String joinFields(List<String> fields) {
        List<String> escaped = new ArrayList<>();
        for (String field : fields) {
            escaped.add(escape(field));
        }
        return String.join(SEPARATOR, escaped);
    }

    /**
     * Marks the characters in a field that would otherwise be read as a
     * separator. The escape character is escaped first; doing it second would
     * escape the marks added for the separators as well.
     *
     * @param field one field of a line, as the user typed it
     * @return the field as it is written to the file
     */
    private static String escape(String field) {
        return field.replace(String.valueOf(ESCAPE_CHAR), "" + ESCAPE_CHAR + ESCAPE_CHAR)
                .replace(String.valueOf(SEPARATOR_CHAR), "" + ESCAPE_CHAR + SEPARATOR_CHAR);
    }

    /**
     * Splits a line into its fields and undoes the escaping in one pass.
     *
     * <p>Scanning character by character rather than splitting on the separator
     * is what lets an escaped separator be told apart from a real one. A
     * regular expression could do it too, but it would have to look backwards
     * for an escape character, and would then be wrong about a field that ends
     * with an escaped escape character.
     *
     * @param line one line of the save file
     * @return the fields of that line, in order, with the escaping removed
     */
    private static List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == ESCAPE_CHAR && i + 1 < line.length()) {
                // The character after the mark is part of the field whatever it
                // is, which is what makes an escaped separator harmless.
                field.append(line.charAt(i + 1));
                i++;
            } else if (current == SEPARATOR_CHAR) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }
        fields.add(field.toString().trim());
        return fields;
    }

    /**
     * Builds the task that one line of the save file describes.
     *
     * <p>Every part of the line is checked, because the file can be edited by
     * hand and a wrong line should cost only itself. A line with the wrong
     * number of fields, an unknown type letter, a status that is neither 0 nor
     * 1, an empty description, or a date field that is not a date is refused
     * rather than turned into a task that misrepresents what was saved.
     *
     * @param line a single line of the save file
     * @return the task that line describes
     * @throws AlfredException if the line is not a task this program wrote
     */
    private static Task parseTask(String line) throws AlfredException {
        List<String> fields = splitFields(line);
        String type = fields.get(0);
        int expectedFields = switch (type) {
        case "T" -> TODO_FIELDS;
        case "D" -> DEADLINE_FIELDS;
        case "E" -> EVENT_FIELDS;
        default -> throw new AlfredException("Unknown task type: " + type);
        };
        if (fields.size() != expectedFields) {
            throw new AlfredException("A " + type + " line needs " + expectedFields + " fields");
        }

        String description = fields.get(2);
        if (description.isEmpty()) {
            throw new AlfredException("A task needs a description");
        }
        Task task = switch (type) {
        case "T" -> new ToDo(description);
        case "D" -> new Deadline(description, Dates.parse(fields.get(3)));
        case "E" -> new Event(description, Dates.parse(fields.get(3)), Dates.parse(fields.get(4)));
        default -> throw new AlfredException("Unknown task type: " + type);
        };

        // Checked rather than compared against "1" alone, so that a status of
        // anything else is treated as damage instead of quietly meaning "not
        // done".
        String status = fields.get(1);
        if (status.equals("1")) {
            task.markDone();
        } else if (!status.equals("0")) {
            throw new AlfredException("A status must be 0 or 1, not " + status);
        }
        return task;
    }
}
