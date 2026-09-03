package alfred.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import alfred.AlfredException;

/**
 * Tests {@link TaskList}, the list the whole program works through.
 *
 * <p>Two things here are worth more than the rest.
 *
 * <p>The first is the index check. It stands between a number the user typed and
 * a list that would throw an unchecked exception of its own, so every operation
 * that takes an index is tried just below the list, just above it, and against
 * an empty list. Those are the three places an off-by-one shows up.
 *
 * <p>The second is the copying. The class promises that the tasks handed in
 * cannot be changed from outside afterwards, whether they arrive as a list or as
 * the array behind a varargs call, and that the list handed out cannot be used
 * to change what is stored. None of those promises is visible in ordinary use,
 * and each would be quietly broken by keeping or returning what was passed, so
 * each gets a test.
 */
public class TaskListTest {
    private static final String NO_SUCH_TASK = "There is no such task, sir.";

    /** A list holding three todos, rebuilt for every test. */
    private TaskList tasks;

    @BeforeEach
    public void setUp() {
        tasks = new TaskList(new ToDo("first"), new ToDo("second"), new ToDo("third"));
    }

    @Test
    public void newList_noTasksGiven_emptyAndSizeZero() {
        TaskList empty = new TaskList();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
    }

    @Test
    public void newList_severalTasksGiven_storedInTheOrderGiven() {
        assertFalse(tasks.isEmpty());
        assertEquals(3, tasks.size());
        assertEquals("[T][ ] first", tasks.get(0).toString());
        assertEquals("[T][ ] third", tasks.get(2).toString());
    }

    @Test
    public void add_listAlreadyHoldingTasks_taskStoredAtTheEnd() {
        tasks.add(new ToDo("fourth"));
        assertEquals(4, tasks.size());
        assertEquals("[T][ ] fourth", tasks.get(3).toString());
    }

    @Test
    public void delete_middleTask_taskReturnedAndGapClosed() throws AlfredException {
        Task removed = tasks.delete(1);
        assertEquals("[T][ ] second", removed.toString());
        assertEquals(2, tasks.size());
        // The task after the removed one moves up a number, which is what the
        // user sees the next time they list the tasks.
        assertEquals("[T][ ] third", tasks.get(1).toString());
    }

    @Test
    public void delete_lastTask_listLeftEmpty() throws AlfredException {
        tasks.delete(2);
        tasks.delete(1);
        tasks.delete(0);
        assertTrue(tasks.isEmpty());
    }

    @Test
    public void delete_indexBelowRange_exceptionThrown() {
        assertRefused(() -> tasks.delete(-1));
    }

    @Test
    public void delete_indexJustPastEnd_exceptionThrown() {
        assertRefused(() -> tasks.delete(3));
    }

    @Test
    public void delete_emptyList_exceptionThrown() {
        TaskList empty = new TaskList();
        assertRefused(() -> empty.delete(0));
    }

    @Test
    public void markDone_validIndex_storedTaskMarkedAndReturned() throws AlfredException {
        Task marked = tasks.markDone(1);
        // The stored task itself comes back, not a copy, so what the user is
        // shown is what the list now holds.
        assertSame(tasks.get(1), marked);
        assertEquals("[T][X] second", tasks.get(1).toString());
        assertEquals("[T][ ] first", tasks.get(0).toString());
    }

    @Test
    public void markDone_alreadyDoneTask_stillDone() throws AlfredException {
        tasks.markDone(0);
        tasks.markDone(0);
        assertEquals("[T][X] first", tasks.get(0).toString());
    }

    @Test
    public void markDone_indexBelowRange_exceptionThrown() {
        assertRefused(() -> tasks.markDone(-1));
    }

    @Test
    public void markDone_indexJustPastEnd_exceptionThrown() {
        assertRefused(() -> tasks.markDone(3));
    }

    @Test
    public void unmarkDone_markedTask_storedTaskUnmarkedAndReturned() throws AlfredException {
        tasks.markDone(1);
        Task unmarked = tasks.unmarkDone(1);
        assertSame(tasks.get(1), unmarked);
        assertEquals("[T][ ] second", tasks.get(1).toString());
    }

    @Test
    public void unmarkDone_taskThatWasNeverDone_stillNotDone() throws AlfredException {
        tasks.unmarkDone(0);
        assertEquals("[T][ ] first", tasks.get(0).toString());
    }

    @Test
    public void unmarkDone_indexBelowRange_exceptionThrown() {
        assertRefused(() -> tasks.unmarkDone(-1));
    }

    @Test
    public void unmarkDone_indexJustPastEnd_exceptionThrown() {
        assertRefused(() -> tasks.unmarkDone(3));
    }

    @Test
    public void newList_sourceArrayChangedAfterwards_storedTasksUnaffected() {
        // An array passed to a varargs parameter is the caller's own array, not
        // one the call made, so the copying the class promises has to cover it.
        Task[] source = { new ToDo("first") };
        TaskList list = new TaskList(source);
        source[0] = new ToDo("swapped in behind the list's back");
        assertEquals("[T][ ] first", list.get(0).toString());
    }

    @Test
    public void newList_sourceListChangedAfterwards_storedTasksUnaffected() {
        List<Task> source = new ArrayList<>();
        source.add(new ToDo("first"));
        TaskList list = new TaskList(source);
        source.add(new ToDo("added behind the list's back"));
        assertEquals(1, list.size());
    }

    @Test
    public void toList_returnedListModified_exceptionThrown() {
        List<Task> copy = tasks.toList();
        assertThrows(UnsupportedOperationException.class, () -> copy.add(new ToDo("sneaked in")));
    }

    @Test
    public void toList_listChangedAfterwards_returnedListUnaffected() throws AlfredException {
        List<Task> copy = tasks.toList();
        tasks.delete(0);
        assertEquals(3, copy.size());
    }

    /**
     * Checks that an operation refuses an index naming no stored task, with the
     * message written for the person who typed the number.
     *
     * @param operation the call to try.
     */
    private void assertRefused(Executable operation) {
        AlfredException e = assertThrows(AlfredException.class, operation);
        assertEquals(NO_SUCH_TASK, e.getMessage());
    }
}
