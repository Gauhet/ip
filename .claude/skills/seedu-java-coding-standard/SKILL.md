---
name: seedu-java-coding-standard
description: The Java coding standard this project follows — the se-edu Java conventions at the intermediate level. Use before writing or editing any .java file, after editing one, and whenever asked to check the coding standard, the Java conventions, the naming rules, the layout rules, or the Javadoc rules. Covers every .java file in the project, tests included.
---

# seedu-java-coding-standard

Every `.java` file in this project follows the
[se-edu Java coding standard](https://se-education.org/guides/conventions/java/intermediate.html)
at the **intermediate** level. That includes the tests under `src/test/java`, not
just the code under `src/main/java`.

The rules are written out in [rules.md](rules.md), so you can apply them while
writing rather than only catching them afterwards. **The page at the URL above
outranks that file.** Fetch it whenever a rule is contested, whenever a finding
would change code, and whenever `rules.md` is silent on the question in front of
you. If the fetch fails and the answer matters, say the check could not be
settled rather than guessing.

## When to use this

* **Before writing Java.** Read `rules.md` first, so the code comes out
  conforming. A rule applied while typing costs nothing; the same rule applied
  afterwards costs an edit, a rebuild, and a test run.
* **After writing or editing any `.java` file**, before reporting the change as
  done, as `AGENTS.md` requires.
* **Whenever asked** to check the coding standard, the conventions, the naming,
  the layout, or the Javadoc.

This skill covers Java only. Markdown files and the prose a reader sees are the
`style-check` skill's, which covers the se-edu Markdown conventions and the
Google developer documentation style guide as well as calling this one.

## Procedure

### 1. Decide what to check

Default to the files changed since the last commit:

```bash
git diff --name-only HEAD -- '*.java'; git ls-files --others --exclude-standard -- '*.java'
```

If that comes back empty, or the user asked for a full review, check every
tracked Java file instead:

```bash
git ls-files -- '*.java'
```

Say which set you checked. A clean report over one changed file and a clean
report over the whole project are different claims, and the reader has to be
able to tell them apart.

### 2. Mechanical pass

These find *candidates*. Every hit still has to be read against the rule before
it becomes a finding — each pattern over-reports by design, because a check that
misses violations is worse than one that raises a few false alarms. Set
`FILES` to the list from step 1 first.

```bash
grep -nP '\t' $FILES                                    # tabs; indentation is 4 spaces
awk 'length > 110 { print FILENAME":"FNR" ("length" chars)" }' $FILES   # soft 110, hard 120
grep -nP '[ \t]+$' $FILES                               # trailing whitespace
grep -nE 'import .*\*;' $FILES                          # wildcard imports
grep -nE '\b\w+ +\w+ *\[\] *[;=,)]' $FILES              # array specifier on the variable
grep -nE '^\s+public\s+[A-Za-z<>\[\]]+\s+\w+\s*[;=]' $FILES   # public non-constant fields
grep -nE '\b(if|for|while|switch|catch)\(' $FILES       # missing space after a reserved word
grep -nE '[a-z][A-Z]{2,}' $FILES                        # uppercase acronym inside a name
```

Javadoc block tags, whose descriptions the conventions require to be
punctuated. A description may wrap onto the lines below it and the period
belongs at the end of the last one, which no single-line pattern can see, so
this one is a Gradle task rather than a grep:

```bash
./gradlew checkJavadocTagPunctuation
```

It is wired into `check`, so `./gradlew check` and `./gradlew build` run it too.
Run it directly while editing, because it names every offending line at once
and costs a second.

Test method names, which the guide requires to be
`featureUnderTest_testScenario_expectedBehavior`:

```bash
awk '/@Test/ { expectTest = 1; next }
     expectTest && /^\s*(public )?void [A-Za-z0-9_]+\(/ {
       name = $0; sub(/^.*void /, "", name); sub(/\(.*$/, "", name)
       if (name !~ /^[a-z][A-Za-z0-9]*_[A-Za-z0-9]+_[A-Za-z0-9]+$/) print FILENAME":"FNR": "name
       expectTest = 0 }' $FILES
```

A method summary opening with the wrong form of the verb. The guide asks for
`Returns`, not `Return` or `Returning`, so this looks for the bare and the
progressive forms of the verbs this project actually uses:

```bash
grep -nE '^\s*\*\s+(Return|Add|Send|Get|Set|Create|Print|Check|Read|Write|Mark|Say|Tell|Show|Build|Convert|Remove|Keep|Prepare|Start|Open|Move|Color|Give|Finish|Prevent|Refuse|Adding|Returning|Creating|Printing|Reading|Writing|Checking)\b' $FILES
```

Constants, which the guide says should share a common prefix when they are
associated. Which ones are associated is a judgment, so this lists them per
file to be read rather than reporting violations:

```bash
grep -nE 'static final ' $FILES | sed 's/ *=.*//'
```

Indentation inside a `switch`, which the standard puts one level in from the
`switch` itself:

```bash
awk '/switch *\(/ { s = match($0, /[^ ]/) - 1 }
     /^ *(case|default)\b/ { c = match($0, /[^ ]/) - 1
       if (c != s + 4) print FILENAME":"FNR": case indented "c", expected "s + 4 }' $FILES
```

Wrapped lines, which are indented 8 spaces past the line they continue:

```bash
awk '{ i = match($0, /[^ ]/) - 1
       if ($0 ~ /^ *([+.?:]|&&|\|\||[A-Za-z0-9_]+\()/ && prev !~ /[;{}]$/ && prev != "" &&
           prev !~ /^ *(\*|\/\/)/ && i != p + 8 && i != p)
         print FILENAME":"FNR": continuation at "i", parent at "p
       prev = $0; p = i }' $FILES
```

A hit here is often a *nested* wrap — a break inside an argument that is itself
on a wrapped line. Those are legitimate, and the guide's preference for
higher-level breaks is what makes them read correctly.

**Anything in `rules.md` that a pattern can see belongs above, not below.** The
punctuation rule was in `rules.md` from the start and was left to the reading
pass, where it was read past twice and reported clean; a peer reviewer found it
on 156 tags. A rule left to attention is a rule that holds until attention
lapses. So when a check here misses something a person catches, the fix is not
to read more carefully next time — it is to add the check, and to prove it fails
on the case that got through.

### 3. Reading pass

The rules a regex cannot see are the ones worth the most care. Read each file
in scope against `rules.md` and judge:

* **Javadoc.** Does every class and every public method have a header comment?
  Does it open with a sentence that summarizes, in the third person present
  (`Returns`, not `Return` or `Returning`)? Is there a blank line before the
  first `@param`? Is every `@param` present, or none of them? Getters, setters,
  overriding methods whose parent Javadoc still fits, and test methods are
  exempt.
* **Names.** Classes and enums are nouns in PascalCase; methods are verbs in
  camelCase; constants are `UPPER_SNAKE_CASE`; booleans read like booleans
  (`isDone`, `hasData`); collections are plural; associated constants share a
  common **prefix**, not a common suffix; acronyms are not shouted
  (`toDvdName`, not `toDVDName`).
* **Test method names.** `featureUnderTest_testScenario_expectedBehavior`.
* **Scope.** Is each variable declared where it is first needed, and
  initialized there? Is any field public that is not a constant?
* **Braces.** Does every `if`, `else`, `for`, and `while` body have them, even
  a one-line one?
* **Blank lines.** Are the logical units within a method separated by one?

### 4. Report

Group findings by file. Give each one the rule it breaks, so the reader can
check the call rather than take it on trust. Separate:

* **Violations** — a stated rule is broken, and the fix is not a matter of
  taste.
* **Judgment calls** — the rule arguably applies, but the current code is
  defensible. Say which way you lean, and leave the decision to the user.

Finish by naming the files checked. If nothing was found, say so plainly; a
clean result is a real result.

## Fixing what you find

Fix violations. Raise judgment calls and leave them alone unless the user
decides. Three cases need care:

* **Anything that changes what the program prints** — an error message, a
  reply, the banner — is covered by `test/ui-test-plan.md`. Update the expected
  output there first, then run the `test-ui` skill, as `AGENTS.md` requires.
* **Renaming anything public** — a class, a method, an enum constant — is a
  design change wearing a style rule's clothes. Propose it; do not do it
  unasked. Renaming a private member is ordinary and needs no permission.
* **Any edit under `src/main/java`** obliges the rest of the `AGENTS.md`
  routine: update the affected JUnit tests, run `./gradlew test`, then run the
  text UI tests.
