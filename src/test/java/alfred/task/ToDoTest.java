package alfred.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ToDo}, the task kind with no date attached to it.
 *
 * <p>Its {@link Task#occursOn(LocalDate)} is inherited rather than written, and
 * that inherited answer is the point: a todo falls on no day, so the {@code on}
 * command must never turn one up. The test is here rather than on {@link Task}
 * because a todo is the only kind that takes the inherited answer, and a
 * subclass that started overriding it would be caught here.
 *
 * <p>The marking tests cover {@link Task#markDone()} and
 * {@link Task#unmarkDone()} on the simplest kind that has them, since neither
 * behaves differently for a deadline or an event.
 */
public class ToDoTest {
    @Test
    public void occursOn_anyDay_false() {
        ToDo todo = new ToDo("read book");
        assertFalse(todo.occursOn(LocalDate.of(2019, 10, 15)));
        assertFalse(todo.occursOn(LocalDate.of(1999, 1, 1)));
    }

    @Test
    public void toString_newTask_statusBoxEmpty() {
        assertEquals("[T][ ] read book", new ToDo("read book").toString());
    }

    @Test
    public void markDone_newTask_statusBoxMarked() {
        ToDo todo = new ToDo("read book");
        todo.markDone();
        assertEquals("[T][X] read book", todo.toString());
    }

    @Test
    public void unmarkDone_doneTask_statusBoxEmptyAgain() {
        ToDo todo = new ToDo("read book");
        todo.markDone();
        todo.unmarkDone();
        assertEquals("[T][ ] read book", todo.toString());
    }

    @Test
    public void toFileFields_notDone_typeStatusAndDescription() {
        assertEquals(List.of("T", "0", "read book"), new ToDo("read book").toFileFields());
    }

    @Test
    public void toFileFields_done_statusIsOne() {
        ToDo todo = new ToDo("read book");
        todo.markDone();
        assertEquals(List.of("T", "1", "read book"), todo.toFileFields());
    }
}
