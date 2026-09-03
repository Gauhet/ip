package alfred;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Converts between a {@link LocalDate} and the two forms a date takes as text.
 *
 * <p>A date is read as {@code yyyy-mm-dd}, the form the user types and the save
 * file holds, and shown as {@code MMM dd yyyy}, so that a named month cannot be
 * misread the way {@code 10-11} can. Both live here so that the commands and
 * the save file cannot disagree about what counts as a date.
 */
public class Dates {
    /** The format a date is typed and saved in, spelled the way a person would write it. */
    private static final String DATE_INPUT_FORMAT = "yyyy-mm-dd";

    /** A date in that format, shown alongside it because an example is easier to copy. */
    private static final String DATE_INPUT_EXAMPLE = "2019-10-15";

    /**
     * The shape of that format: four digits, then two, then two.
     *
     * <p>Matching says only that the text is shaped like a date, not that it
     * names a day that exists: {@code 2019-02-30} matches and {@code Sunday}
     * does not. That is the line between the two messages below.
     */
    private static final Pattern DATE_INPUT_SHAPE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    /**
     * The format a date is shown in, for example {@code Oct 15 2019}.
     *
     * <p>The locale is pinned to English so the month name does not change with
     * the computer the program runs on.
     */
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    /** Prevents this class from being instantiated. */
    private Dates() {
    }

    /**
     * Writes a date the way it is shown to the user.
     *
     * <p>This is not the form {@link #parse(String)} reads. Nothing written by
     * this method is ever saved or read back.
     *
     * @param date the day to write.
     * @return the date in the reader's form, such as {@code Oct 15 2019}.
     */
    public static String format(LocalDate date) {
        return DATE_DISPLAY_FORMAT.format(date);
    }

    /**
     * Reads a date written in the accepted format.
     *
     * @param text the date as it was written, already trimmed.
     * @return the day that text names.
     * @throws AlfredException if the text is not in the accepted format, or is
     *         in that format but names a day that does not exist.
     */
    static LocalDate parse(String text) throws AlfredException {
        if (!DATE_INPUT_SHAPE.matcher(text).matches()) {
            throw new AlfredException("I don't know '" + text + "' as a date, sir. Do use "
                    + DATE_INPUT_FORMAT + ", as in " + DATE_INPUT_EXAMPLE + ".");
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            // Shaped like a date, so the day itself is what is wrong.
            throw new AlfredException("There is no such date as '" + text
                    + "', sir. Do check the day and the month.");
        }
    }
}
