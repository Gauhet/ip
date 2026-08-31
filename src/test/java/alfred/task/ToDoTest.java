package alfred.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 *
 * <p>The keyword tests cover {@link Task#matches(String)} for the same reason:
 * it reads only the description, which every kind has and none of them stores
 * differently. What it does <em>not</em> read is checked on a deadline, in
 * {@link DeadlineTest}, since a todo has no date to leave out of the search.
 */
public class ToDoTest {
    @Test
    public void occursOn_anyDay_false() {
        ToDo todo = new ToDo("read book");
        assertFalse(todo.occursOn(LocalDate.of(2019, 10, 15)));
        assertFalse(todo.occursOn(LocalDate.of(1999, 1, 1)));
    }

    @Test
    public void matches_wordInDescription_true() {
        assertTrue(new ToDo("read book").matches("book"));
    }

    @Test
    public void matches_wordNotInDescription_false() {
        assertFalse(new ToDo("read book").matches("meeting"));
    }

    @Test
    public void matches_differentCase_true() {
        // A search is for the word, not for one spelling of it, so neither a
        // capitalized keyword nor a capitalized description should hide a match.
        assertTrue(new ToDo("read book").matches("BOOK"));
        assertTrue(new ToDo("Read Book").matches("book"));
    }

    @Test
    public void matches_partOfALongerWord_true() {
        // The rule is containment, not whole words: find book turns up bookshop.
        assertTrue(new ToDo("visit bookshop").matches("book"));
    }

    @Test
    public void matches_phraseSpanningWords_true() {
        assertTrue(new ToDo("read book tonight").matches("read book"));
    }

    @Test
    public void matches_wordsPresentButNotAdjacent_false() {
        // The keyword is one piece of text, so the words have to appear
        // together and in order rather than merely both be present.
        assertFalse(new ToDo("read a book").matches("read book"));
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
