package PlayPrompter;

import javax.swing.SwingUtilities;

import Euchre.EuchreEngine;
import GUI.GUI;
import Logging.MasterLogger;
import MachineLearning.CoinTosser;

public abstract class MasterPrompter {

    public int playSpeed;

    public int playerTurn; // Identifies whose turn it is inside a trick
    public boolean trumpCalled;
    
    protected boolean firstInit;
    protected boolean firstGame;

    protected MasterLogger logger;
    protected CoinTosser tosser;
    protected EuchreEngine engine;
    protected GUI gui;

    /**
     * Initiates a new game of Euchre by resetting PlayPrompter and giving the GUI the fresh instance.
     * Also handles the initial shuffling and bidding before the loopable 5 trick code.
     */
    public void init() {
        this.playerTurn = 0;
        this.trumpCalled = false;

        // GUI and Prompter have much circular dependency.
        // This quarantine is necessary to avoid "New Game" catastrophes.
        // TODO: actually fix this.
        if (firstInit) {
            gui.setPrompter(this);
            gui.init();
            firstInit = false;
        }
        this.engine = new EuchreEngine();
        this.engine.init();
        this.gui.setEngine(engine);
        this.tosser = new CoinTosser(this.engine);

        // Randomly roll for dealer, set leader (player who leads bidding round and first hand of trick)
        gui.updateMainText("Initializing game...");
        this.sleep(playSpeed);
        gui.updateMainText("Randomly deciding who goes first...");
        this.sleep(playSpeed);
        this.engine.dealer = (int)(Math.random() * 4);
        this.engine.leader = (this.engine.dealer + 1)%4;

        // Handles shuffling or prompting user to shuffle.
            if (this.engine.dealer == 0) {
                gui.updateMainText("You're dealer!");
                this.sleep(playSpeed);
                gui.updateMainText("Shuffle the deck a few times.");
                gui.buildShuffleButtons();
            } else {
                gui.updateMainText("Player " + this.engine.dealer + " is dealer.");
                engine.deck.shuffle();
                this.sleep(playSpeed);
                mainLoop();
            }
    }
    public abstract void mainLoop();

    /**
     * This idiot needs to be called every time I want to break mainLoop()'s
     * big while() loop. This will be necessary every time I want to update the
     * Swing stuff
     * @param action any command. Anything.
     */
    public void run(Runnable action) {
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread-safe sleep function.
     * @param millis time to pause execution in millis
     */
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}