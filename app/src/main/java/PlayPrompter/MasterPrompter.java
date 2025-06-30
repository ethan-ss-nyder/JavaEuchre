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

    protected MasterLogger logger;
    protected CoinTosser tosser;
    protected EuchreEngine engine;
    protected GUI gui;

    /**
     * Initiates a new game of Euchre by resetting PlayPrompter and giving the GUI the fresh instance.
     * Randomly picks a dealer.
     */
    public void init() {

        // This quarantine is necessary to avoid "New Game" catastrophes.
        // TODO: actually fix this.
        if (firstInit) {
            gui.setPrompter(this);
            gui.init();
            firstInit = false;
        }
        this.engine.init();
        this.gui.setEngine(engine);
        this.tosser = new CoinTosser(this.engine);
        this.playerTurn = 0;
        this.trumpCalled = false;

        // Randomly roll for dealer, set leader (player who leads bidding round and first hand of trick)
        this.gui.updateMainText("Initializing game...");
        this.sleep(playSpeed);
        this.gui.updateMainText("Randomly deciding who goes first...");
        this.sleep(playSpeed);
        this.engine.dealer = (int)(Math.random() * 4);
        this.engine.leader = (this.engine.dealer + 1)%4;

        this.mainLoop();
    }

    /**
     * Loops the game. Should call deal(), bid(), and playTricks() in that order.
     */
    public abstract void mainLoop();

    /**
     * Handles dealing: shuffle deck, have EuchreEngine deal cards into player hands, 
     * prompt GUI to display dealing animation, reveal player hand, 
     */
    protected void deal() {

        // Have the dealer shuffle the deck
        if (this.engine.dealer == 0) {
            this.gui.updateMainText("You are dealing.");
            this.sleep(this.playSpeed);
            this.gui.updateMainText("Shuffle the deck a few times.");
            this.gui.buildShuffleButtonsNoLoop();
        } else {
            this.gui.updateMainText("Player " + this.engine.dealer + " is dealing.");
            this.engine.deck.shuffle();
        }
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

        // Pause, then reveal the player his/her hand (or all hands if that's selected)
        if (this.gui.allFaceUp) {
            this.run(() -> this.gui.displayHand(this.engine.dealer, this.engine.playerHands[this.engine.dealer]));
        } else {
            this.run(() -> gui.displayHand(0, this.engine.playerHands[0]));
        }
        this.sleep(playSpeed);

        // Put bidding card in the center
        this.engine.bidCard = engine.deck.pop();
        this.run(() -> gui.buildCenter("The " + this.engine.bidCard.name.toLowerCase() + " is showing.", this.engine.bidCard, false));
        this.run(() -> this.gui.displayTrickInfo());
        this.sleep(playSpeed);
    }

    protected void bid() {

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
        this.sleep(playSpeed);
    }
    
    protected void playTricks() {
        
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
        this.engine.roundNumber++;
        this.sleep(playSpeed * 4);
    }

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