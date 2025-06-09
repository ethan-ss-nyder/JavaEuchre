import Logging.*;

public class Main {
    public static void main(String args[]) {
        // Set up the GUI and Prompter to see each other.
        GUI gui = new GUI();
        new PlayPrompter(new AntiLogger(), gui);
    }
}