package PlayPrompter;

import Euchre.EuchreEngine;
import Logging.MasterLogger;

public class AutoPlayPrompter extends MasterPrompter{

    /**
     * Create a new PlayPrompter instance.
     * @param logger pass in a logger is logging is desired, an antilogger is not.
     * @param gui pass in a GUI instance.
     * @param playSpeed in milliseconds how long of a pause between each step of play.
     */
    public AutoPlayPrompter(MasterLogger logger, EuchreEngine engine) {
        this.firstInit = true;
        this.logger = logger;
        this.engine = engine;
        
        this.init();
    }

    @Override
    public void mainLoop() {
        // Main gameplay loop regulated by win conditions
        while(this.engine.teamOneScore < 10 && this.engine.teamTwoScore < 10) {
            this.deal();
            this.bid();
            this.playTricks();
        }

        // Update display based on win condition
        if (this.engine.teamOneScore >= 10) {
            gui.updateMainText("Team one wins the game!");
            this.sleep(playSpeed * 5);
        } else if (this.engine.teamTwoScore >= 10) {
            gui.updateMainText("Team two wins the game!");
            this.sleep(playSpeed * 5);
        }

        // After each game, initialize the prompter and restart
        this.init();
    }
    
    @Override
    protected void deal() {
        // Have the dealer shuffle the deck
        this.gui.updateMainText("Player " + this.engine.dealer + " is dealing.");
        this.engine.deck.shuffle();
        this.run(() -> this.gui.displayTrickInfo());
        this.sleep(playSpeed);

        // Deal cards in the background as well as populate face-down cards in players' hands via GUI
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
                final int index = j;
                final int index2 = i;
                this.run(() -> gui.displayHandDown(index, index2));
                this.engine.playerHands[j].add(this.engine.deck.pop());
                this.sleep(playSpeed / 10);
            }
        }
        this.sleep(playSpeed);

        // Pause, then reveal all hands
        this.run(() -> gui.displayHands());
        this.sleep(playSpeed);

        // Put bidding card in the center
        this.engine.bidCard = engine.deck.pop();
        this.run(() -> gui.buildCenter("The " + this.engine.bidCard.name.toLowerCase() + " is showing.", this.engine.bidCard, false));
        this.run(() -> this.gui.displayTrickInfo());
        this.sleep(playSpeed);
    }
}
