package alfred;

/**
 * Something the program can tell the user about and carry on from: a mistake in
 * what they typed, such as an unrecognized command or one missing part of its
 * input, or a save file that cannot be read, written, or made sense of. The
 * message is shown to the user word for word.
 *
 * <p>Checked, so the compiler insists each one is caught and answered.
 */
public class AlfredException extends Exception {
    /**
     * Creates an exception carrying the message to show the user.
     *
     * @param message the explanation, phrased as Alfred would say it
     */
    public AlfredException(String message) {
        super(message);
    }
}
