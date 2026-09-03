package alfred;

import alfred.command.Command;
import alfred.command.ExitCommand;
import alfred.task.TaskList;

/**
 * Alfred the Butler: a personal chatbot that keeps a list of tasks, taking one
 * command per line until the user says {@code bye} or the input runs out. The
 * list is saved to disk after every change and read back at startup, so it
 * outlives a single run.
 *
 * <p>This class holds the command loop and nothing else that can be given a
 * home of its own: what the user sees and types is {@link Ui}'s, what a typed
 * line means is {@link Parser}'s, what to do about it is the {@link Command}'s,
 * the tasks themselves are {@link TaskList}'s, and what is written to and read
 * from disk is {@link Storage}'s.
 *
 * <p>A run is an object rather than a static method, so that the parts it works
 * with are fields set up once instead of locals threaded through every call. It
 * also means a second chatbot, saving somewhere else, is another instance
 * rather than another program.
 *
 * <p>There are two ways in. {@link #run()} is the console one, which reads a
 * line, answers it, and goes round again until told to stop. The window uses
 * {@link #getGreeting()} and {@link #getResponse(String)} instead, one line at
 * a time, because a window brings its own loop and cannot lend it out. Both
 * reach the commands by the same path, so neither can do something the other
 * cannot.
 */
public class AlfredTheButler {
    /**
     * Where a normal run keeps its tasks. Named here rather than inside
     * {@link Storage}, so that the class that does the saving does not also
     * decide where to save.
     */
    private static final String SAVE_FILE = "data/alfred.txt";

    /** Everything the user sees and types. */
    private final Ui ui;

    /** The save file this run reads from and writes to. */
    private final Storage storage;

    /**
     * The tasks. Not final, because the list read back at startup replaces the
     * empty one this starts with.
     */
    private TaskList tasks;

    /**
     * Sets up a run that keeps its tasks in one named file.
     *
     * <p>Nothing is read here. Loading is left to {@link #run()} because it has
     * something to say to the user — how many tasks came back, or that the file
     * could not be read — and saying it from a constructor would put it before
     * the greeting, which is not the order a conversation goes in.
     *
     * @param filePath where to keep the tasks, such as {@code data/alfred.txt}
     */
    public AlfredTheButler(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        tasks = new TaskList();
    }

    /**
     * Sets up a run that keeps its tasks where a normal run keeps them.
     *
     * <p>The window creates a chatbot without saying where to save, since it
     * has no more reason to choose than {@link #main(String[])} has.
     */
    public AlfredTheButler() {
        this(SAVE_FILE);
    }

    /**
     * Greets the user, then handles one command per line until {@code bye} or
     * the end of the input: {@code list}, {@code on <date>},
     * {@code mark <number>}, {@code unmark <number>}, {@code delete <number>},
     * and the three that add a task, {@code todo}, {@code deadline}, and
     * {@code event}. Any other word is refused rather than guessed at.
     *
     * <p>What each of those does is the command's own business, not this
     * method's: a line is turned into a {@link Command} and carried out, so
     * this loop is the same however many commands there come to be.
     *
     * <p>Commands run inside a {@code try} so that a mistake in what was typed
     * becomes an ordinary reply and the loop carries on. Catching in one place
     * lets each command throw its own message without knowing how it is
     * printed.
     */
    public void run() {
        boolean isExit = false;

        ui.showWelcome();
        restoreTasks();
        while (!isExit) {
            try {
                // End of input is treated as `bye`, so that a piped or
                // redirected session that simply runs out of lines finishes the
                // same way a typed one does instead of failing to read a line
                // that is not there.
                Command command = ui.hasNextCommand()
                        ? Parser.parse(ui.readCommand())
                        : new ExitCommand();
                command.execute(tasks, ui, storage);
                // Asked after the command has run, and skipped if it threw, so
                // that a command which could not be carried out cannot end the
                // session on its way out.
                isExit = command.isExit();
            } catch (AlfredException e) {
                ui.showError(e.getMessage());
            } catch (RuntimeException e) {
                // A safety net, not a substitute for handling: everything the
                // user can get wrong is refused above with a message of its own.
                // This one catches the mistakes in this program, so that a bug
                // in one command costs that command rather than the session and
                // the tasks typed since the last save.
                ui.showInternalError(e);
            }
        }
    }

    /**
     * Returns Alfred's opening words, and reads back the tasks the last run
     * left behind.
     *
     * <p>What {@link #run()} does before its loop, for a caller that has no
     * loop: the window shows this as Alfred's first message. Loading is part of
     * it because the answer says how it went — how many tasks came back, or
     * that the file could not be read — and because a window that had not
     * loaded would answer the first command against an empty list and save that
     * over the tasks on disk.
     *
     * @return the greeting, and what came of reading the save file.
     */
    public String getGreeting() {
        ui.startCapturing();
        ui.showWelcome();
        restoreTasks();
        return ui.stopCapturing();
    }

    /**
     * Returns what Alfred says back to one line sent from the window.
     *
     * <p>The line is carried out exactly as a typed one is, and what would have
     * been printed is handed back instead. The commands are reached the same
     * way and mistakes are answered the same way, so the window and the console
     * cannot come to disagree about what a command does.
     *
     * <p>What this does not do is end the session. {@link #run()} stops when a
     * command says to; there is no loop here to stop, so {@code bye} is
     * answered like any other command and the window is the user's to close.
     *
     * @param input The line the user typed.
     * @return the reply, as the console would have printed it, one line per
     *     line.
     */
    public String getResponse(String input) {
        ui.startCapturing();
        try {
            // Trimmed here for the same reason the console trims what it
            // reads: the parser is given a trimmed line, and a window hands
            // over the text field exactly as it stands. Without this, a stray
            // space before a command would leave it unrecognized in the window
            // but not at the console.
            Command command = Parser.parse(input.trim());
            command.execute(tasks, ui, storage);
        } catch (AlfredException e) {
            ui.showError(e.getMessage());
        } catch (RuntimeException e) {
            // The same safety net the loop has, and for the same reason: a bug
            // in one command costs that command rather than the session.
            ui.showInternalError(e);
        }
        return ui.stopCapturing();
    }

    /**
     * Reads back the tasks the last run left behind, and says what came of it.
     *
     * <p>This has a {@code try} of its own because it runs before the loop and
     * so cannot rely on the loop's catch. A file that cannot be read leaves the
     * empty list in place rather than stopping the program: the user can still
     * work, and the first save will write a file that can be read.
     */
    private void restoreTasks() {
        try {
            Storage.LoadResult loaded = storage.load();
            tasks = new TaskList(loaded.tasks());
            // Said only when there is something to say. On a first run there is
            // no file yet, and announcing that nothing came back would be noise.
            if (!tasks.isEmpty()) {
                ui.showLoaded(tasks.size());
            }
            // Warned about separately, and even when nothing else was restored,
            // because the damaged lines are dropped from the file as soon as the
            // list next changes.
            if (loaded.skippedLines() > 0) {
                ui.showSkippedLines(loaded.skippedLines());
            }
        } catch (AlfredException e) {
            ui.showError(e.getMessage());
        }
    }

    /**
     * Starts one run of the chatbot, saving to the usual file.
     *
     * @param args ignored; the save file is not yet something to choose
     */
    public static void main(String[] args) {
        new AlfredTheButler(SAVE_FILE).run();
    }
}
