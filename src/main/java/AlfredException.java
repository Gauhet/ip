/**
 * A mistake in what the user typed, such as an unrecognised command or one
 * missing part of its input. The message is shown to the user word for word.
 *
 * <p>Checked, so the compiler insists each one is caught and answered. Bugs
 * stay unchecked and still crash rather than becoming a polite message.
 */
public class AlfredException extends Exception {
    /**
     * @param message the explanation, phrased as Alfred would say it
     */
    public AlfredException(String message) {
        super(message);
    }
}
