# AlfredTheButler

This is a greenfield Java project for a personal chatbot, named after _Alfred Pennyworth_, butler to the Wayne family. Given below are instructions on how to set it up.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/AlfredTheButler.java` file, right-click it, and choose `Run AlfredTheButler.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see something like the below as the output:
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

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
