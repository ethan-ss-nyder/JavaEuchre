package MachineLearning;

import Euchre.Card;
import Euchre.Deck;
import Euchre.EuchreEngine;
import Euchre.Card.Suit;

/**
 * Coin tosser will "toss a coin" on any Euchre-related decision.
 * Pair with a EuchreEngine to limit CoinTosser to legal moves. (Necessary).
 * This class is really just a bot that will make legal moves. For training purposes.
 */
public class CoinTosser {
    
    private EuchreEngine engine;

    public CoinTosser(EuchreEngine engine) {
        this.engine = engine;
    }

    public void setHand(Deck hand, int player) {
        this.engine.playerHands[player] = hand;
    }

    /**
     * Returns a legal, playable card.
     * @return a legal, playable card.
     */
    public int suggestCard(int player) {

        // Only add led-suit cards to temp.
        Deck temp = new Deck();
        temp.add(this.engine.playerHands[player].getDeck());

        for (int i = 0; i < temp.getDeck().length; i++) {
            if (temp.getDeck()[i].suit != this.engine.led) {
                temp.pop(i);
                i--;
            }
        }

        // If CoinTosser can't follow suit, just suggest the first card in the hand
        if (temp.getDeck().length == 0) {
            return 0;
        } else { // If CoinTosser CAN follow suit, return the first card in temp
            return this.engine.playerHands[player].getIndex(temp.takeTopCard());
        }
    }

    /**
     * Returns a non-trump card that can be swapped with the bid card.
     * @param player the player.
     * @return A card.
     */
    public void suggestSwapBidCard(int player) {

        Deck temp = new Deck();
        temp.add(this.engine.playerHands[player].getDeck());

        // Filter trump cards from temp, leaving only non-trump in temp
        for (int i = 0; i < temp.getDeck().length; i++) {
            if (temp.getDeck()[i].suit == this.engine.trump || this.engine.isLeftBower(temp.getDeck()[i])) {
                temp.pop(i);
                i--;
            }
        }

        // If CoinTosser has only trump cards, just take the top card
        if (temp.getDeck().length == 5) {
            this.engine.bidCardSwap = this.engine.playerHands[player].takeTopCard();
        // Otherwise just take the top card in temp
        } else { 
            this.engine.bidCardSwap = temp.takeTopCard();
        }
    }

    /**
     * Toss a coin. Literal 50/50. Are we calling Trump?
     * @return true if CoinTosser is feeling confident. False if not.
     */
    public boolean callTrump() {
        return Math.random() < 0.5;
    }

    /**
     * Automatically updates EuchreEngine with a random trump card
     */
    public void callSuit() {
        int temp = (int)(Math.random() * 3);

        switch (temp) {
            case 0: this.engine.setTrump(Suit.SPADES); break;
            case 1: this.engine.setTrump(Suit.CLUBS); break;
            case 2: this.engine.setTrump(Suit.DIAMONDS); break;
            case 3: this.engine.setTrump(Suit.HEARTS); break;
            default: break;
        }
    }

}
