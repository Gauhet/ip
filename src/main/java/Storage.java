import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the task list on the hard disk, so that tasks outlive a single run of
 * the program. One line of the file describes one task, in the order the tasks
 * are stored:
 *
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | June 6th
 * E | 0 | project meeting | Mon 2pm | 4pm
 * </pre>
 *
 * <p>The first field is the type letter, the second is 1 for a task that is
 * done and 0 for one that is not, and the third is the description. Any fields
 * after that belong to the type: the due time of a deadline, or the start and
 * end of an event. Each task writes its own line, in
 * {@link Task#toFileFormat()}.
 *
 * <p>The file is written after every change and read once at startup, so the
 * list a run begins with is the list the previous run ended with.
 */
public class Storage {
    /**
     * Where the tasks are kept, relative to the directory the program is
     * started from. Hard-coded because nothing yet asks for a second save file.
     */
    private static final Path FILE = Path.of("data", "alfred.txt");

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
            lines.add(task.toFileFormat());
        }
        try {
            Files.createDirectories(FILE.getParent());
            Files.write(FILE, lines);
        } catch (IOException e) {
            // Reported the same way as a mistyped command, so that a disk
            // problem becomes a reply the user can read rather than a stack
            // trace that ends the session.
            throw new AlfredException("I could not save your tasks, sir: " + e.getMessage());
        }
    }

    /**
     * Reads the saved tasks back, in the order they were written.
     *
     * <p>A missing file is not a problem: it is what the first ever run sees,
     * and it means there is nothing to restore rather than that something went
     * wrong.
     *
     * @return the saved tasks, or an empty list if nothing has been saved yet
     * @throws AlfredException if the file exists but cannot be read
     */
    public List<Task> load() throws AlfredException {
        List<Task> tasks = new ArrayList<>();
        if (!Files.exists(FILE)) {
            return tasks;
        }
        try {
            for (String line : Files.readAllLines(FILE)) {
                tasks.add(parseTask(line));
            }
        } catch (IOException | RuntimeException e) {
            // Deliberately minimal: one bad line gives up on the whole file.
            // RuntimeException is caught alongside IOException because a short
            // or misspelled line fails inside parseTask rather than on the read.
            // Handling each line on its own, so that one typo costs one task
            // instead of all of them, is worth doing but is not done yet.
            throw new AlfredException("I could not read your saved tasks, sir. Starting with an empty list.");
        }
        return tasks;
    }

    /**
     * Builds the task that one line of the save file describes.
     *
     * <p>The line is assumed to be well formed, which is what makes this short.
     * A line that is not throws, and {@link #load()} turns that into the one
     * message it reports.
     *
     * @param line a single line of the save file
     * @return the task that line describes
     */
    private static Task parseTask(String line) {
        // Split on the separator with its spaces, so that the spaces are not
        // left on the ends of the fields.
        String[] fields = line.split(" \\| ");
        Task task = switch (fields[0]) {
        case "T" -> new ToDo(fields[2]);
        case "D" -> new Deadline(fields[2], fields[3]);
        case "E" -> new Event(fields[2], fields[3], fields[4]);
        default -> throw new IllegalArgumentException("Unknown task type: " + fields[0]);
        };
        // A task is built not done, so only a saved 1 needs acting on.
        if (fields[1].equals("1")) {
            task.markDone();
        }
        return task;
    }
}
