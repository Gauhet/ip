package alfred;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Dates}.
 *
 * <p>The {@link Dates#parse(String)} tests are grouped by the three things that
 * method can do, because those are the three things a caller has to be able to
 * rely on: it returns the day the text names, it refuses text that is not
 * shaped like a date, or it refuses text that is shaped like one but names no
 * real day. The last two are checked by their message and not only by the
 * exception type, since both throw an {@link AlfredException} and telling them
 * apart is the whole point of the distinction.
 *
 * <p>The {@link Dates#format(LocalDate)} tests cover the form a date takes when
 * it is shown, and pin the one property of it that is a decision rather than an
 * accident: the month is named in English whatever locale the machine is set
 * to. Left to the default locale, the same task would read differently from one
 * machine to the next, so a test guards it.
 *
 * <p>The test class sits in package {@code alfred} because {@code parse} is
 * package-private. That is deliberate: the visibility stays as narrow as the
 * program needs it, and the test reaches the method by being a neighbor rather
 * than by the method being opened up for it.
 */
public class DatesTest {
    @Test
    public void parse_validDate_dateReturned() throws AlfredException {
        assertEquals(LocalDate.of(2019, 10, 15), Dates.parse("2019-10-15"));
    }

    @Test
    public void parse_leapDayInLeapYear_dateReturned() throws AlfredException {
        assertEquals(LocalDate.of(2020, 2, 29), Dates.parse("2020-02-29"));
    }

    @Test
    public void parse_yearBoundaries_dateReturned() throws AlfredException {
        assertEquals(LocalDate.of(2019, 1, 1), Dates.parse("2019-01-01"));
        assertEquals(LocalDate.of(2019, 12, 31), Dates.parse("2019-12-31"));
    }

    @Test
    public void parse_emptyText_formatExceptionThrown() {
        assertFormatRefused("");
    }

    @Test
    public void parse_notADateAtAll_formatExceptionThrown() {
        assertFormatRefused("Sunday");
    }

    @Test
    public void parse_singleDigitMonth_formatExceptionThrown() {
        assertFormatRefused("2019-1-15");
    }

    @Test
    public void parse_singleDigitDay_formatExceptionThrown() {
        assertFormatRefused("2019-10-5");
    }

    @Test
    public void parse_dayFirstOrder_formatExceptionThrown() {
        assertFormatRefused("15-10-2019");
    }

    @Test
    public void parse_slashSeparators_formatExceptionThrown() {
        assertFormatRefused("2019/10/15");
    }

    @Test
    public void parse_noSeparators_formatExceptionThrown() {
        assertFormatRefused("20191015");
    }

    @Test
    public void parse_trailingText_formatExceptionThrown() {
        // A date followed by anything else is still the wrong format: the shape
        // has to account for the whole text, not merely appear somewhere in it.
        assertFormatRefused("2019-10-15 please");
    }

    @Test
    public void parse_leapDayInCommonYear_nonexistentDateExceptionThrown() {
        assertNonexistentDateRefused("2019-02-29");
    }

    @Test
    public void parse_dayAfterEndOfMonth_nonexistentDateExceptionThrown() {
        assertNonexistentDateRefused("2019-04-31");
    }

    @Test
    public void parse_monthAboveTwelve_nonexistentDateExceptionThrown() {
        assertNonexistentDateRefused("2019-13-01");
    }

    @Test
    public void parse_zeroMonth_nonexistentDateExceptionThrown() {
        assertNonexistentDateRefused("2019-00-15");
    }

    @Test
    public void parse_zeroDay_nonexistentDateExceptionThrown() {
        assertNonexistentDateRefused("2019-10-00");
    }

    @Test
    public void format_ordinaryDate_monthNamedAndDayPadded() {
        assertEquals("Oct 15 2019", Dates.format(LocalDate.of(2019, 10, 15)));
    }

    @Test
    public void format_singleDigitDay_dayPaddedToTwoDigits() {
        assertEquals("Jan 01 2019", Dates.format(LocalDate.of(2019, 1, 1)));
    }

    @Test
    public void format_lastDayOfYear_displayFormReturned() {
        assertEquals("Dec 31 2019", Dates.format(LocalDate.of(2019, 12, 31)));
    }

    @Test
    public void format_leapDay_displayFormReturned() {
        assertEquals("Feb 29 2020", Dates.format(LocalDate.of(2020, 2, 29)));
    }

    @Test
    public void format_nonEnglishDefaultLocale_monthNamedInEnglish() {
        Locale original = Locale.getDefault();
        try {
            // German would name this month Okt, so the assertion below fails
            // if the display format ever stops pinning its locale.
            Locale.setDefault(Locale.GERMANY);
            assertEquals("Oct 15 2019", Dates.format(LocalDate.of(2019, 10, 15)));
        } finally {
            // Restored whatever the assertion did, so that a failure here
            // cannot leak a locale into the tests that run after it.
            Locale.setDefault(original);
        }
    }

    /**
     * Checks that the text is refused for not being in the accepted format.
     *
     * @param text the date as it was written.
     */
    private void assertFormatRefused(String text) {
        AlfredException e = assertThrows(AlfredException.class, () -> Dates.parse(text));
        assertEquals("I don't know '" + text + "' as a date, sir. Do use yyyy-mm-dd, as in 2019-10-15.",
                e.getMessage());
    }

    /**
     * Checks that the text is refused for naming a day that does not exist,
     * rather than for being in the wrong format.
     *
     * @param text the date as it was written.
     */
    private void assertNonexistentDateRefused(String text) {
        AlfredException e = assertThrows(AlfredException.class, () -> Dates.parse(text));
        assertEquals("There is no such date as '" + text + "', sir. Do check the day and the month.",
                e.getMessage());
    }
}
