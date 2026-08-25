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
* The last input line must be `bye`. Without it the program would keep reading
  from a stream that has ended and crash instead of exiting cleanly.
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

## Known gaps (not yet covered)

These are behaviours the program does not handle yet, so there is nothing stable
to assert. Add cases here as the features are implemented.

* Invalid input: unknown commands, `mark` with a non-number or an out-of-range
  number, `deadline`/`event` missing their `/by`, `/from` or `/to` keywords.
  Today these crash with an exception rather than printing an error message.
* There is no `todo` keyword yet, so `todo borrow book` is stored as a task
  literally described "todo borrow book".
* The 100-task limit and the wording "Now you have 1 tasks in the list."
  (singular/plural) are known rough edges.
