---
name: test-ui
description: Run the text UI tests for AlfredTheButler. Compiles the project, replays each test case from test/ui-test-plan.md through a fresh run of the program, and compares the console output against the expected output, stopping at the first failure. Use when asked to test the UI, run the UI tests, run the text-ui tests, check the chatbot output, or verify that a command still prints the right thing. Also use after changing anything that affects console output.
---

# test-ui

Runs the test cases in `test/ui-test-plan.md` against the real program and
reports the console session.

## The contract with the test plan

`test/ui-test-plan.md` holds every test case. Each case is a `## TC<n>: <title>`
heading, a prose **Aim**, then either two or three fenced code blocks.

A **one-run case** has two blocks, in this order:

1. **Input** — the lines typed into the program, one command per line, last line
   `bye`.
1. **Expected output** — the entire console output of that run, from the
   greeting banner to the farewell message.

A **two-run case** has three blocks, and exists to test what survives a restart:

1. **First input** — the lines typed into the first run.
1. **Second input** — the lines typed into a second run, started after the first
   has exited, against the save file the first run left behind.
1. **Expected output** — the console output of **both** runs, one after the
   other, so the second greeting appears in the middle of the block.

The number of blocks is what tells the two apart, so a case must have exactly
two or exactly three.

Each case starts from a **fresh program and an empty save file**, so cases never
share task state. Within a two-run case the save file is *not* cleared between
the two runs — carrying it over is the whole point of the case.

Read the plan's own "Rules for writing a test case" section before adding cases.

## Procedure

Use the Bash tool (Git Bash) for all of the steps below. Set up the paths once,
in the same command as the compile, since shell state does not persist between
calls:

```bash
WORK="$TMPDIR/test-ui"          # or any scratch dir outside the repo
rm -rf "$WORK" && mkdir -p "$WORK/build"
```

### 1. Compile

```bash
javac -d "$WORK/build" src/main/java/*.java
```

If compilation fails, stop. Report the compiler errors and do not run any case —
a failure to build is not a test failure, and running stale class files would
report the wrong result.

### 2. Extract the cases from the plan

Pull each case's two fenced blocks straight out of the Markdown, rather than
retyping them. Copying by hand risks changing a space and testing the wrong
thing:

```bash
awk -v out="$WORK" '
  /^## TC/ { tc++; fence = 0; next }
  tc == 0  { next }
  /^```/   { fence++; infence = (fence % 2 == 1); blk = int((fence + 1) / 2); next }
  infence  { print > (out "/tc" tc ".b" blk) }
' test/ui-test-plan.md
```

Every opening fence starts the next block, so a case's blocks land in
`tc<n>.b1`, `tc<n>.b2`, and — for a two-run case only — `tc<n>.b3`. Which block
is which follows from how many there are:

```bash
if [ -f "$WORK/tc$n.b3" ]; then   # two-run case
  inputs="$WORK/tc$n.b1 $WORK/tc$n.b2"; expected="$WORK/tc$n.b3"
else                              # one-run case
  inputs="$WORK/tc$n.b1";          expected="$WORK/tc$n.b2"
fi
```

Before running anything, check that every case produced a `b1` and a `b2`, and
that no case produced a `b4`. A case with any other number of blocks does not
follow the required shape, and the plan should be fixed first.

### 3. Run the cases, in the order they appear in the plan

Run them in a loop that **stops at the first failure** and prints a per-case
pass/fail line, so the failing case is always identifiable. Never run them in a
way that swallows individual results or keeps going after a failure.

1. **Clear the save file**, so the case starts with an empty task list:

   ```bash
   rm -f data/alfred.txt
   ```

   This matters as much as compiling. The program reads `data/alfred.txt` at
   startup, so without this the tasks from one case reappear in the next, and
   every case after the first compares against the wrong list. Clear it once
   per **case**, never between the two runs of a two-run case.
1. Run the program once per input block, appending each run's output to the
   same actual-output file so a two-run case is compared as one session:

   ```bash
   : > "$WORK/tc1.actual"
   r=0
   for in in $inputs; do
     r=$((r + 1))
     java -cp "$WORK/build" AlfredTheButler < "$in" > "$WORK/tc1.r$r.actual" 2>&1
     cat "$WORK/tc1.r$r.actual" >> "$WORK/tc1.actual"
   done
   ```

   Each run's output is kept on its own as well as appended, because the
   transcript in step 5 has to be built per run.

   Give each run a short timeout (10s is plenty). If it times out, treat it as a
   failure: it usually means the input did not end with `bye`, so the program is
   still waiting for a command that will never come.
1. Normalize both files and diff them. Normalizing strips carriage returns,
   trailing whitespace, and blank lines at the end of the file — differences
   that come from the platform, not from the program:

   ```bash
   norm() {
     sed -e 's/\r$//' -e 's/[[:space:]]*$//' "$1" |
       awk '{ line[n++] = $0 }
            END { last = n - 1
                  while (last >= 0 && line[last] == "") last--
                  for (i = 0; i <= last; i++) print line[i] }'
   }
   norm "$expected"        > "$WORK/tc1.expected.norm"
   norm "$WORK/tc1.actual" > "$WORK/tc1.actual.norm"
   diff -u "$WORK/tc1.expected.norm" "$WORK/tc1.actual.norm"
   ```

   `diff` exits 0 when the files match. Any other exit status is a failure.
1. If the case passed, move on to the next one. If it failed, go to
   "Reporting a failure" below and stop.

Putting those together:

```bash
for n in $(seq 1 "$CASES"); do
  if [ -f "$WORK/tc$n.b3" ]; then
    inputs="$WORK/tc$n.b1 $WORK/tc$n.b2"; expected="$WORK/tc$n.b3"
  else
    inputs="$WORK/tc$n.b1";              expected="$WORK/tc$n.b2"
  fi
  rm -f data/alfred.txt
  : > "$WORK/tc$n.actual"
  r=0
  for in in $inputs; do
    r=$((r + 1))
    timeout 10 java -cp "$WORK/build" AlfredTheButler < "$in" > "$WORK/tc$n.r$r.actual" 2>&1
    cat "$WORK/tc$n.r$r.actual" >> "$WORK/tc$n.actual"
  done
  norm "$expected"          > "$WORK/tc$n.e.norm"
  norm "$WORK/tc$n.actual"  > "$WORK/tc$n.a.norm"
  if diff -u "$WORK/tc$n.e.norm" "$WORK/tc$n.a.norm" > "$WORK/tc$n.diff"; then
    echo "TC$n PASS"
  else
    echo "TC$n FAIL"; cat "$WORK/tc$n.diff"; break
  fi
done
```

### 4. Reporting a failure

Terminate the session immediately — do not run the remaining cases, and do not
try to fix the code unless asked. Report, in this order:

* which case failed (number, title, and its aim),
* the exact input that was fed to the program,
* the **expected** output, in full,
* the **actual** output, in full,
* the `diff -u` output, which points at the first differing line,
* a one-line reading of what the difference suggests (for example: an extra
  blank line, a changed word, an exception stack trace where a reply was
  expected).

Then say clearly that the remaining cases were not run.

Do not "fix" the plan to match the program. A mismatch means either the program
or the expected output is wrong, and deciding which is the user's call.

### 5. Reporting success

When every case passes, show the console session so it can be read like a real
run. Piping input means the commands are not echoed, so put them back: every
reply is wrapped in a pair of divider lines, so after each *closing* divider
insert the next command, prefixed with `> ` as if it had been typed at the
prompt. Blocks pair up 1:1 with input lines after the greeting.

A block is a response to a command, *except* for the blocks the program prints
before it reads anything: the greeting, and the startup message it prints when
it has restored tasks or could not read the save file. Those have to be passed
over, or every command lands one block early:

```bash
STARTUP_MSG="I've brought back|I could not read your saved tasks"

transcript() {   # $1 = actual output file, $2 = input file
  extra=0
  grep -qE "$STARTUP_MSG" "$1" && extra=1
  awk -v inputs="$2" -v extra="$extra" '
    { print }
    /^ *_{20,}$/ {
        if (++dividers % 2 == 0) {
            if (passed < extra) { passed++ }
            else if ((getline cmd < inputs) > 0) print "> " cmd
        }
    }
  ' "$1"
}
transcript "$WORK/tc1.r1.actual" "$WORK/tc1.b1"
```

`STARTUP_MSG` matches the wording the program uses today. If those messages are
reworded, update it, or the interleaving silently goes back to being one block
out on any run that loads tasks.

**For a two-run case, transcribe each run separately** against its own input
file, and show the two one after the other with a line saying the program was
restarted in between:

```bash
transcript "$WORK/tc1.r1.actual" "$WORK/tc1.b1"
echo "--- program restarted, same save file ---"
transcript "$WORK/tc1.r2.actual" "$WORK/tc1.b2"
```

Do **not** run the combined output against the two input files concatenated.
The second run prints its own greeting, which is a divider pair with no command
in front of it, so every command from that point on is inserted one block early.
The result looks plausible and is wrong, which is worse than not interleaving at
all.

Counting divider lines rather than splitting on blank lines matters: the
greeting banner has a blank line inside it, so blank-line splitting would run
one command out of step.

Check the result before showing it. The tell that it is right is that the last
`>` line is the case's last command and no commands are left over; the tell that
it is wrong is a `> bye` sitting in the middle with replies after it.

If the interleaving ever comes out wrong — commands left over at the end, or a
`>` line in the middle of a reply — fall back to showing the input list and then
the raw output, and say that the transcript could not be interleaved.

Put each case's transcript in a fenced code block under its heading, and finish
with a one-line summary: how many cases ran and that all passed.

## Adding a test case

When asked to add a case:

1. Append a new `## TC<n>` section to `test/ui-test-plan.md` with its aim and
   Input block.
1. Get the expected output by **running** the program with that input, then
   paste the real output into the Expected output block — after checking line by
   line that the output is actually correct. Writing expected output by hand is
   how typos in indentation end up baked into the plan; running it and reviewing
   it is both faster and more accurate.
1. Re-run the whole plan to confirm nothing else broke.

## When the output changes on purpose

Some changes alter output that many cases share — renaming the chatbot, editing
the banner, fixing the "1 tasks" wording. The greeting is repeated in full in
every expected block, so such a change invalidates all of them at once.

Regenerate those blocks by running each case and pasting in the real output.
Do not hand-edit them: retyping the same banner six times is how one block ends
up with a wrong space that then looks like a genuine test failure. Read the
regenerated output before pasting it, to confirm the change is the one intended
and nothing else moved.

## Notes

* Java 25 is required (`java -version` to check).
* Class files go to a scratch directory, never into the repository.
* `data/alfred.txt` is the one repository path this skill writes to, because the
  program writes there and the file has to be cleared between cases. It is
  ignored by Git, so a run leaves the working tree clean. Whatever the last case
  saved is left behind afterwards.
* This skill only reads and runs; it never edits `src/main/java`.
