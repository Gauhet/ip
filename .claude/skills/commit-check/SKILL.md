---
name: commit-check
description: Check a drafted commit message, and the current branch name, against the se-edu Git conventions fetched live from their URL. Use before creating any commit, when proposing a commit message for approval, when asked to check or review a commit message, and when naming or renaming a branch.
---

# commit-check

Checks a commit message **before it becomes a commit**, against the se-edu Git
conventions. The rules are **read from the guide at run time**, not copied into
this file, so the check follows the guide as it stands today rather than as it
stood when this skill was written.

This is a draft check. It runs on a message you are about to commit, not as a
sweep over history.

## The guide

`https://se-education.org/guides/conventions/git.html`

It covers three things: the subject line, the body, and branch names. All three
are in scope here.

## Procedure

### 1. Fetch the guide

Fetch `https://se-education.org/guides/conventions/git.html` with WebFetch,
asking for every rule it states on subject lines, bodies, and branch names,
along with its examples. The page is short, so ask for the rules in full.

**If the fetch fails, stop and say so.** Report that the check did not run. Do
not fall back on remembered rules and present the result as a completed check:
the point of fetching is that the guide, not a recollection of it, is the
authority.

WebFetch caches the page for about 15 minutes, so checking two drafts in a row
costs one fetch.

### 2. Run the mechanical checks

Write the drafted message to a scratch file first — checking a message that
only exists in your head is how a 74-character subject gets through. Then:

```bash
MSG="$1"   # path to the file holding the drafted message

subject=$(sed -n '1p' "$MSG")
n=${#subject}
[ "$n" -gt 72 ] && echo "FAIL subject is $n chars (hard limit 72)"
[ "$n" -gt 50 ] && [ "$n" -le 72 ] && echo "WARN subject is $n chars (aim for 50)"

case "$subject" in
  [A-Z]*) ;;
  *) echo "FAIL subject does not start with a capital letter" ;;
esac
case "$subject" in
  *.) echo "FAIL subject ends with a period" ;;
esac

# Imperative mood, checked after any optional "scope: " prefix
verb=$(printf '%s' "$subject" | sed -E 's/^[^:]{1,30}: *//')
printf '%s' "$verb" |
  grep -qiE '^(add|fix|updat|remov|chang|creat|implement|refactor|delet|mov|renam|us|writ|test)(ed|ing)\b' &&
  echo "FAIL subject is not in the imperative mood: '$verb'"

awk 'NR == 2 && $0 != "" { print "FAIL no blank line between subject and body" }
     NR >= 3 && length > 72 { print "FAIL body line "NR" is "length" chars (wrap at 72)" }' "$MSG"
```

The imperative check is a heuristic over the verbs that come up most often. It
catches `Added` and `Adding` but not `Wrote`, so read the subject yourself as
well rather than treating a silent run as proof.

### 3. Check the branch name

```bash
branch=$(git rev-parse --abbrev-ref HEAD)
case "$branch" in
  master|main)
    echo "OK '$branch' is the default branch; the naming rules do not apply" ;;
  *)
    printf '%s' "$branch" | grep -qE '^([0-9]+-)?[a-z0-9]+(-[a-z0-9]+)*$' ||
      echo "FAIL branch '$branch' is not in kebab case" ;;
esac
```

A branch name is worth fixing early, while `git branch -m <new-name>` is all it
takes. Once a branch is pushed and others have it, renaming stops being free.

### 4. Read what the commands cannot see

* **Does the subject say what the commit does?** A subject that passes every
  mechanical rule can still be uninformative. `Update files` breaks nothing and
  tells the reader nothing.
* **Does the body explain what and why, rather than how?** The how is in the
  diff. Reviewers read the body for the reasoning that is not in the diff.
* **Is the scope prefix, if used, accurate?** `Person class: Remove static
  imports` is only right if the change is confined to that class.

### 5. Report and get approval

Show the message in full, in a fenced block, then say which rules it was
checked against and anything that failed. `AGENTS.md` requires the message to
be approved before the commit is made, so end by asking — never commit off the
back of a clean check alone.

## Local rules that outrank the guide

The guide is a general one; this project's own instructions come first, which
is the ordering the guide itself recommends.

* `AGENTS.md` asks for enough detail to explain the rationale. Weigh that
  against the size of the change: a one-line style fix does not need a body,
  and demanding one produces padding rather than reasoning. Flag a missing body
  only when the reasoning genuinely is not evident from the subject and diff.
* Follow any preference the user has stated about message length or shape, over
  the guide's default.
* Do not add trailers the project has not asked for, such as `Co-Authored-By`.
* `AGENTS.md` also says not to commit or push unless explicitly asked, and to
  use lightweight tags unless an annotated tag is requested.

## If the message is already committed

Sometimes the message under review turns out to be the one on `HEAD` already.
Check whether that commit has been pushed:

```bash
git log --oneline origin/$(git rev-parse --abbrev-ref HEAD)..HEAD
```

* **Not pushed** (the commit is listed): offer `git commit --amend` to correct
  the message, and ask before running it. Amending rewrites that commit, which
  is harmless while it exists only locally.
* **Already pushed** (nothing listed): report the problem and stop. Correcting
  it would mean rewriting published history and force-pushing, which can break
  anyone who has already pulled the branch. That is the user's call to make
  explicitly, not something to offer as a convenience.

## Notes

* This skill checks and reports. The only edit it ever offers is amending an
  unpushed commit message, and only after asking.
* The guide outranks this file. If the fetched page contradicts something
  written here, follow the page and say that the skill is out of date.
