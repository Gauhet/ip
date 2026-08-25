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

**Aim:** A plain line is stored as a todo and confirmed with a running count,
and `list` shows the stored tasks numbered from 1 in the order they were added.

**Input:**

```
read book
buy milk
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
read book
buy milk
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
borrow book
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

## Known gaps (not yet covered)

These are behaviours the program does not handle yet, so there is nothing stable
to assert. Add cases here as the features are implemented.

* Invalid input still crashes the program instead of printing an error message.
  Each of these was tried by hand against the current code:

  | Input | What happens today |
  | --- | --- |
  | `mark abc` | `NumberFormatException` |
  | `mark 99` | `NullPointerException` (empty slot in the array) |
  | `mark 0` | `ArrayIndexOutOfBoundsException` (index -1) |
  | `deadline foo` | `StringIndexOutOfBoundsException` (no `/by`) |
  | `event foo /from Mon` | `StringIndexOutOfBoundsException` (no `/to`) |

  Blank input used to belong on this list, quietly adding `[T][ ]` with nothing
  after it. It is now refused with a message, and TC7 covers it.

  The rest are not test cases yet because a stack trace is not correct behaviour
  to assert, and the line numbers in it change on every edit. Write the cases as
  each one is handled, using the new error message as expected output.
* Two crashes that are not caused by invalid input: adding a 101st task
  overruns the fixed-size array, and input that ends without `bye` leaves the
  program reading from a stream that has ended.
* There is no `todo` keyword yet, so `todo borrow book` is stored as a task
  literally described "todo borrow book".
* The 100-task limit and the wording "Now you have 1 tasks in the list."
  (singular/plural) are known rough edges.
