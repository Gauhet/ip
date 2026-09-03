/**
 * Alfred the Butler: a chatbot that keeps a list of tasks, taking one typed
 * command per line and saving the list to disk after every change.
 *
 * <p>{@link alfred.AlfredTheButler} holds the command loop. Each round of the
 * conversation passes through four parts, and this package holds three of them:
 *
 * <ul>
 *   <li>{@link alfred.Ui} reads the line and prints every reply, so the layout
 *       of a message lives in one place.
 *   <li>{@link alfred.Parser} turns that line into an
 *       {@link alfred.command.Command}, refusing anything it cannot read
 *       before the task list has been touched.
 *   <li>{@link alfred.Storage} writes the list to a file and reads it back at
 *       startup.
 * </ul>
 *
 * <p>The fourth is the task list itself, in {@link alfred.task}.
 *
 * <p>{@link alfred.Dates} sits alongside them because both the commands the
 * user types and the lines of the save file have to agree on what counts as a
 * date. {@link alfred.AlfredException} is what any of them raises to refuse
 * something and carry on.
 */
package alfred;
