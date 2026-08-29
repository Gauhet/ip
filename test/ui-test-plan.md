# Text UI test plan

Manual-style tests for the text UI of `AlfredTheButler`, run automatically by the
`test-ui` skill (`.claude/skills/test-ui/SKILL.md`).

## How a test case is run

1. All sources in `src/main/java` are compiled to a temporary build directory.
1. For **each** test case, the whole `data` folder is deleted, then a **fresh**
   program is started and the case's `Input` lines are fed to it on standard
   input, one per line.
1. The program's whole console output is compared with the case's
   `Expected output`.

The program reads `data/alfred.txt` at startup and rewrites it after every
change, so deleting it before each case is what keeps cases independent: tasks
added in one case never leak into the next. This is why every case that needs a
task list has to add the tasks itself.

The **folder** is deleted, not just the file, which means every case that does
not seed a save file starts from exactly what a fresh copy of this project on
another computer looks like: no save file, and no folder to hold one. The
program has to create both on demand, so that path is exercised by every such
case rather than needing one of its own.

A **two-run case** is the exception, and the only way to test that tasks survive
a restart. It has three fenced blocks instead of two: two inputs, run one after
the other against the *same* save file, and one expected output holding both
runs' console output end to end. The file is cleared before the case, not
between its two runs.

## Rules for writing a test case

* Every fenced block is introduced by a bold label saying what it is, and the
  label is what the runner goes by, not the order. A one-run case has
  **Input** and **Expected output**; a two-run case has **First input**,
  **Second input**, and **Expected output**. The aim is the prose above them.
* A case may add a **Save file** block, whose contents become `data/alfred.txt`
  before the case runs. That is the only way to test what the program does with
  a file it did not write, since it never saves a line it cannot read back.
* The last input line is normally `bye`, but does not have to be: input that
  simply runs out is treated the same way, which TC16 proves. Extra words after
  the keyword are ignored, so `bye now` also exits; only TC8 relies on that.
* `Expected output` is the **entire** console output of the run, starting with
  the greeting banner and ending with the farewell message.
* Comparison is line-by-line and exact, except that trailing whitespace,
  Windows/Unix line-ending differences, and blank lines at the very end of the
  output are ignored. Those differ between machines without being real bugs.
* Cases run top to bottom. The session stops at the first failing case.

Note that the greeting banner is repeated in every expected output. That is
deliberate: each case is readable on its own. A more compact alternative would
be to store the banner once and have the runner prepend it, at the cost of an
extra rule to remember when reading the file.

---

## TC1: Startup and exit

**Aim:** The greeting (banner, name, prompt) is printed once at startup, and
`bye` exits with the farewell message and nothing else.

**Input:**

```
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC2: Add todos and list them

**Aim:** `todo` stores a task and confirms it with a running count, and `list`
shows the stored tasks numbered from 1 in the order they were added.

**Input:**

```
todo read book
todo buy milk
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] buy milk
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][ ] buy milk
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC3: Mark and unmark a task

**Aim:** `mark <n>` and `unmark <n>` change the status box of the n-th task
only, and the change survives until it is reversed (checked with `list` after
each command).

**Input:**

```
todo read book
todo buy milk
mark 2
list
unmark 2
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] buy milk
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] buy milk
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][X] buy milk
    ____________________________________________________________

    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] buy milk
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][ ] buy milk
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC4: Add a deadline

**Aim:** `deadline <description> /by <date>` splits the line at `/by`, reads the
date as a real date rather than as the words that were typed, stores a deadline,
and displays it as `[D]` with its due date in brackets.

The date goes in as `2019-10-15`, the `yyyy-mm-dd` form the program reads, and
comes out as `Oct 15 2019`, the form a reader gets. That the two differ is the
assertion: text that had merely been carried through would come back exactly as
it was typed, so a display in the input's own form would prove nothing.

**Input:**

```
deadline return book /by 2019-10-15
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: Oct 15 2019)
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[D][ ] return book (by: Oct 15 2019)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC5: Add an event

**Aim:** `event <description> /from <start> /to <end>` splits the line at both
keywords, reads both dates, stores an event, and displays it as `[E]` with its
start and end dates.

The start and the end are different days, so a start wrongly stored in place of
the end, or the reverse, would show up rather than being hidden by two equal
values. Both are shown as `MMM dd yyyy`, the same reader's form a deadline
gets.

**Input:**

```
event project meeting /from 2019-12-02 /to 2019-12-03
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC6: Mixed task types in one list

**Aim:** The three task types can coexist in one list, keep their own type
boxes, are numbered in the order they were added, and `mark` picks the right one
by number regardless of type.

**Input:**

```
todo borrow book
deadline return book /by 2019-10-15
event project meeting /from 2019-12-02 /to 2019-12-03
mark 2
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: Oct 15 2019)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] return book (by: Oct 15 2019)
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] borrow book
     2.[D][X] return book (by: Oct 15 2019)
     3.[E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC7: Blank input is refused

**Aim:** A line with nothing on it draws an error message instead of being
stored as a task with an empty description, and the loop carries on afterwards
rather than exiting.

The case feeds two blank lines: one truly empty, one holding only spaces. The
second checks that the line is judged by its content rather than its length,
though the two are indistinguishable when reading this file. That is
deliberately harmless: if an editor ever strips the trailing spaces, the line
becomes a second empty line and the expected output is unchanged.

**Input:**

```

   
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     You'll have to give me something to work with, sir.
    ____________________________________________________________

    ____________________________________________________________
     You'll have to give me something to work with, sir.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC8: Spacing and extra words do not stop a command

**Aim:** A command is found by its first word, so spaces around the line are
ignored and words after the keyword are ignored too. `   list   ` and
`list all` both list, and `bye now` still exits.

The list is left empty on purpose, so the case tests only how the line is read
and does not have to be rewritten whenever the way tasks are added changes.

**Input:**

```
   list   
list all
bye now
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC9: Add todos with the `todo` keyword

**Aim:** `todo <description>` stores a todo described by the words after the
keyword, not by the whole line, and it is displayed and numbered exactly like a
todo added any other way.

The keyword is now the only way to add a todo; a bare description is refused,
which TC10 covers. This case overlaps with TC2 on purpose: TC2 is about the
running count and the numbering, this one about the keyword and how much of the
line becomes the description.

**Input:**

```
todo read book
todo return the Batmobile
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] return the Batmobile
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][ ] return the Batmobile
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC10: Unrecognized commands are refused

**Aim:** A word that is not a command draws an error quoting that word back,
rather than being stored as a task. Covers both a mistyped command (`lst`) and
a bare description of the kind that used to become a todo (`borrow book`).

The closing `list` is the point of the case: it shows an empty list, proving
the two refused lines stored nothing rather than being quietly accepted.

**Input:**

```
lst
borrow book
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     I'm afraid I don't know 'lst', sir.
    ____________________________________________________________

    ____________________________________________________________
     I'm afraid I don't know 'borrow', sir.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC11: Task commands with missing parts are refused

**Aim:** `todo`, `deadline`, and `event` each check that every part they need is
present, and refuse the command with one message naming all of those parts
rather than crashing or storing a half-built task.

The six inputs cover every way a part can go missing: no arguments at all, a
keyword with no separator, a separator with nothing before it, and a separator
with nothing after it. `deadline /by Sunday` is the case that needs the
separator to be matched without its surrounding spaces; matched with them, it
would look like a missing `/by` rather than a missing description.

`Sunday` is deliberately not a date. The case still expects the message about
the missing description, which is what shows the parts are checked for presence
before the date is read, rather than the other way around.

As in TC10, the closing `list` is the assertion that matters: an empty list
shows none of the six was stored.

**Input:**

```
todo
deadline return book
deadline /by Sunday
deadline return book /by
event project meeting
event project meeting /from Mon 2pm
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     A todo needs a description, sir.
    ____________________________________________________________

    ____________________________________________________________
     A deadline needs a description and a /by date, sir.
    ____________________________________________________________

    ____________________________________________________________
     A deadline needs a description and a /by date, sir.
    ____________________________________________________________

    ____________________________________________________________
     A deadline needs a description and a /by date, sir.
    ____________________________________________________________

    ____________________________________________________________
     An event needs a description, a /from date, and a /to date, sir.
    ____________________________________________________________

    ____________________________________________________________
     An event needs a description, a /from date, and a /to date, sir.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC12: Bad task numbers are refused

**Aim:** `mark`, `unmark`, and `delete` check the number they are given before
using it. Anything that is not a number, and any number that does not name a
task in the list, draws a message rather than a crash.

The inputs cover both messages and every route to them: a word, no number at
all, a negative, zero, a number past the end of the list, and a number given
when the list is empty. The first line runs before anything has been added,
which is the empty-list case; it shares the out-of-range message rather than
having one of its own.

All three commands read their number through the same helper, so the two
`delete` lines are there to show that `delete` uses it too, rather than to
retest the checks themselves.

The closing `list` shows the task still present and still unmarked, proving
that none of the refused commands changed anything on its way to being
refused — in particular that a refused `delete` removed nothing.

**Input:**

```
mark 1
todo read book
mark abc
mark
mark -1
mark 0
mark 2
unmark 9
delete xyz
delete 2
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     There is no such task, sir.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     That is not a task number, sir.
    ____________________________________________________________

    ____________________________________________________________
     That is not a task number, sir.
    ____________________________________________________________

    ____________________________________________________________
     There is no such task, sir.
    ____________________________________________________________

    ____________________________________________________________
     There is no such task, sir.
    ____________________________________________________________

    ____________________________________________________________
     There is no such task, sir.
    ____________________________________________________________

    ____________________________________________________________
     There is no such task, sir.
    ____________________________________________________________

    ____________________________________________________________
     That is not a task number, sir.
    ____________________________________________________________

    ____________________________________________________________
     There is no such task, sir.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC13: Delete a task

**Aim:** `delete <n>` removes the n-th task, confirms it by showing the task
that was removed and the number left, and the tasks after it move up so the
list stays numbered from 1 with no gap.

The task deleted is the middle one of three, which is what makes the
renumbering visible: after `delete 2` the third task has to become number 2.
The two `list` commands bracket the deletion so the before and after can be
compared directly. Deleting the last remaining task afterwards checks the
count reaches zero and the list comes back empty rather than breaking.

**Input:**

```
todo read book
deadline return book /by 2019-10-15
event project meeting /from 2019-12-02 /to 2019-12-03
list
delete 2
list
delete 1
delete 1
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: Oct 15 2019)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: Oct 15 2019)
     3.[E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [D][ ] return book (by: Oct 15 2019)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
     Now you have 0 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC14: Tasks survive a restart

**Aim:** Tasks saved by one run are read back by the next. All three types come
back, with their descriptions, their dates, their done status, and their
original order intact, and the count of restored tasks is announced once before
the first command.

This is a two-run case: the first run builds and marks a list and exits, the
second starts against the file the first left behind and does nothing but
`list`. The task marked in the first run is the one that proves the status
digit is read as well as written, and the deadline and event prove the fields
after the description survive the round trip.

Those fields are now dates rather than free text, so this case carries the
weight of the date handling: the second run rebuilds each one with
`LocalDate.parse`, and a date written to the file in a form that could not be
read back would be dropped as a damaged line and show up here as a missing
task.

It is also what holds the two date formats apart. The file keeps `yyyy-mm-dd`,
because that is the form that reads back exactly, while the list shows
`MMM dd yyyy`. Saving what the user is shown would round-trip a month name
through `LocalDate.parse`, and this case is where that would surface, as three
tasks that failed to come back.

**First input:**

```
todo read book
deadline return book /by 2019-10-15
event project meeting /from 2019-12-02 /to 2019-12-03
mark 1
bye
```

**Second input:**

```
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: Oct 15 2019)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________

    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     I've brought back 3 tasks from last time, sir.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: Oct 15 2019)
     3.[E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC15: An emptied list brings nothing back

**Aim:** A run that ends with no tasks leaves nothing for the next run to
restore. The next run says nothing about loading and starts with an empty list,
rather than announcing zero tasks or bringing back the deleted one.

The first run adds a task and deletes it again, so the save file is emptied
rather than never written. That is the case that would break if deleting the
last task left the file untouched, or if the greeting announced a count
unconditionally.

**First input:**

```
todo read book
delete 1
bye
```

**Second input:**

```
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] read book
     Now you have 0 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________

    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC16: Input that runs out is treated as `bye`

**Aim:** Input that ends without an exit command finishes the session cleanly,
with the same farewell as `bye`, instead of failing to read a line that is not
there.

The input deliberately has no `bye`. Before this was handled, the program threw
`NoSuchElementException` and printed a stack trace after the last command, which
is what a redirected or piped session hits every time it reaches the end of its
file.

**Input:**

```
todo read book
list
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC17: A description containing the field separator survives a restart

**Aim:** A description holding the ` | ` that separates fields in the save file
comes back whole, rather than being cut short at the first one.

This is the case that would silently lose data: before the separator was
escaped, `todo a | b` saved as three fields and came back as just `a`, with no
error to show anything had gone wrong. The deadline covers a separator in the
description of a task that has a field after it, which is where a lost
separator would misalign the remaining fields rather than only truncating the
last one.

The `/by` date can no longer hold a separator, since a `yyyy-mm-dd` date has no
room for one, so the escaping is exercised through the description alone.

**First input:**

```
todo a | b
deadline pipe | desc /by 2019-10-15
bye
```

**Second input:**

```
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] a | b
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] pipe | desc (by: Oct 15 2019)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________

    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     I've brought back 2 tasks from last time, sir.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] a | b
     2.[D][ ] pipe | desc (by: Oct 15 2019)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC18: Damaged lines are skipped, not fatal

**Aim:** A save file with bad lines in it still gives up its good ones. Each
readable task is restored, the unreadable lines are counted and reported, and
the user is told the lines will be lost, since nothing keeps a copy of them.

The seeded file covers every way a line can be wrong: an unknown type letter,
too few fields, a missing date field, a status that is neither 0 nor 1, and a
date field that is not a date. A good task sits at each end, so a bad line in
the middle cannot be shown to stop the ones after it from loading.

The good event is seeded as `2019-12-02` and listed as `Dec 02 2019`, which is
the file's form and the reader's form seen side by side in one case: the block
below is exactly what is on disk, and the expected output is what a person sees.

The last two bad lines are the ones the dates brought with them, and they are
wrong in the two different ways a date can be. `Sunday` is not shaped like a
date at all; `2019-02-30` is shaped like one and names a day that does not
exist. A hand-edited file is the only way either reaches this code, since the
program never writes one.

Each has to cost its own line and no more. The exception `LocalDate.parse`
throws is unchecked, so passed along as it came it would escape the loading loop
and cost the whole file; `DateParser` turns it into the checked one the loop
watches for.

**Save file:**

```
T | 1 | read book
X | 0 | unknown type
T | 0
D | 0 | no by field
T | 2 | bad status
D | 0 | bad date | Sunday
D | 0 | impossible day | 2019-02-30
E | 0 | good event | 2019-12-02 | 2019-12-03
```

**Input:**

```
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     I've brought back 2 tasks from last time, sir.
    ____________________________________________________________

    ____________________________________________________________
     I could not make sense of 6 lines in your saved tasks, sir.
     I have left them out, and they will be gone once the list changes.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[E][ ] good event (from: Dec 02 2019 to: Dec 03 2019)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC19: A date that cannot be read is refused with a message

**Aim:** A date the program cannot read draws a message saying what to type,
the task is not stored, and the session carries on.

A date can be wrong in two ways, and the two get different messages because the
user can do something different about each:

* **Not shaped like a date.** `Sunday`, `2/12/2019`, and `2019-2-3` are written
  in the wrong format, so the reply names the right one and shows an example.
  `2019-2-3` is the case that would slip through a looser check: it is nearly
  right, and wrong only in that the month and day are not padded to two digits.
* **Shaped like a date but naming no real day.** `2019-02-30` has the format
  exactly right, so repeating the format would not help. The reply points at the
  day and the month instead.

The last input covers a bad `/to`, so the check is shown to apply to an event's
dates and not only to a deadline's.

Earlier this behavior was the happy path only, and the unchecked exception from
`LocalDate.parse` fell through to the safety net meant for bugs in the program,
which replied with the name of a Java class. This case previously asserted that,
so that it would fail and ask to be rewritten once a real message existed. This
is that rewrite.

The closing `list` is the assertion that matters: an empty list shows that no
half-built task was stored on its way to being refused.

**Input:**

```
deadline return book /by Sunday
deadline pay rent /by 2/12/2019
deadline file taxes /by 2019-2-3
deadline leap /by 2019-02-30
event party /from 2019-12-02 /to next week
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     I don't know 'Sunday' as a date, sir. Do use yyyy-mm-dd, as in 2019-10-15.
    ____________________________________________________________

    ____________________________________________________________
     I don't know '2/12/2019' as a date, sir. Do use yyyy-mm-dd, as in 2019-10-15.
    ____________________________________________________________

    ____________________________________________________________
     I don't know '2019-2-3' as a date, sir. Do use yyyy-mm-dd, as in 2019-10-15.
    ____________________________________________________________

    ____________________________________________________________
     There is no such date as '2019-02-30', sir. Do check the day and the month.
    ____________________________________________________________

    ____________________________________________________________
     I don't know 'next week' as a date, sir. Do use yyyy-mm-dd, as in 2019-10-15.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC20: Edge-case dates survive a save and a load

**Aim:** A date comes back from the save file as the same day it went in, for
the dates most likely to expose a mistake in the two formats.

TC14 already restarts the program with a deadline and an event in the list, but
its dates are ordinary ones. This case picks the three that would catch a format
error TC14 would miss:

* `2019-01-05` has a single-digit month and a single-digit day. Written with a
  pattern that does not pad, it would save as `2019-1-5`, which
  `LocalDate.parse` refuses, and the task would come back as a skipped line.
* `2020-02-29` is a leap day. It exists only if the year is carried with it, so
  a format that dropped or altered the year would turn it into an impossible
  date rather than a merely wrong one.
* The event runs from `2019-12-31` to `2020-01-01`, crossing a year boundary, so
  a year taken from the wrong field would show up as a start and end in the same
  year.

Reading them back is the assertion. The dates the second run prints are produced
from the file rather than from anything left in memory, so a date that did not
survive shows up either as a changed day or as a task that is missing entirely.

**First input:**

```
deadline file taxes /by 2019-01-05
deadline leap day /by 2020-02-29
event new year /from 2019-12-31 /to 2020-01-01
bye
```

**Second input:**

```
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] file taxes (by: Jan 05 2019)
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] leap day (by: Feb 29 2020)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] new year (from: Dec 31 2019 to: Jan 01 2020)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________

    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     I've brought back 3 tasks from last time, sir.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[D][ ] file taxes (by: Jan 05 2019)
     2.[D][ ] leap day (by: Feb 29 2020)
     3.[E][ ] new year (from: Dec 31 2019 to: Jan 01 2020)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC21: An event cannot end before it starts

**Aim:** An event whose `/to` date falls before its `/from` date is refused,
while one that starts and ends on the same day is allowed.

Both dates can be perfectly readable and still describe something that cannot
happen, so this check is separate from reading them, and runs only once both
have been read: two dates that cannot be read cannot be compared.

The second input is the boundary. An event lasting a single day has an end that
is not after its start, so a check written with the comparison the wrong way
round would refuse it. Storing it is what shows only a strictly earlier end is
refused.

**Input:**

```
event backwards /from 2020-01-01 /to 2019-01-01
event one day /from 2020-01-01 /to 2020-01-01
list
bye
```

**Expected output:**

```
    ____________________________________________________________
            _     _      _____  ____   _____  ____
           / \   | |    |  ___||  _ \ | ____||  _ \
          / _ \  | |    | |_   | |_) ||  _|  | | | |
         / ___ \ | |___ |  _|  |  _ < | |___ | |_| |
        /_/   \_\|_____||_|    |_| \_\|_____||____/
                    P E N N Y W O R T H

      Butler to the Wayne family  --  At your service
     Hello! I'm AlfredTheButler
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     An event cannot end before it starts, sir.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] one day (from: Jan 01 2020 to: Jan 01 2020)
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[E][ ] one day (from: Jan 01 2020 to: Jan 01 2020)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## Known gaps (not yet covered)

No invalid command crashes the program any more. Blank input, an unknown
keyword, a missing description or separator, and a bad task number are all
refused with a message, covered by TC7, TC10, TC11, and TC12.

A damaged save file no longer costs the whole list, a description containing the
field separator survives being saved, input that runs out ends the session
cleanly, and counts read as "1 task" rather than "1 tasks". Those are covered by
TC18, TC17, TC16, and the counts throughout.

A date that cannot be read now has a message of its own, saying either what
format to use or that the day does not exist, covered by TC19. A date missing
altogether is covered by TC11, an event that ends before it starts by TC21, and
an unreadable date in the save file by TC18.

One gap is a limit of the program rather than of the tests. Only `yyyy-mm-dd` is
accepted, so `2/12/2019` is refused rather than understood, and a time of day
such as `1800` has nowhere to go, since a task holds a `LocalDate` and not a
`LocalDateTime`. Both are refusals with a clear reason rather than failures, but
accepting more of what a user might reasonably type is the next thing to add.

The rest are limits of the tests rather than of the program.

* The contents of `data/alfred.txt` are still not read by any case directly.
  TC14, TC15, and TC17 check the file indirectly, by restarting the program and
  looking at what comes back, which catches a wrong or missing line. Asserting
  the exact bytes of the file is not something this plan can express.
* A save file that cannot be opened at all — one whose permissions deny reading,
  or whose name has been taken by a folder — is handled and reported, but no
  case covers it. A **Save file** block can only put text in the file; it cannot
  make the file unreadable. A failing save is untestable for the same reason.
  Both were checked by hand.
* Damaged lines are reported and skipped, but nothing keeps a copy of them, so
  they are gone from the file as soon as the list next changes. TC18 asserts the
  warning that says so, which is the whole of the mitigation. Copying the file
  aside before the first overwrite would close this properly.
* The safety net that catches an unexpected fault inside a command is not
  covered, since reaching it needs a bug to exist.
