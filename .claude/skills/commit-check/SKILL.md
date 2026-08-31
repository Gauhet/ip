---
name: commit-check
description: Check a drafted commit message, and the current branch name, against this project's Git standard, then get the message approved before it becomes a commit. Use before creating any commit, when proposing a commit message for approval, when asked to check or review a commit message, and when naming or renaming a branch.
---

# commit-check

Checks a commit message **before it becomes a commit**, then gets it approved.

This is a draft check. It runs on a message you are about to commit, not as a
sweep over history.

## The rules belong to `seedu-git-standard`

**The se-edu Git conventions are the `seedu-git-standard` skill's.** It holds
the rules written out, the mechanical checks over a drafted message, and the
branch name check. **Invoke it, and fold its findings into the report below.**
If it is not available, read `.claude/skills/seedu-git-standard/SKILL.md` and
follow its steps directly.

What stays here is the workflow around one particular message: writing the
draft down, getting it approved, and dealing with a message that has already
been committed.

## Procedure

### 1. Write the draft to a file

Check a message that exists on disk, not one that exists only in your head —
that is how a 74-character subject gets through. Put it in the scratch
directory.

### 2. Apply the standard

Invoke `seedu-git-standard` against that file. It checks the subject, the body,
and the current branch name, and reports what it finds.

### 3. Report and get approval

Show the message in full, in a fenced block, then say what it was checked
against and anything that failed. `AGENTS.md` requires the message to be
approved before the commit is made, so end by asking — never commit off the
back of a clean check alone.

Two of the project's local rules bear on the draft itself, and the standard
lists the rest:

* `AGENTS.md` asks for enough detail to explain the rationale. Weigh that
  against the size of the change: a one-line style fix does not need a body,
  and demanding one produces padding rather than reasoning.
* Do not add trailers the project has not asked for, such as `Co-Authored-By`.

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
* `seedu-git-standard` outranks this file on any question of what the rules
  are, and the se-edu guide outranks it in turn.
