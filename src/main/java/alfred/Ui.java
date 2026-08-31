package alfred;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

import alfred.task.Task;
import alfred.task.TaskList;

/**
 * Everything the user sees and types: the banner, the reply blocks, and the
 * line-by-line reading of commands.
 *
 * <p>Gathering all of it here keeps the layout of a reply — the dividers, the
 * indent, the blank line after — in one place, so that the rest of the program
 * can say <em>what</em> to tell the user without also saying how it looks on
 * screen. Changing the shape of a reply is then one edit here rather than one
 * per message.
 *
 * <p>Reading belongs with writing for the same reason: both ends of the
 * conversation go through {@code System.in} and {@code System.out}, so a later
 * move to another kind of interface, such as a window, is a change to this
 * class alone.
 *
 * <p>This class holds a {@link Scanner} over standard input, so one instance is
 * made and used for a whole run rather than one per command.
 */
public class Ui {
    /** Horizontal rule that opens and closes every message block. */
    private static final String DIVIDER = "    " + "_".repeat(60);

    /** Indent for message text, one space deeper than the divider. */
    private static final String INDENT = "     ";

    /** Extra indent for a line that belongs under the one above it, such as a marked task. */
    private static final String SUB_INDENT = "  ";

    /** Name the chatbot introduces itself by. */
    private static final String NAME = "AlfredTheButler";

    /** ASCII-art logo shown once at startup. */
    private static final String BANNER =
            "            _     _      _____  ____   _____  ____\n"
            + "           / \\   | |    |  ___||  _ \\ | ____||  _ \\\n"
            + "          / _ \\  | |    | |_   | |_) ||  _|  | | | |\n"
            + "         / ___ \\ | |___ |  _|  |  _ < | |___ | |_| |\n"
            + "        /_/   \\_\\|_____||_|    |_| \\_\\|_____||____/\n"
            + "                    P E N N Y W O R T H\n"
            + "\n"
            + "      Butler to the Wayne family  --  At your service";

    /** Where the user's commands are read from, kept open for the whole run. */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Says whether there is another command to read.
     *
     * <p>Asked before every read, because input that has run out — a piped or
     * redirected session, rather than a typed one — is a normal way for a run
     * to end and not a failure.
     *
     * @return true if a line is waiting, false at the end of the input
     */
    boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command line, trimmed so that a stray space around a
     * command does not stop it being recognized.
     *
     * @return the line the user typed, without leading or trailing spaces
     */
    String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Prints the banner and the welcome message. */
    void showWelcome() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println(INDENT + "Hello! I'm " + NAME);
        System.out.println(INDENT + "What can I do for you?");
        System.out.println(DIVIDER);
        System.out.println();
    }

    /** Prints the parting message, the last thing any run prints. */
    public void showFarewell() {
        reply("Bye. Hope to see you again soon!");
    }

    /**
     * Reports that tasks were restored from the save file.
     *
     * @param taskCount how many tasks came back
     */
    void showLoaded(int taskCount) {
        reply("I've brought back " + describeCount(taskCount, "task") + " from last time, sir.");
    }

    /**
     * Warns that part of the save file could not be understood.
     *
     * @param skippedLines how many lines were left out
     */
    void showSkippedLines(int skippedLines) {
        reply("I could not make sense of " + describeCount(skippedLines, "line")
                        + " in your saved tasks, sir.",
                "I have left them out, and they will be gone once the list changes.");
    }

    /**
     * Reports something the user can put right, such as a mistyped command.
     *
     * @param message the refusal, written for the person who typed the command
     */
    void showError(String message) {
        reply(message);
    }

    /**
     * Reports a fault in the program itself, rather than in what was typed.
     *
     * <p>The exception is shown because a fault that cannot be named cannot be
     * reported, and the reassurance is added because the user has no way of
     * knowing whether their tasks survived.
     *
     * @param e the fault that escaped the command that caused it
     */
    void showInternalError(RuntimeException e) {
        reply("Something went wrong on my end, sir: " + e,
                "Your tasks are unharmed. Do carry on.");
    }

    /**
     * Confirms that a task has been stored, showing the task itself so the
     * user can see how it was understood.
     *
     * @param task the task that was just added
     * @param taskCount how many tasks are stored now that it has been added
     */
    public void showAdded(Task task, int taskCount) {
        reply("Got it. I've added this task:",
                SUB_INDENT + task,
                "Now you have " + describeCount(taskCount, "task") + " in the list.");
    }

    /**
     * Confirms that a task has been dropped from the list.
     *
     * @param task the task that was removed
     * @param taskCount how many tasks are left
     */
    public void showRemoved(Task task, int taskCount) {
        reply("Noted. I've removed this task:",
                SUB_INDENT + task,
                "Now you have " + describeCount(taskCount, "task") + " in the list.");
    }

    /**
     * Confirms that a task is now done, showing it with its new mark.
     *
     * @param task the task that was marked
     */
    public void showMarked(Task task) {
        reply("Nice! I've marked this task as done:", SUB_INDENT + task);
    }

    /**
     * Confirms that a task is no longer done, showing it with its new mark.
     *
     * @param task the task that was unmarked
     */
    public void showUnmarked(Task task) {
        reply("OK, I've marked this task as not done yet:", SUB_INDENT + task);
    }

    /**
     * Prints the stored tasks as a numbered list under a heading,
     * numbered from 1.
     *
     * @param tasks the tasks to show, in the order they are stored
     */
    public void showList(TaskList tasks) {
        // One line for the heading, then one per task.
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        reply(lines);
    }

    /**
     * Prints the tasks that fall on one day, in the order they are stored.
     *
     * <p>Each task is numbered by its place in the whole list rather than by
     * its place among the matches. That is what makes the number usable:
     * {@code mark}, {@code unmark}, and {@code delete} count through the whole
     * list, so a number shown here acts on the task it is printed beside.
     * Numbering the matches from 1 would read more tidily and would be a trap,
     * since {@code mark 2} would then act on some task the user is not looking
     * at. The numbers can therefore have gaps, which is the visible sign that
     * they mean something outside this list.
     *
     * <p>An empty result gets a sentence of its own rather than a heading with
     * nothing under it, because "nothing on that day" is the answer to the
     * question, not an empty container.
     *
     * @param tasks every stored task, in the order they are stored
     * @param date the day being asked about
     */
    public void showTasksOn(TaskList tasks, LocalDate date) {
        String when = Dates.format(date);
        List<String> lines = numberMatches(tasks, task -> task.occursOn(date));
        if (lines.isEmpty()) {
            reply("You have nothing on " + when + ", sir.");
            return;
        }
        lines.add(0, "Here is what you have on " + when + ":");
        reply(lines.toArray(new String[0]));
    }

    /**
     * Prints the tasks whose description contains a keyword, in the order they
     * are stored.
     *
     * <p>Numbered by place in the whole list, on the same terms and for the same
     * reason as {@link #showTasksOn(TaskList, LocalDate)}: the number shown is
     * the one {@code mark}, {@code unmark}, and {@code delete} will act on.
     *
     * <p>The keyword is not quoted back in the "nothing found" message, because
     * it is the last thing the user typed and is still on the screen above the
     * answer.
     *
     * @param tasks every stored task, in the order they are stored
     * @param keyword the text being searched for
     */
    public void showMatchingTasks(TaskList tasks, String keyword) {
        List<String> lines = numberMatches(tasks, task -> task.matches(keyword));
        if (lines.isEmpty()) {
            reply("I found no matching tasks, sir.");
            return;
        }
        lines.add(0, "Here are the matching tasks in your list:");
        reply(lines.toArray(new String[0]));
    }

    /**
     * Returns the display lines for the tasks a test accepts, each numbered by
     * its place in the whole list.
     *
     * <p>Shared by the two commands that show part of the list, so that the
     * numbering rule they both depend on is written once and cannot drift apart
     * between them. The test is passed in as a {@link Predicate}, which is the
     * standard way to hand a yes-or-no question to a method; writing the loop
     * out again in each caller would work as well and is what this replaced,
     * at the cost of two copies of the one line that has to be right.
     *
     * @param tasks every stored task, in the order they are stored
     * @param isMatch the test a task has to pass to be shown
     * @return a mutable list of numbered lines, empty if nothing matched
     */
    private static List<String> numberMatches(TaskList tasks, Predicate<Task> isMatch) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (isMatch.test(tasks.get(i))) {
                lines.add((i + 1) + "." + tasks.get(i));
            }
        }
        return lines;
    }

    /**
     * Prints one or more lines inside a divider block, each indented,
     * followed by a blank line.
     *
     * @param lines the lines to display, in order
     */
    private void reply(String... lines) {
        System.out.println(DIVIDER);
        for (String line : lines) {
            System.out.println(INDENT + line);
        }
        System.out.println(DIVIDER);
        System.out.println();
    }

    /**
     * Returns a count with its noun, made plural unless there is exactly one of
     * them, for example {@code 1 task} or {@code 2 tasks}.
     *
     * @param count how many there are
     * @param noun the singular form of what is being counted
     * @return the count and the noun, ready to drop into a sentence
     */
    private static String describeCount(int count, String noun) {
        if (count == 1) {
            return count + " " + noun;
        }
        return count + " " + noun + "s";
    }
}
