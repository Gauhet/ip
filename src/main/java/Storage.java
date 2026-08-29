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
 * <p>Only writing is implemented. Nothing reads the file back yet, so the tasks
 * it holds do not reappear when the program starts again.
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
}
