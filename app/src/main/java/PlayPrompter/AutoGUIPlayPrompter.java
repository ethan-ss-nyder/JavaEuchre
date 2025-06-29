package PlayPrompter;

import Euchre.*;
import GUI.GUI;
import Logging.*;

public class AutoGUIPlayPrompter extends MasterPrompter {
    
    /**
     * Create a new PlayPrompter instance.
     * @param logger pass in a logger is logging is desired, an antilogger is not.
     * @param gui pass in a GUI instance.
     * @param playSpeed in milliseconds how long of a pause between each step of play.
     */
    public AutoGUIPlayPrompter(MasterLogger logger, GUI gui, EuchreEngine engine, int playSpeed) {
        this.firstInit = true;
        this.playSpeed = playSpeed;
        this.logger = logger;
        this.gui = gui;
        this.engine = engine;
        
        this.gui.allFaceUp = true;
        this.init();
    }

    public void mainLoop() {

        while(this.engine.teamOneScore < 10 && this.engine.teamTwoScore < 10) {
            this.deal();
            this.bid();
            this.playTricks();
        }

        if (this.engine.teamOneScore >= 10) {
            gui.updateMainText("Team one wins the game!");
            this.sleep(playSpeed * 5);
        } else if (this.engine.teamTwoScore >= 10) {
            gui.updateMainText("Team two wins the game!");
            this.sleep(playSpeed * 5);
        }

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

    @Override
    protected void bid() {

        // Initial bidding round, turn a card over
        this.playerTurn = this.engine.leader;
        for (int i = 0; i < 4; i++) {
            this.trumpCalled = this.tosser.callTrump();
            if (this.trumpCalled) {
                this.gui.updateMainText("Player " + this.playerTurn + " said to pick up the " + this.engine.bidCard.name);
                this.engine.setOffense(this.playerTurn%2);
                this.run(() -> this.gui.displayTrickInfo());
                break;
            } else {
                this.gui.updateMainText("Player " + this.playerTurn + " has passed.");
            }
            this.playerTurn = (this.playerTurn + 1) % 4; // Neat modular stuff
            this.sleep(playSpeed);
        }
        
        // If a full round of initial bidding is done, we go on to the free bidding round.
        if (!this.trumpCalled) {
            this.gui.updateMainText("All players passed. Moving on to free bids.");
            this.sleep(playSpeed);
            for (int i = 0; i < 3; i++) {
                this.tosser.callSuit();
                if (this.engine.trump != null) {
                    this.gui.updateMainText("Player " + this.playerTurn + " has called " + this.engine.trump.toString());
                    this.engine.setOffense(this.playerTurn%2);
                    this.run(() -> this.gui.displayTrickInfo());
                    this.trumpCalled = true;
                    break;
                } else {
                    this.gui.updateMainText("Player " + this.playerTurn + " has passed.");
                    this.trumpCalled = false;
                }
                this.playerTurn = (this.playerTurn + 1) % 4;
                this.sleep(playSpeed);
            }

            // If three players passed, we're back to the dealer. We are, in fact, playing screw the dealer.
            if (!this.trumpCalled) {
                this.gui.updateMainText("Screw the dealer. Dealer must pick a suit.");
                this.sleep(playSpeed);
                this.tosser.callSuit();
                this.gui.updateMainText("Player " + this.playerTurn + " has called " + this.engine.trump.toString());
                this.trumpCalled = true;
            }

        // If a full round of bidding is done and trump WAS called, handle swapping with the bid card.
        } else {
            this.engine.setTrump(this.engine.bidCard.suit);
            this.run(() -> this.gui.displayTrickInfo());
            this.sleep(playSpeed);
            this.tosser.suggestSwapBidCard(this.engine.dealer);
            this.engine.swapBidCard();
            this.run(() -> this.gui.displayHands());
            this.run(() -> this.gui.buildCenter("Player " + this.engine.dealer + " swapped cards.", null, true));
            this.run(() -> this.gui.displayTrickInfo());
            this.sleep(playSpeed);
        }

        this.playerTurn = this.engine.leader;
        this.run(() -> this.gui.displayTrickInfo());
        this.sleep(playSpeed);
    }

    @Override
    protected void playTricks() {
        for (int i = 0; i < 5; i++) { // Five tricks
            for (int j = 0; j < 4; j++) { // Four players play per trick
                this.gui.updateMainText("Player " + this.playerTurn + " is playing.");
                this.engine.playCard(this.playerTurn, this.tosser.suggestCard(this.playerTurn));
                this.run(() -> gui.displayHands());
                // After each play, display all played cards, increment player turn
                this.run(() -> gui.displayPlayedCards());
                this.playerTurn = (this.playerTurn + 1)%4;
                this.sleep(playSpeed);
            }

            // Somebody won the trick. Update all relevant things based on that info
            int winner = this.engine.getWinner(true);
            this.gui.displayScore();
            this.gui.updateMainText("Player " + winner + " wins this trick.");
            this.playerTurn = winner;
            this.engine.leader = winner;
            this.engine.resetLedSuit();
            this.sleep(playSpeed * 4);

            // Clear the middle of the screen, prep for next trick
            this.engine.clearPlayedCards();
            this.gui.displayPlayedCards();
            this.sleep(playSpeed);
        }

        // After all five tricks are played, update winner info
        int[] score = this.engine.getRoundWinner();
        this.gui.updateMainText("Team " + score[0] + " was awarded " + score[1] + " point(s)");
        this.engine.updateScore();
        this.gui.displayScore();
        this.engine.resetAfterTricks();
        this.engine.dealer = (this.engine.dealer + 1)%4;
        this.sleep(playSpeed * 4);
    }
}