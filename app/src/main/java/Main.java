import GUI.GUI;
import Logging.*;
import PlayPrompter.PlayPrompter;

public class Main {

    // This is a generic pause variable used across PlayPrompter objects which calls GUI functions,
    // so make this small for very fast GUI updates, make it large for longer "animations".
    private final static int PLAY_SPEED = 400;

    public static void main(String args[]) {
        // Set up the GUI and Prompter to see each other.
        GUI gui = new GUI();
        new PlayPrompter(new AntiLogger(), gui, PLAY_SPEED);
    }
}