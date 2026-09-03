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
 * file, and whether it falls on a given day, so that a fourth kind can be added
 * without hunting through the program for places that test which kind it is.
 *
 * <p>{@link alfred.task.TaskList} holds the tasks in the order they were added.
 * It is the only part of the program that knows how many tasks there are, so it
 * is where a task number typed by the user is checked.
 */
package alfred.task;
