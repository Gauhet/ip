/**
 * Alfred the Butler: a personal chatbot.
 * At this stage it only greets the user and then says goodbye.
 */
public class AlfredTheButler {
    /**
     * Runs the chatbot: prints the banner and greeting, then the farewell.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        String line = "____________________________________________________________";
        String name = "AlfredTheButler";

        String banner =
                "            _     _      _____  ____   _____  ____\n"
                + "           / \\   | |    |  ___||  _ \\ | ____||  _ \\\n"
                + "          / _ \\  | |    | |_   | |_) ||  _|  | | | |\n"
                + "         / ___ \\ | |___ |  _|  |  _ < | |___ | |_| |\n"
                + "        /_/   \\_\\|_____||_|    |_| \\_\\|_____||____/\n"
                + "                    P E N N Y W O R T H\n"
                + "\n"
                + "      Butler to the Wayne family  --  At your service";

        System.out.println(line);
        System.out.println(banner);
        System.out.println("Hello! I'm " + name);
        System.out.println("What can I do for you?");
        System.out.println(line);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(line);
    }
}
