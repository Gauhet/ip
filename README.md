# AlfredTheButler

This is a greenfield Java project for a personal chatbot, named after _Alfred Pennyworth_, butler to the Wayne family. Given below are instructions on how to set it up.

## Setting up in IntelliJ

Prerequisites: JDK 25, update IntelliJ to the most recent version.

1. Open IntelliJ (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into IntelliJ as follows:

   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/alfred/AlfredTheButler.java` file, right-click it, and choose `Run AlfredTheButler.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see something like the below as the output:

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
   Bye. Hope to see you again soon!
   ____________________________________________________________
   ```

## Creating and running the JAR file

The build script uses the [Gradle Shadow plugin](https://gradleup.com/shadow/) to package the program and its dependencies into a single executable JAR file, often called a _fat JAR_. Because everything is inside one file, you can copy it to any computer with JDK 25 installed and run it there, without Gradle or the source code.

1. From the project directory, build the JAR file:

   ```
   ./gradlew shadowJar
   ```

   On Windows, run `.\gradlew.bat shadowJar` instead.
1. Find the JAR file at `build/libs/alfred.jar`. Git ignores the `build` folder, so you rebuild the JAR file with the command above instead of finding it in the repository.
1. Run the JAR file from a terminal:

   ```
   java -jar build/libs/alfred.jar
   ```

   The program saves your task list to a `data` folder inside the folder you run the command from, so run it from the folder where you want that data kept.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
