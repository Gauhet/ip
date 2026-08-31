/**
 * The tasks themselves, and the list they are kept in.
 *
 * <p>{@link alfred.task.Task} holds what every kind of task shares — a
 * description and whether it is done — and the three kinds differ in the dates
 * they carry: {@link alfred.task.ToDo} has none,
 * {@link alfred.task.Deadline} has a day it is due, and
 * {@link alfred.task.Event} spans a stretch of days.
 *
 * <p>Each kind answers for itself how it is shown, how it is written to the save
 * file, and whether it falls on a given day. That keeps those three answers next
 * to the dates they are made from, and means a fourth kind of task can be added
 * without hunting through the rest of the program for the places that would have
 * had to test which kind it is.
 *
 * <p>{@link alfred.task.TaskList} holds the tasks in the order they were added,
 * which is the order they are shown and saved in. It is the only part of the
 * program that knows how many tasks there are, so it is where a task number
 * typed by the user is checked.
 */
package alfred.task;
