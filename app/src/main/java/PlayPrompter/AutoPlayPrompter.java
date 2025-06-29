package PlayPrompter;

import Euchre.*;
import GUI.GUI;
import Logging.*;
import MachineLearning.*;

public class AutoPlayPrompter extends MasterPrompter {
    
    /**
     * Create a new PlayPrompter instance.
     * @param logger pass in a logger is logging is desired, an antilogger is not.
     * @param gui pass in a GUI instance.
     * @param playSpeed in milliseconds how long of a pause between each step of play.
     */
    public AutoPlayPrompter(MasterLogger logger, GUI gui, int playSpeed) {
        this.firstInit = true;
        this.firstGame = true;
        this.playSpeed = playSpeed;
        this.logger = logger;
        this.gui = gui;
        
        this.init();
    }

    public void mainLoop() {

        while(this.engine.teamOneScore < 10 && this.engine.teamTwoScore < 10) {

            // Handles shuffling or prompting user to shuffle.
            if (this.engine.dealer == 0 && !firstGame) {
                gui.updateMainText("You're dealer!");
                this.sleep(this.playSpeed);
                gui.updateMainText("Shuffle the deck a few times.");
                gui.buildShuffleButtonsNoLoop();
            } else if (this.engine.dealer != 0 && !firstGame) {
                gui.updateMainText("Player " + this.engine.dealer + " is dealer.");
                engine.deck.shuffle();
                this.sleep(this.playSpeed);
            }

            /**
             * Dealing.
             */
            if (this.engine.dealer == 0) {
                gui.updateMainText("You are dealing.");
            } else {
                gui.updateMainText("Player " + this.engine.dealer + " is dealing.");
            }
            this.run(() -> this.gui.displayTrickInfo());
            this.sleep(playSpeed);

            // Deal cards in the background as well as populate face-down cards in players' hands via GUI
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 4; j++) {
                    final int index = j;
                    final int index2 = i;
                    this.run(() -> gui.displayHandDown(index, index2));
                    engine.playerHands[j].add(engine.deck.pop());
                    this.sleep(100);
                }
            }

            // Pause, then reveal the player his/her hand
            this.sleep(playSpeed);
            this.run(() -> gui.displayHand(0, engine.playerHands[0]));
            this.sleep(playSpeed);

            // Put bidding card in the center
            this.engine.bidCard = engine.deck.pop();
            this.run(() -> gui.buildCenter("The " + this.engine.bidCard.name.toLowerCase() + " is showing.", this.engine.bidCard, false));
            this.run(() -> this.gui.displayTrickInfo());
            this.sleep(playSpeed);

            /**
             * Bidding.
             */
            // Initial bidding round, turn a card over
            this.sleep(playSpeed);
            this.playerTurn = this.engine.leader;
            for (int i = 0; i < 4; i++) {
                if (this.playerTurn == 0) {
                    this.run(() -> this.gui.buildBiddingButtons("Bidding on " + this.engine.bidCard.name));
                    if (this.trumpCalled) {
                        this.gui.updateMainText("You said to pick up the " + this.engine.bidCard.name);
                        this.engine.setOffense(0);
                        this.run(() -> this.gui.displayTrickInfo());
                        break;
                    } else {
                        this.gui.updateMainText("You've passed.");
                    }
                } else {
                    this.trumpCalled = this.tosser.callTrump();
                    if (this.trumpCalled) {
                        this.gui.updateMainText("Player " + this.playerTurn + " said to pick up the " + this.engine.bidCard.name);
                        this.engine.setOffense(this.playerTurn%2);
                        this.run(() -> this.gui.displayTrickInfo());
                        break;
                    } else {
                        this.gui.updateMainText("Player " + this.playerTurn + " has passed.");
                    }
                }
                this.playerTurn = (this.playerTurn + 1) % 4; // Neat modular stuff
                this.sleep(playSpeed);
            }
            
            // If a full round of initial bidding is done, we go on to the free bidding round.
            if (!this.trumpCalled) {
                this.gui.updateMainText("All players passed. Moving on to free bids.");
                this.sleep(playSpeed);
                for (int i = 0; i < 3; i++) {
                    if (this.playerTurn == 0) {
                        this.run(() -> gui.buildFreeBidButtons("Pick a trump suit"));
                        if (this.trumpCalled) {
                            this.gui.updateMainText("You called " + this.engine.trump.toString() + " as the trump suit.");
                            this.engine.setOffense(0);
                            this.run(() -> this.gui.displayTrickInfo());
                            break;
                        } else {
                            this.gui.updateMainText("You've passed.");
                        }
                    } else {
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
                    }
                    this.playerTurn = (this.playerTurn + 1) % 4;
                    this.sleep(playSpeed);
                }
                // If three players passed, we're back to the dealer. We are, in fact, playing screw the dealer.
                if (!this.trumpCalled) {
                    this.gui.updateMainText("Screw the dealer. Dealer must pick a suit.");
                    this.sleep(playSpeed);
                    if (this.playerTurn == 0) {
                        this.run(() -> gui.buildFreeBidButtonsNoPass("Bidding on " + this.engine.bidCard.name));
                        this.gui.updateMainText("You called " + this.engine.trump.toString() + " as the trump suit.");
                    } else {
                        this.tosser.callSuit();
                        this.gui.updateMainText("Player " + this.playerTurn + " has called " + this.engine.trump.toString());
                    }
                    this.trumpCalled = true;
                }
            // If a full round of bidding is done and trump WAS called, handle swapping with the bid card.
            } else {
                this.engine.setTrump(this.engine.bidCard.suit);
                this.run(() -> this.gui.displayTrickInfo());
                this.sleep(playSpeed);
                if (this.engine.dealer == 0) {
                    this.run(() -> this.gui.buildBidCardSwapButtons("What card do you want to swap?"));
                    this.engine.swapBidCard();
                    this.run(() -> this.gui.displayHand(0, this.engine.playerHands[0]));
                    this.run(() -> this.gui.buildCenter("Cards swapped.", null, true));
                } else {
                    this.tosser.suggestSwapBidCard(this.engine.dealer);
                    this.engine.swapBidCard();
                    // If all hands are already face-up, when refreshing the dealer's swap, keep it face-up.
                    if (this.gui.allFaceUp) {
                        this.run(() -> this.gui.displayHand(this.engine.dealer, this.engine.playerHands[this.engine.dealer]));
                    } else {
                        this.run(() -> this.gui.displayHandDown(this.engine.dealer, this.engine.playerHands[this.engine.dealer].getDeck().length));
                    }
                    this.run(() -> this.gui.buildCenter("Player " + this.engine.dealer + " swapped cards.", null, true));
                }
                this.run(() -> this.gui.displayTrickInfo());
                this.sleep(playSpeed);
            }

            this.playerTurn = this.engine.leader;
            this.run(() -> this.gui.displayTrickInfo());

            /**
             * Playing tricks
             */
            this.sleep(playSpeed);
            for (int i = 0; i < 5; i++) { // Five tricks
                for (int j = 0; j < 4; j++) { // Four players play per trick
                    // If it's the player's turn
                    if (this.playerTurn == 0) {
                            this.run(() -> gui.buildPlayCardButtons("Your turn"));
                    // If it's not the player's turn
                    } else {
                        this.gui.updateMainText("Player " + this.playerTurn + " is playing.");
                        this.engine.playCard(this.playerTurn, this.tosser.suggestCard(this.playerTurn));
                    }
                    // Extra logic to display cards face up or face down depending on player preference
                    if (this.gui.allFaceUp) {
                        this.run(() -> gui.displayHand(this.playerTurn, this.engine.playerHands[this.playerTurn]));
                    } else {
                        this.run(() -> gui.displayHandDown(this.playerTurn, this.engine.playerHands[this.playerTurn].getDeck().length - 1));
                    }
                    // After each play, display all cards, increment player turn
                    this.run(() -> gui.displayPlayedCards());
                    this.run(() -> gui.displayHand(0, this.engine.playerHands[0])); // Bit of redundancy to keep player 0's hand up
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
                this.sleep(2000);

                // Clear the middle of the screen, prep for next trick
                this.engine.clearPlayedCards();
                this.gui.displayPlayedCards();
                this.sleep(playSpeed);
            }

            int[] score = this.engine.getRoundWinner();
            this.gui.updateMainText("Team " + score[0] + " was awarded " + score[1] + " point(s)");
            this.engine.updateScore();
            this.gui.displayScore();
            this.engine.resetAfterTricks();
            this.engine.dealer = (this.engine.dealer + 1)%4;
            this.firstGame = false;
            this.sleep(5000);
        }

        if (this.engine.teamOneScore >= 10) {
            System.out.println("Team one won! (Your team!)");
            gui.updateMainText("Team one wins the game!");
            this.sleep(5000);
        } else if (this.engine.teamTwoScore >= 10) {
            gui.updateMainText("Team two wins the game!");
            this.sleep(5000);
        }
    }
}