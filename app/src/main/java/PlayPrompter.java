import javax.swing.SwingUtilities;

import Euchre.*;
import Logging.*;
import MachineLearning.*;

public class PlayPrompter {

    public int playerTurn; // Identifies whose turn it is inside a trick
    public boolean trumpCalled;

    private EuchreEngine engine;
    private GUI gui;
    
    private boolean firstInit;

    private MasterLogger logger;

    private CoinTosser tosser;
    
    /**
     * Creates a new PlayPrompter.
     * 
     * @param logger Logger if logging is desired, AntiLogger if not.
     */
    PlayPrompter(MasterLogger logger, GUI gui) {
        this.firstInit = true;
        this.logger = logger;
        this.gui = gui;
        
        this.init();
    }

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
        this.sleep(500);
        gui.updateMainText("Randomly deciding who goes first...");
        this.sleep(1000);
        this.engine.dealer = (int)(Math.random() * 4);
        this.engine.leader = (this.engine.dealer + 1)%4;

        // Handles shuffling or prompting user to shuffle.
            if (this.engine.dealer == 0) {
                gui.updateMainText("You're dealer!");
                this.sleep(1000);
                gui.updateMainText("Shuffle the deck a few times.");
                gui.buildShuffleButtons();
            } else {
                gui.updateMainText("Player " + this.engine.dealer + " is dealer.");
                engine.deck.shuffle();
                this.sleep(1000);
                mainLoop();
            }
    }

    public void mainLoop() {

        while(this.engine.teamOneScore < 10 && this.engine.teamTwoScore < 10) {

            // Handles shuffling or prompting user to shuffle.
            if (this.engine.dealer == 0) {
                gui.updateMainText("You're dealer!");
                this.sleep(1000);
                gui.updateMainText("Shuffle the deck a few times.");
                gui.buildShuffleButtonsNoLoop();
            } else {
                gui.updateMainText("Player " + this.engine.dealer + " is dealer.");
                engine.deck.shuffle();
                this.sleep(1000);
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
            this.sleep(500);

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
            this.sleep(500);
            this.run(() -> gui.displayHand(0, engine.playerHands[0]));
            this.sleep(500);

            // Put bidding card in the center
            this.engine.bidCard = engine.deck.pop();
            this.run(() -> gui.buildCenter("The " + this.engine.bidCard.name.toLowerCase() + " is showing.", this.engine.bidCard, false));
            this.run(() -> this.gui.displayTrickInfo());
            this.sleep(500);

            /**
             * Bidding.
             */
            // Initial bidding round, turn a card over
            this.sleep(1000);
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
                this.sleep(1000);
            }
            
            // If a full round of initial bidding is done, we go on to the free bidding round.
            if (!this.trumpCalled) {
                this.gui.updateMainText("All players passed. Moving on to free bids.");
                this.sleep(1000);
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
                    this.sleep(1000);
                }
                // If three players passed, we're back to the dealer. We are, in fact, playing screw the dealer.
                if (!this.trumpCalled) {
                    this.gui.updateMainText("Screw the dealer. Dealer must pick a suit.");
                    this.sleep(1000);
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
                this.sleep(1000);
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
                this.sleep(1000);
            }

            this.playerTurn = this.engine.leader;
            this.run(() -> this.gui.displayTrickInfo());

            /**
             * Playing tricks
             */
            this.sleep(1000);
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
                    this.sleep(1000);
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
                this.sleep(1000);
            }

            int[] score = this.engine.getRoundWinner();
            this.gui.updateMainText("Team " + score[0] + " was awarded " + score[1] + " point(s)");
            this.engine.updateScore();
            this.gui.displayScore();
            this.engine.resetAfterTricks();
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

    /**
     * This idiot needs to be called every time I want to break mainLoop()'s
     * big while() loop. This will be necessary every time I want to update the
     * Swing stuff
     * 
     * @param action any command. Anything.
     */
    public void run(Runnable action) {
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}