# Text UI test plan

Manual-style tests for the text UI of `AlfredTheButler`, run automatically by the
`test-ui` skill (`.claude/skills/test-ui/SKILL.md`).

## How a test case is run

1. All sources in `src/main/java` are compiled to a temporary build directory.
2. For **each** test case, a **fresh** program is started and the case's `Input`
   lines are fed to it on standard input, one per line.
3. The program's whole console output is compared with the case's
   `Expected output`.

Because each case gets its own run, cases are independent: tasks added in one
case never leak into the next. This is why every case that needs a task list has
to add the tasks itself.

## Rules for writing a test case

* Every case has three fenced blocks, in this order: **Input**, then
  **Expected output**. The aim is the prose line above them.
* The last input line must be one that exits, which in practice means `bye`.
  Without it the program would keep reading from a stream that has ended and
  crash instead of exiting cleanly. Extra words after the keyword are ignored,
  so `bye now` also exits; only TC8 relies on that, to prove it.
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
     Now you have 1 tasks in the list.
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
     Now you have 1 tasks in the list.
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

**Aim:** `deadline <description> /by <time>` splits the line at `/by`, stores a
deadline, and displays it as `[D]` with its due time in brackets.

**Input:**

```
deadline return book /by Sunday
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
       [D][ ] return book (by: Sunday)
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[D][ ] return book (by: Sunday)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## TC5: Add an event

**Aim:** `event <description> /from <start> /to <end>` splits the line at both
keywords, stores an event, and displays it as `[E]` with its start and end
times.

**Input:**

```
event project meeting /from Mon 2pm /to 4pm
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
       [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[E][ ] project meeting (from: Mon 2pm to: 4pm)
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
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
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
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: Sunday)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] return book (by: Sunday)
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] borrow book
     2.[D][X] return book (by: Sunday)
     3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
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
     Now you have 1 tasks in the list.
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

## TC10: Unrecognised commands are refused

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

**Aim:** `todo`, `deadline` and `event` each check that every part they need is
present, and refuse the command with one message naming all of those parts
rather than crashing or storing a half-built task.

The six inputs cover every way a part can go missing: no arguments at all, a
keyword with no separator, a separator with nothing before it, and a separator
with nothing after it. `deadline /by Sunday` is the case that needs the
separator to be matched without its surrounding spaces; matched with them, it
would look like a missing `/by` rather than a missing description.

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
     A deadline needs a description and a /by time, sir.
    ____________________________________________________________

    ____________________________________________________________
     A deadline needs a description and a /by time, sir.
    ____________________________________________________________

    ____________________________________________________________
     A deadline needs a description and a /by time, sir.
    ____________________________________________________________

    ____________________________________________________________
     An event needs a description, a /from time and a /to time, sir.
    ____________________________________________________________

    ____________________________________________________________
     An event needs a description, a /from time and a /to time, sir.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

---

## Known gaps (not yet covered)

These are behaviours the program does not handle yet, so there is nothing stable
to assert. Add cases here as the features are implemented.

* A bad task number still crashes rather than drawing an error message. Each of
  these was tried by hand against the current code:

  | Input | What happens today |
  | --- | --- |
  | `mark abc` | `NumberFormatException` |
  | `mark` | `NumberFormatException` (empty string) |
  | `mark 99` | `NullPointerException` (empty slot in the array) |
  | `mark 0` | `ArrayIndexOutOfBoundsException` (index -1) |

  `unmark` fails in exactly the same ways, since both read their number the
  same way.

  These are not test cases yet because a stack trace is not correct behaviour
  to assert, and the line numbers in it change on every edit. Write the cases
  once the numbers are checked, using the new error messages as expected
  output.

  The rows that used to sit here for blank input, a missing description and a
  missing `/by` or `/to` are gone: those are handled now, and TC7, TC10 and
  TC11 cover them.
* Two crashes that are not caused by invalid input: adding a 101st task
  overruns the fixed-size array, and input that ends without `bye` leaves the
  program reading from a stream that has ended.
* The 100-task limit and the wording "Now you have 1 tasks in the list."
  (singular/plural) are known rough edges.
