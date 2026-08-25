# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: Average
* IDE and level of expertise: Average

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Testing the text UI

After every change to the code under `src/main/java`, and before reporting that change as done:

1. **Update `test/ui-test-plan.md` if needed.** Add a test case for a new command or behavior, and update the expected output of any existing case whose output the change alters. A change that only reorganizes internals without altering what the program prints needs no update to the plan; in that case say so explicitly, rather than skipping the step silently.
2. **Run the `test-ui` tests** over the whole plan against the rebuilt program. If you support skills, invoke the `test-ui` skill. If you do not (Codex, for example), read `.claude/skills/test-ui/SKILL.md` and follow the procedure in it directly — it is a plain Markdown file of shell steps and needs no skill mechanism to execute. Either way the procedure, and therefore the result, is the same.
3. **Report the result.** On success, show the console session. On failure, show the failing case with its expected and actual output. Do not describe a code change as complete or working while a case is failing.

Do these two steps in that order. Running the tests first and then editing the plan to match whatever the program happened to print makes the tests agree with bugs instead of catching them. Similarly, when a case fails, fix the program or correct the expected output deliberately, after deciding which of the two is actually wrong; never edit the plan just to make the test pass.

Expected output belongs in the plan only after it has been produced by an actual run and read line by line to confirm it is correct. See `.claude/skills/test-ui/SKILL.md` for how cases are written, run, and compared.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.
