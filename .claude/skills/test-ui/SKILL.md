---
name: test-ui
description: Run the text UI tests for AlfredTheButler. Compiles the project, replays each test case from test/ui-test-plan.md through a fresh run of the program, and compares the console output against the expected output, stopping at the first failure. Use when asked to test the UI, run the UI tests, run the text-ui tests, check the chatbot output, or verify that a command still prints the right thing. Also use after changing anything that affects console output.
---

# test-ui

Runs the test cases in `test/ui-test-plan.md` against the real program and
reports the console session.

## The contract with the test plan

`test/ui-test-plan.md` holds every test case. Each case is a `## TC<n>: <title>`
heading, a prose **Aim**, then two fenced code blocks in this order:

1. **Input** — the lines typed into the program, one command per line, last line
   `bye`.
1. **Expected output** — the entire console output of that run, from the
   greeting banner to the farewell message.

Each case runs in its own fresh program, so cases never share task state.

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
  /^## TC/            { tc++; fence = 0; next }
  tc == 0             { next }
  /^```/              { fence++; infence = (fence % 2 == 1); next }
  infence && fence == 1 { print > (out "/tc" tc ".in") }
  infence && fence == 3 { print > (out "/tc" tc ".expected") }
' test/ui-test-plan.md
```

Fence 1 is the Input block and fence 3 is the Expected output block, because
each case has exactly two fenced blocks in that order. Check that the expected
number of `.in` / `.expected` pairs appeared before running anything; if a case
is missing, the plan does not follow the required shape and should be fixed
first.

### 3. Run the cases, in the order they appear in the plan

Run them in a loop that **stops at the first failure** and prints a per-case
pass/fail line, so the failing case is always identifiable. Never run them in a
way that swallows individual results or keeps going after a failure.

1. Run the program with the case's input file on standard input:

   ```bash
   java -cp "$WORK/build" AlfredTheButler < "$WORK/tc1.in" > "$WORK/tc1.actual" 2>&1
   ```

   Give the run a short timeout (10s is plenty). If it times out, treat it as a
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
   norm "$WORK/tc1.expected" > "$WORK/tc1.expected.norm"
   norm "$WORK/tc1.actual"   > "$WORK/tc1.actual.norm"
   diff -u "$WORK/tc1.expected.norm" "$WORK/tc1.actual.norm"
   ```

   `diff` exits 0 when the files match. Any other exit status is a failure.
1. If the case passed, move on to the next one. If it failed, go to
   "Reporting a failure" below and stop.

Putting those together:

```bash
for n in $(seq 1 "$CASES"); do
  java -cp "$WORK/build" AlfredTheButler < "$WORK/tc$n.in" > "$WORK/tc$n.actual" 2>&1
  norm "$WORK/tc$n.expected" > "$WORK/tc$n.e.norm"
  norm "$WORK/tc$n.actual"   > "$WORK/tc$n.a.norm"
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

```bash
transcript() {   # $1 = actual output file, $2 = input file
  awk -v inputs="$2" '
    { print }
    /^ *_{20,}$/ {
        if (++dividers % 2 == 0 && (getline cmd < inputs) > 0) print "> " cmd
    }
  ' "$1"
}
transcript "$WORK/tc1.actual" "$WORK/tc1.in"
```

Counting divider lines rather than splitting on blank lines matters: the
greeting banner has a blank line inside it, so blank-line splitting would run
one command out of step.

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
* This skill only reads and runs; it never edits `src/main/java`.
