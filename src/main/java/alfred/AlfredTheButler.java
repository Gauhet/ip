package alfred;

import alfred.command.Command;
import alfred.command.ExitCommand;
import alfred.task.TaskList;

/**
 * Alfred the Butler: a personal chatbot that keeps a list of tasks, taking one
 * command per line until the user says {@code bye} or the input runs out. The
 * list is saved to disk after every change and read back at startup.
 *
 * <p>This class holds the command loop and nothing else that can be given a
 * home of its own: {@link Ui} owns what the user sees and types, {@link Parser}
 * what a typed line means, the {@link Command} what to do about it,
 * {@link TaskList} the tasks, and {@link Storage} the save file.
 *
 * <p>There are two ways in. {@link #run()} is the console one. The window uses
 * {@link #getGreeting()} and {@link #getResponse(String)} instead, one line at
 * a time. Both reach the commands by the same path.
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

    /** The tasks. Not final, because the list read back at startup replaces the empty one. */
    private TaskList tasks;

    /**
     * The kind of the command last carried out, named by its class, or null
     * before any has been. Kept so that the window can color a reply by the
     * kind of command it answers.
     */
    private String commandType;

    /**
     * Sets up a run that keeps its tasks in one named file.
     *
     * <p>Nothing is read here: loading has something to say to the user, and it
     * belongs after the greeting.
     *
     * @param filePath where to keep the tasks, such as {@code data/alfred.txt}.
     */
    public AlfredTheButler(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        tasks = new TaskList();
    }

    /** Sets up a run that keeps its tasks where a normal run keeps them. */
    public AlfredTheButler() {
        this(SAVE_FILE);
    }

    /**
     * Greets the user, then handles one command per line until {@code bye} or
     * the end of the input: {@code list}, {@code find <keyword>},
     * {@code on <date>}, {@code mark <number>}, {@code unmark <number>},
     * {@code delete <number>}, and the three that add a task, {@code todo},
     * {@code deadline}, and {@code event}. Any other word is refused rather
     * than guessed at.
     *
     * <p>Commands run inside a {@code try} so that a mistake in what was typed
     * becomes an ordinary reply.
     */
    public void run() {
        boolean isExit = false;

        ui.showWelcome();
        restoreTasks();
        while (!isExit) {
            try {
                // End of input is treated as `bye`, so that a piped session that
                // runs out of lines finishes the same way a typed one does.
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
                // A safety net for the mistakes in this program, so that a bug
                // in one command costs that command rather than the session.
                ui.showInternalError(e);
            }
        }
    }

    /**
     * Returns Alfred's opening words, and reads back the tasks the last run
     * left behind.
     *
     * <p>What {@link #run()} does before its loop, for a caller that has no
     * loop. Loading is part of it because a window that had not loaded would
     * save an empty list over the tasks on disk.
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
     * <p>What this does not do is end the session: there is no loop here to
     * stop, so {@code bye} is answered like any other command.
     *
     * @param input the line the user typed.
     * @return the reply, as the console would have printed it, one line per line.
     */
    public String getResponse(String input) {
        ui.startCapturing();
        try {
            // Trimmed for the same reason the console trims what it reads: a
            // window hands over the text field exactly as it stands.
            Command command = Parser.parse(input.trim());
            command.execute(tasks, ui, storage);
            commandType = command.getClass().getSimpleName();
        } catch (AlfredException e) {
            // Forgotten rather than left as it was, so that a refusal is not
            // colored as though the command before it had just run again.
            commandType = null;
            ui.showError(e.getMessage());
        } catch (RuntimeException e) {
            commandType = null;
            ui.showInternalError(e);
        }
        return ui.stopCapturing();
    }

    /**
     * Returns the kind of the command last carried out, named by its class.
     *
     * @return the class name of the last command, or null before one has been
     *     carried out.
     */
    public String getCommandType() {
        return commandType;
    }

    /**
     * Reads back the tasks the last run left behind, and says what came of it.
     *
     * <p>This has a {@code try} of its own because it runs before the loop.
     */
    private void restoreTasks() {
        try {
            Storage.LoadResult loadResult = storage.load();
            tasks = new TaskList(loadResult.tasks());
            // Said only when there is something to say. On a first run there is
            // no file yet, and announcing that nothing came back would be noise.
            if (!tasks.isEmpty()) {
                ui.showLoaded(tasks.size());
            }
            // Warned about separately, and even when nothing else was restored,
            // because the damaged lines are dropped as soon as the list changes.
            if (loadResult.skippedLines() > 0) {
                ui.showSkippedLines(loadResult.skippedLines());
            }
        } catch (AlfredException e) {
            ui.showError(e.getMessage());
        }
    }

    /**
     * Starts one run of the chatbot, saving to the usual file.
     *
     * @param args ignored; the save file is not yet something to choose.
     */
    public static void main(String[] args) {
        new AlfredTheButler(SAVE_FILE).run();
    }
}
