import Euchre.EuchreEngine;
import GUI.*;
import Logging.*;
import PlayPrompter.*;

public class Main {

    // This is a generic pause variable used across PlayPrompter objects which calls GUI functions,
    // so make this small for very fast GUI updates, make it large for longer "animations".
    private final static int PLAY_SPEED = 100;

    public static void main(String args[]) {
        
        // Manual game
        //new PlayPrompter(new AntiLogger(), new GUI(), new EuchreEngine(), PLAY_SPEED);

        // Automated games
        new AutoGUIPlayPrompter(new GameLogger("testlogger.jsonl"), new GUI(), new EuchreEngine(), PLAY_SPEED);
        //new AutoPlayPrompter(new GameLogger("testlogger.jsonl"), new EuchreEngine());
    }
}