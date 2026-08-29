import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Converts between a {@link LocalDate} and the two forms a date takes as text,
 * and explains a refusal in terms the user can act on.
 *
 * <p>The two forms answer to different readers. A date is <em>read</em> as
 * {@code yyyy-mm-dd}, the form the user types and the save file holds, chosen
 * because it reads back exactly. A date is <em>shown</em> as
 * {@code MMM dd yyyy}, because a person reading the list wants a month named
 * rather than numbered, and because a named month cannot be misread the way
 * {@code 10-11} can. Keeping a date as a {@code LocalDate} rather than as text
 * is what allows both at once.
 *
 * <p>Both live here, rather than beside the code that happens to need each, so
 * that which form belongs where is settled in one place. Reading is wanted by
 * the commands the user types and by the lines of the save file, and those two
 * coming to disagree about what counts as a date would show up as a task that
 * saves but will not load.
 *
 * <p>A refusal tells the two ways a date can be wrong apart, because the user
 * can do something different about each. Text that is not shaped like a date at
 * all was written in the wrong format, so the reply names the right one. Text
 * that is shaped like a date but names no real day is a wrong number rather
 * than a wrong format, and repeating the format would not help.
 */
public class Dates {
    /** The format a date is written in, spelled the way a person would write it. */
    private static final String FORMAT = "yyyy-mm-dd";

    /** A date in that format, shown alongside it because an example is easier to copy than a pattern. */
    private static final String EXAMPLE = "2019-10-15";

    /**
     * The shape of the format: four digits, then two, then two, separated by
     * hyphens.
     *
     * <p>Matching says only that the text is shaped like a date, not that it
     * names a day that exists. That is the point: {@code 2019-02-30} matches
     * and {@code Sunday} does not, which is exactly the line between the two
     * messages below.
     */
    private static final Pattern SHAPE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    /**
     * How a date is written when it is shown to the user, for example
     * {@code Oct 15 2019}.
     *
     * <p>The locale is pinned to English so the month name does not change with
     * the computer the program runs on. Left to the default locale, the same
     * task would show {@code Oct} on one machine and {@code okt} on another,
     * and the text UI tests would pass or fail by machine rather than by
     * behavior.
     */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    /**
     * Writes a date the way it is shown to the user.
     *
     * <p>This is not the form {@link #parse(String)} reads, and deliberately
     * so. Nothing written by this method is ever saved or read back.
     *
     * @param date the day to write
     * @return the date in the reader's form, such as {@code Oct 15 2019}
     */
    public static String format(LocalDate date) {
        return DISPLAY_FORMAT.format(date);
    }

    /**
     * Reads a date written in the accepted format.
     *
     * @param text the date as it was written, already trimmed
     * @return the day that text names
     * @throws AlfredException if the text is not in the accepted format, or is
     *         in that format but names a day that does not exist
     */
    public static LocalDate parse(String text) throws AlfredException {
        if (!SHAPE.matcher(text).matches()) {
            throw new AlfredException("I don't know '" + text + "' as a date, sir. Do use "
                    + FORMAT + ", as in " + EXAMPLE + ".");
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            // Shaped like a date, so the format is not what is wrong. What is
            // left is the day itself: a 30th of February, or a 13th month.
            throw new AlfredException("There is no such date as '" + text
                    + "', sir. Do check the day and the month.");
        }
    }
}
