package alfred.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Deadline}, which falls on the single day it is due.
 *
 * <p>The two forms its date takes are both checked, because they are
 * deliberately different and each has a reader that depends on it: the display
 * form is what the user reads, and the {@code yyyy-mm-dd} form is what the save
 * file holds and reads back. A change that made the saved field match the shown
 * one would look tidy and would stop every existing save file loading.
 */
public class DeadlineTest {
    private static final LocalDate OCT_14 = LocalDate.of(2019, 10, 14);

    private static final LocalDate OCT_15 = LocalDate.of(2019, 10, 15);

    private static final LocalDate OCT_16 = LocalDate.of(2019, 10, 16);

    @Test
    public void occursOn_dayItIsDue_true() {
        assertTrue(createDeadline().occursOn(OCT_15));
    }

    @Test
    public void occursOn_dayBefore_false() {
        assertFalse(createDeadline().occursOn(OCT_14));
    }

    @Test
    public void occursOn_dayAfter_false() {
        assertFalse(createDeadline().occursOn(OCT_16));
    }

    @Test
    public void toString_notDone_dueDateShownInReadersForm() {
        assertEquals("[D][ ] return book (by: Oct 15 2019)", createDeadline().toString());
    }

    @Test
    public void toString_done_statusBoxMarked() {
        Deadline deadline = createDeadline();
        deadline.markDone();
        assertEquals("[D][X] return book (by: Oct 15 2019)", deadline.toString());
    }

    @Test
    public void toFileFields_notDone_dueDateSavedInReadableBackForm() {
        assertEquals(List.of("D", "0", "return book", "2019-10-15"), createDeadline().toFileFields());
    }

    @Test
    public void toFileFields_done_statusIsOne() {
        Deadline deadline = createDeadline();
        deadline.markDone();
        assertEquals("1", deadline.toFileFields().get(1));
    }

    /**
     * Returns a deadline due on October 15, 2019.
     *
     * @return a fresh deadline, so that marking one test's copy cannot affect another
     */
    private Deadline createDeadline() {
        return new Deadline("return book", OCT_15);
    }
}
