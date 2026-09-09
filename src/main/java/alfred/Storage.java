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
 * after that belong to the type, each written as {@code yyyy-mm-dd}. A
 * description may itself contain the separator, as in {@code todo a | b}, so
 * that character is escaped on the way out and restored on the way in.
 */
public class Storage {
    /** What goes between two fields of a line, with a space on each side. */
    private static final String SEPARATOR = " | ";

    /** The character the separator is built from, and which therefore has to be escaped. */
    private static final char SEPARATOR_CHAR = '|';

    /** Marks the character after it as part of a field rather than as a separator. */
    private static final char ESCAPE_CHAR = '\\';

    /** How many fields a line has, by type letter, counting the type letter itself. */
    private static final int FIELDS_TODO = 3;

    private static final int FIELDS_DEADLINE = 4;

    private static final int FIELDS_EVENT = 5;

    /**
     * What one call to {@link Storage#load()} found: the tasks it could read,
     * and how many lines it had to give up on.
     *
     * @param tasks the tasks that were read, in the order they were saved.
     * @param skippedLines how many lines could not be understood.
     */
    record LoadResult(List<Task> tasks, int skippedLines) { }

    /** Where the tasks are kept, as given to the constructor. */
    private final Path file;

    /**
     * Prepares to keep the tasks in one named file.
     *
     * <p>A relative path is read against the directory the program was started
     * from.
     *
     * @param filePath where to keep the tasks, such as {@code data/alfred.txt}.
     */
    Storage(String filePath) {
        this.file = Path.of(filePath);
    }

    /**
     * Writes the whole task list to the save file, replacing whatever it held
     * before, and creating the file and its folder if they are not there yet.
     *
     * <p>A full rewrite cannot leave the file half-updated.
     *
     * @param tasks the tasks to save, in the order they are stored.
     * @throws AlfredException if the file cannot be written.
     */
    public void save(List<Task> tasks) throws AlfredException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(joinFields(task.toFileFields()));
        }
        try {
            Path folder = file.getParent();
            // Null when the path is a bare filename, naming no folder to create.
            if (folder != null) {
                Files.createDirectories(folder);
            }
            Files.write(file, lines);
        } catch (IOException e) {
            throw new AlfredException("I could not save your tasks, sir: " + describe(e));
        }
    }

    /**
     * Reads the saved tasks back, in the order they were written.
     *
     * <p>A missing file is what the first ever run sees, and is not a problem.
     * A bad line is skipped rather than abandoning the whole file.
     *
     * <p>Skipping works by catching {@link AlfredException}, so everything that
     * can refuse a line has to raise that one and not an unchecked exception.
     * {@link Dates#parse(String)} is the case to watch.
     *
     * @return the tasks that could be read, and how many lines were skipped.
     * @throws AlfredException if the file exists but cannot be read at all.
     */
    LoadResult load() throws AlfredException {
        List<Task> tasks = new ArrayList<>();
        if (!Files.exists(file)) {
            return new LoadResult(tasks, 0);
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            // A file that cannot be opened at all is different from a file with
            // a bad line in it: there is nothing to salvage.
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
     * <p>The message of a file exception is often only the path, so the class
     * name is used to say what kind of failure it was.
     *
     * @param e the problem that came back from the file system.
     * @return a short description naming the kind of failure and the path.
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
     * inside a field.
     *
     * @param fields the task's fields, in the order they are saved.
     * @return the line to write to the file.
     */
    private static String joinFields(List<String> fields) {
        List<String> escapedFields = new ArrayList<>();
        for (String field : fields) {
            escapedFields.add(escape(field));
        }
        return String.join(SEPARATOR, escapedFields);
    }

    /**
     * Marks the characters in a field that would otherwise be read as a
     * separator. The escape character is escaped first; doing it second would
     * escape the marks added for the separators as well.
     *
     * @param field one field of a line, as the user typed it.
     * @return the field as it is written to the file.
     */
    private static String escape(String field) {
        return field.replace(String.valueOf(ESCAPE_CHAR), "" + ESCAPE_CHAR + ESCAPE_CHAR)
                .replace(String.valueOf(SEPARATOR_CHAR), "" + ESCAPE_CHAR + SEPARATOR_CHAR);
    }

    /**
     * Splits a line into its fields and undoes the escaping in one pass.
     *
     * <p>Scanning character by character is what lets an escaped separator be
     * told apart from a real one.
     *
     * @param line one line of the save file.
     * @return the fields of that line, in order, with the escaping removed.
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
     * hand and a wrong line should cost only itself.
     *
     * @param line a single line of the save file.
     * @return the task that line describes.
     * @throws AlfredException if the line is not a task this program wrote.
     */
    private static Task parseTask(String line) throws AlfredException {
        List<String> fields = splitFields(line);
        String type = fields.get(0);
        Task task = switch (type) {
        case "T" -> parseToDo(fields);
        case "D" -> parseDeadline(fields);
        case "E" -> parseEvent(fields);
        default -> throw new AlfredException("Unknown task type: " + type);
        };
        applyStatus(task, fields.get(1));
        return task;
    }

    /**
     * Builds the todo that one save line describes.
     *
     * @param fields the fields of a line whose type letter is {@code T}.
     * @return the todo those fields describe.
     * @throws AlfredException if the line holds the wrong number of fields, or
     *         its description is empty.
     */
    private static ToDo parseToDo(List<String> fields) throws AlfredException {
        checkFieldCount(fields, FIELDS_TODO);
        return new ToDo(readDescription(fields));
    }

    /**
     * Builds the deadline that one save line describes.
     *
     * @param fields the fields of a line whose type letter is {@code D}.
     * @return the deadline those fields describe.
     * @throws AlfredException if the line holds the wrong number of fields, its
     *         description is empty, or its date cannot be read.
     */
    private static Deadline parseDeadline(List<String> fields) throws AlfredException {
        checkFieldCount(fields, FIELDS_DEADLINE);
        return new Deadline(readDescription(fields), Dates.parse(fields.get(3)));
    }

    /**
     * Builds the event that one save line describes.
     *
     * @param fields the fields of a line whose type letter is {@code E}.
     * @return the event those fields describe.
     * @throws AlfredException if the line holds the wrong number of fields, its
     *         description is empty, or either of its dates cannot be read.
     */
    private static Event parseEvent(List<String> fields) throws AlfredException {
        checkFieldCount(fields, FIELDS_EVENT);
        return new Event(readDescription(fields), Dates.parse(fields.get(3)),
                Dates.parse(fields.get(4)));
    }

    /**
     * Refuses a line that does not hold exactly the fields its type calls for.
     *
     * @param fields the fields the line was split into.
     * @param expectedFields how many fields the line's type letter calls for.
     * @throws AlfredException if the line holds any other number of them.
     */
    private static void checkFieldCount(List<String> fields, int expectedFields)
            throws AlfredException {
        if (fields.size() != expectedFields) {
            throw new AlfredException("A " + fields.get(0) + " line needs "
                    + expectedFields + " fields");
        }
    }

    /**
     * Returns the description a save line carries, which every type of task has.
     *
     * @param fields the fields the line was split into.
     * @return the description the line gives.
     * @throws AlfredException if the description is empty.
     */
    private static String readDescription(List<String> fields) throws AlfredException {
        String description = fields.get(2);
        if (description.isEmpty()) {
            throw new AlfredException("A task needs a description");
        }
        return description;
    }

    /**
     * Marks a task done or not done, as the status field of its save line says.
     *
     * <p>The field is checked rather than compared against {@code 1} alone, so
     * that anything else is treated as damage instead of quietly meaning "not
     * done".
     *
     * @param task the task the rest of the line described.
     * @param status the status field of that line.
     * @throws AlfredException if the status is neither {@code 0} nor {@code 1}.
     */
    private static void applyStatus(Task task, String status) throws AlfredException {
        if (status.equals("1")) {
            task.markDone();
        } else if (!status.equals("0")) {
            throw new AlfredException("A status must be 0 or 1, not " + status);
        }
    }
}
