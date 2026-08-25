/**
 * A command the user can type, paired with the keyword that invokes it.
 */
public enum Command {
    BYE("bye"),
    LIST("list"),
    TODO("todo"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    DEADLINE("deadline"),
    EVENT("event");

    /** The word the user types to invoke this command. */
    private final String keyword;

    Command(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the command that the given keyword invokes.
     *
     * @param keyword the first word of the line the user typed
     * @return the command that keyword names
     * @throws AlfredException if no command has that keyword
     */
    public static Command fromKeyword(String keyword) throws AlfredException {
        for (Command command : values()) {
            if (command.keyword.equals(keyword)) {
                return command;
            }
        }
        // Only the keyword is quoted back. Repeating the whole line would bury
        // the one word that was not understood.
        throw new AlfredException("I'm afraid I don't know '" + keyword + "', sir.");
    }
}
