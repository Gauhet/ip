import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Turns the text of a date into a {@link LocalDate}, in the one format the
 * program accepts, and explains a refusal in terms the user can act on.
 *
 * <p>Reading a date is wanted in two places: the commands the user types, and
 * the lines of the save file. Doing it here rather than in each of them means
 * the two cannot come to disagree about what counts as a date, which is a
 * disagreement that would show up as a task that saves but will not load.
 *
 * <p>A refusal tells the two ways a date can be wrong apart, because the user
 * can do something different about each. Text that is not shaped like a date at
 * all was written in the wrong format, so the reply names the right one. Text
 * that is shaped like a date but names no real day is a wrong number rather
 * than a wrong format, and repeating the format would not help.
 */
public class DateParser {
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
