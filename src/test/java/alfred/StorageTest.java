package alfred;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import alfred.task.Deadline;
import alfred.task.Event;
import alfred.task.Task;
import alfred.task.ToDo;

/**
 * Tests {@link Storage#save(List)} and {@link Storage#load()}, the two halves of
 * keeping the task list on disk.
 *
 * <p>Most of the tests save and load in one go rather than checking either
 * alone. What matters about this class is not the shape of a line but that the
 * list a run ends with is the list the next run starts from, and a round trip is
 * the only thing that says so. It also catches the failure the two halves can
 * have between them, where each is self-consistent and they disagree with each
 * other.
 *
 * <p>One test does pin the file format line by line, because the format is
 * documented and a silent change to it would strand every file already written.
 *
 * <p>The rest cover a save file that has been edited by hand, which is the case
 * the class is careful about: a damaged line has to cost that line alone and be
 * counted, rather than taking the file down with it.
 *
 * <p>Every test writes inside a scratch folder that JUnit makes and removes, so
 * nothing here touches the real save file.
 */
public class StorageTest {
    private static final LocalDate OCT_15 = LocalDate.of(2019, 10, 15);

    private static final LocalDate DEC_2 = LocalDate.of(2019, 12, 2);

    private static final LocalDate DEC_3 = LocalDate.of(2019, 12, 3);

    /** The save file under test, inside the scratch folder. */
    private Path file;

    private Storage storage;

    /**
     * Points the storage at a file of this test's own.
     *
     * @param tempDir a scratch folder JUnit creates and deletes per test.
     */
    @BeforeEach
    public void setUp(@TempDir Path tempDir) {
        file = tempDir.resolve("tasks.txt");
        storage = new Storage(file.toString());
    }

    @Test
    public void save_allThreeTaskTypes_documentedFormatWritten() throws AlfredException, IOException {
        ToDo todo = new ToDo("read book");
        todo.markDone();
        storage.save(List.of(todo,
                new Deadline("return book", OCT_15),
                new Event("project meeting", DEC_2, DEC_3)));
        assertEquals(List.of("T | 1 | read book",
                        "D | 0 | return book | 2019-10-15",
                        "E | 0 | project meeting | 2019-12-02 | 2019-12-03"),
                Files.readAllLines(file));
    }

    @Test
    public void save_folderMissing_folderCreated() throws AlfredException {
        Path nested = file.getParent().resolve("data").resolve("tasks.txt");
        new Storage(nested.toString()).save(List.of(new ToDo("read book")));
        assertTrue(Files.exists(nested));
    }

    @Test
    public void saveThenLoad_allThreeTaskTypes_tasksRestoredInOrder() throws AlfredException {
        storage.save(List.of(new ToDo("read book"),
                new Deadline("return book", OCT_15),
                new Event("project meeting", DEC_2, DEC_3)));
        Storage.LoadResult result = storage.load();
        assertEquals(0, result.skippedLines());
        assertEquals(List.of("[T][ ] read book",
                        "[D][ ] return book (by: Oct 15 2019)",
                        "[E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)"),
                describeAll(result.tasks()));
    }

    @Test
    public void saveThenLoad_doneTask_stillDone() throws AlfredException {
        ToDo todo = new ToDo("read book");
        todo.markDone();
        storage.save(List.of(todo));
        assertEquals("[T][X] read book", storage.load().tasks().get(0).toString());
    }

    @Test
    public void saveThenLoad_emptyList_nothingRestored() throws AlfredException {
        storage.save(List.of());
        Storage.LoadResult result = storage.load();
        assertTrue(result.tasks().isEmpty());
        assertEquals(0, result.skippedLines());
    }

    @Test
    public void saveThenLoad_descriptionContainingSeparator_descriptionRestored() throws AlfredException {
        assertDescriptionSurvives("a | b");
    }

    @Test
    public void saveThenLoad_descriptionContainingEscapeCharacter_descriptionRestored()
            throws AlfredException {
        assertDescriptionSurvives("a \\ b");
    }

    @Test
    public void saveThenLoad_descriptionContainingEscapedSeparator_descriptionRestored()
            throws AlfredException {
        // An escape character followed by a separator, both typed by the user.
        // Reading the line by looking backwards for an escape character gets
        // this one wrong, which is why the fields are scanned forwards.
        assertDescriptionSurvives("a \\| b");
    }

    @Test
    public void saveThenLoad_descriptionEndingWithEscapeCharacter_descriptionRestored()
            throws AlfredException {
        assertDescriptionSurvives("note\\");
    }

    @Test
    public void load_fileMissing_noTasksAndNothingSkipped() throws AlfredException {
        Storage.LoadResult result = storage.load();
        assertTrue(result.tasks().isEmpty());
        assertEquals(0, result.skippedLines());
    }

    @Test
    public void load_blankLines_passedOverWithoutCounting() throws AlfredException, IOException {
        Files.write(file, List.of("", "T | 0 | read book", "   ", ""));
        Storage.LoadResult result = storage.load();
        assertEquals(1, result.tasks().size());
        // A blank line is not damage, so it is not held against the file.
        assertEquals(0, result.skippedLines());
    }

    @Test
    public void load_unknownTypeLetter_lineSkipped() throws AlfredException, IOException {
        assertLineSkipped("X | 0 | read book");
    }

    @Test
    public void load_tooFewFields_lineSkipped() throws AlfredException, IOException {
        assertLineSkipped("T | 0");
    }

    @Test
    public void load_tooManyFields_lineSkipped() throws AlfredException, IOException {
        assertLineSkipped("T | 0 | read book | 2019-10-15");
    }

    @Test
    public void load_statusNeitherZeroNorOne_lineSkipped() throws AlfredException, IOException {
        // A status of anything else is damage rather than a quiet "not done".
        assertLineSkipped("T | 2 | read book");
    }

    @Test
    public void load_emptyDescription_lineSkipped() throws AlfredException, IOException {
        assertLineSkipped("T | 0 | ");
    }

    @Test
    public void load_unreadableDate_lineSkipped() throws AlfredException, IOException {
        assertLineSkipped("D | 0 | return book | Sunday");
    }

    @Test
    public void load_dateNamingNoRealDay_lineSkipped() throws AlfredException, IOException {
        // Caught as an AlfredException rather than escaping as an unchecked one,
        // which is what keeps the rest of the file readable.
        assertLineSkipped("D | 0 | return book | 2019-02-30");
    }

    @Test
    public void load_damagedLineAmongGoodOnes_onlyDamagedLineLost() throws AlfredException, IOException {
        Files.write(file, List.of("T | 0 | read book", "X | 0 | broken", "T | 1 | write essay"));
        Storage.LoadResult result = storage.load();
        assertEquals(1, result.skippedLines());
        assertEquals(List.of("[T][ ] read book", "[T][X] write essay"), describeAll(result.tasks()));
    }

    @Test
    public void load_severalDamagedLines_allCounted() throws AlfredException, IOException {
        Files.write(file, List.of("X | 0 | broken", "T | 2 | bad status", "T | 0"));
        Storage.LoadResult result = storage.load();
        assertTrue(result.tasks().isEmpty());
        assertEquals(3, result.skippedLines());
    }

    /**
     * Checks that a description survives being written and read back, whatever
     * characters it holds.
     *
     * @param description the description to put through the save file.
     * @throws AlfredException if the file cannot be written or read.
     */
    private void assertDescriptionSurvives(String description) throws AlfredException {
        storage.save(List.of(new ToDo(description)));
        Storage.LoadResult result = storage.load();
        assertEquals(0, result.skippedLines());
        assertEquals("[T][ ] " + description, result.tasks().get(0).toString());
    }

    /**
     * Checks that one damaged line is left out and counted, rather than read as
     * a task or taking the whole file down.
     *
     * @param line the line to put in the save file.
     * @throws AlfredException if the file cannot be read.
     * @throws IOException if the line cannot be written.
     */
    private void assertLineSkipped(String line) throws AlfredException, IOException {
        Files.write(file, List.of(line));
        Storage.LoadResult result = storage.load();
        assertTrue(result.tasks().isEmpty());
        assertEquals(1, result.skippedLines());
    }

    /**
     * Returns the tasks as the text they are shown by, which is what makes a
     * restored list comparable without every task class needing an equals.
     *
     * @param tasks the tasks to describe.
     * @return one display form per task, in the order given.
     */
    private List<String> describeAll(List<Task> tasks) {
        return tasks.stream().map(Task::toString).toList();
    }
}
