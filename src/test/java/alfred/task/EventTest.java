package alfred.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Event}, whose {@link Event#occursOn(LocalDate)} is the only task
 * kind that answers for a stretch of days rather than one.
 *
 * <p>That range is where the tests are concentrated. Both ends count as part of
 * the event, so both are checked, along with the days just outside them: an
 * event running from Monday to Wednesday is one the user has on Monday, and a
 * range that quietly excluded an end would hide a task from the {@code on}
 * command on the very day it starts.
 */
public class EventTest {
    private static final LocalDate DEC_1 = LocalDate.of(2019, 12, 1);

    private static final LocalDate DEC_2 = LocalDate.of(2019, 12, 2);

    private static final LocalDate DEC_3 = LocalDate.of(2019, 12, 3);

    private static final LocalDate DEC_4 = LocalDate.of(2019, 12, 4);

    private static final LocalDate DEC_5 = LocalDate.of(2019, 12, 5);

    @Test
    public void occursOn_dayBeforeItStarts_false() {
        assertFalse(createThreeDayEvent().occursOn(DEC_1));
    }

    @Test
    public void occursOn_dayItStarts_true() {
        assertTrue(createThreeDayEvent().occursOn(DEC_2));
    }

    @Test
    public void occursOn_dayInTheMiddle_true() {
        assertTrue(createThreeDayEvent().occursOn(DEC_3));
    }

    @Test
    public void occursOn_dayItEnds_true() {
        assertTrue(createThreeDayEvent().occursOn(DEC_4));
    }

    @Test
    public void occursOn_dayAfterItEnds_false() {
        assertFalse(createThreeDayEvent().occursOn(DEC_5));
    }

    @Test
    public void occursOn_eventLastingOneDay_trueOnThatDayAlone() {
        Event event = new Event("sports day", DEC_2, DEC_2);
        assertTrue(event.occursOn(DEC_2));
        assertFalse(event.occursOn(DEC_1));
        assertFalse(event.occursOn(DEC_3));
    }

    @Test
    public void toString_notDone_datesShownInReadersForm() {
        assertEquals("[E][ ] project meeting (from: Dec 02 2019 to: Dec 04 2019)",
                createThreeDayEvent().toString());
    }

    @Test
    public void toString_done_statusBoxMarked() {
        Event event = createThreeDayEvent();
        event.markDone();
        assertEquals("[E][X] project meeting (from: Dec 02 2019 to: Dec 04 2019)", event.toString());
    }

    @Test
    public void toFileFields_notDone_typeStatusDescriptionAndBothDates() {
        assertEquals(List.of("E", "0", "project meeting", "2019-12-02", "2019-12-04"),
                createThreeDayEvent().toFileFields());
    }

    @Test
    public void toFileFields_done_statusIsOne() {
        Event event = createThreeDayEvent();
        event.markDone();
        assertEquals("1", event.toFileFields().get(1));
    }

    /**
     * Returns an event running from December 2 to December 4, the fixture the
     * range tests are built around.
     *
     * @return a fresh event, so that marking one test's copy cannot affect another
     */
    private Event createThreeDayEvent() {
        return new Event("project meeting", DEC_2, DEC_4);
    }
}
