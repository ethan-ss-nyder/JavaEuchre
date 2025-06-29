package Euchre;

import Euchre.Card.Suit;

/**
 * EuchreEngine holds all the logic for the game of Euchre and can give information about the state of the game.
 */
public class EuchreEngine {

    public Card.Suit trump;
    public Card.Suit led;
    public Card bidCard;

    public int[] trickScore = {0, 0}; // First element is the tricks taken by team one, second element is tricks by team two
    public int teamOneScore;
    public int teamTwoScore;

    public int leader; // Identifies the player 0-3 who is leading
    public int dealer; // Identifies the player 0-3 who is the dealer

    public Deck deck; // Entire deck, all 24 cards
    private Deck playedCards; // All cards played throughout all tricks
    public Deck trickPlayedCards; // All cards played during the trick

    // All player hands
    private Deck playerZeroHand;
    private Deck playerOneHand;
    private Deck playerTwoHand;
    private Deck playerThreeHand;
    public Deck[] playerHands;

    public int offense = -1; // 0 for team 1, 1 for team 2. Sorry

    public Card bidCardSwap;

    /**
     * Initializes a Euchre game.
     */
    public void init() {
        this.deck = new Deck();
        this.deck.buildEuchreDeck();

        this.trump = null;
        this.led = null;
        this.bidCard = null;
        this.leader = -1;
        this.dealer = -1;
        this.teamOneScore = 0;
        this.teamTwoScore = 0;

        this.trickPlayedCards = new Deck();
        this.playedCards = new Deck();
        this.playerZeroHand = new Deck();
        this.playerOneHand = new Deck();
        this.playerTwoHand = new Deck();
        this.playerThreeHand = new Deck();
        this.playerHands = new Deck[] {playerZeroHand, playerOneHand, playerTwoHand, playerThreeHand};
    }

    /**
     * Tells the engine which suit is trump.
     * @param suit
     */
    public void setTrump(Card.Suit suit) {
        this.trump = suit;
    }

    /**
     * Tells the engine which team said "pick it up".
     * @param team the team which said "pick it up". 0 is team with players 0 and 2, 1 is the team with players 1 and 3.
     */
    public void setOffense(int team) {
        this.offense = team;
    }

    /**
     * Something buggy was happening with this, so now it has to happen manually.
     */
    public void resetLedSuit() {
        this.led = null;
    }

    public void resetAfterTricks() {
        this.deck = new Deck();
        this.deck.buildEuchreDeck();

        this.trickScore[0] = 0;
        this.trickScore[1] = 0;
        this.offense = -1;
    }

    /**
     * Updates scores after a round of five tricks has been played.
     */
    public void updateScore() {
        int[] score = this.getRoundWinner();
        if (score[0] == 0) {
            this.teamOneScore += score[1];
        } else {
            this.teamTwoScore += score[1];
        }
    }
    
    /**
     * Give the engine a single played card to add to an internal list of played cards.
     * 
     * @param card: the played card.
     */
    public void giveUsedCard(Card card) {
        this.trickPlayedCards.add(card);
        this.playedCards.add(card);
    }

    /**
     * Give the engine a many played cards to add to an internal list of played cards.
     * 
     * @param cards: the played cards.
     */
    public void giveUsedCards(Card[] cards) {
        this.playedCards.add(cards);
    }

    /**
     * Give the engine a player's current hand.
     * 
     * @param cards: a player's current hand.
     */
    public void giveHand(Card[] cards) {
        this.playerZeroHand.add(cards);
    }

    /**
     * Removes a card from a players hand and gives it to the engine.
     * @param player The player to remove the card from.
     * @param card The card to be played.
     */
    public void playCard(int player, int cardIndex) {
        this.giveUsedCard(this.playerHands[player].pop(cardIndex));

        // After submitting the played card, if it's the first card, fix the `led` variable
        if (trickPlayedCards.getDeck().length == 1) {
            if (this.isLeftBower(this.trickPlayedCards.getDeck()[0])) {
                switch (this.trickPlayedCards.getDeck()[0].suit) {
                    case SPADES: this.led = Suit.CLUBS; break;
                    case CLUBS: this.led = Suit.SPADES; break;
                    case DIAMONDS: this.led = Suit.HEARTS; break;
                    case HEARTS: this.led = Suit.DIAMONDS; break;
                }
            } else {
                this.led = this.trickPlayedCards.getDeck()[0].suit;
            }
        }
    }

    /**
     * Clears the trick's played cards in the probable case that you want to clear
     * the cache, you know?
     */
    public void clearPlayedCards() {
        this.trickPlayedCards = new Deck();
    }

    /**
     * Swaps the specified card from a players hand with the bid card.
     * @param playerCard the specified card.
     */
    public void swapBidCard() {
        outerLoop:
        for (Deck playerHand : this.playerHands) {
            for (Card card : playerHand.getDeck()) {
                if (card.name.equals(this.bidCardSwap.name)) {
                    Card tempCard = playerHand.pop(playerHand.getIndex(card));
                    playerHand.add(this.bidCard);
                    this.deck.add(tempCard);
                    break outerLoop;
                }
            }
        }
    }

    /**
     * Returns the player who won the trick. Optionally updates the score.
     * @param updateScore true if you wish for the trickScore to update.
     * @return the player number who won the trick.
     */
    public int getWinner(boolean updateScore) {
    int winner = this.leader;
    Card bestCard = trickPlayedCards.getDeck()[0];

    for (int i = 0; i < 4; i++) {
        int player = (this.leader + i) % 4;

        if (this.cardRank(trickPlayedCards.getDeck()[i]) > this.cardRank(bestCard)) {
            winner = player;
            bestCard = trickPlayedCards.getDeck()[i];
        }
    }

    if (updateScore) {
        this.trickScore[winner % 2] += 1;
    }

    return winner;
}

    /**
     * Helper to check if card is Left Bower (jack of same color as trump)
     */
    public boolean isLeftBower(Card card) {
        if (card.value != 11) return false;
        return (trump == Card.Suit.CLUBS && card.suit == Card.Suit.SPADES) ||
               (trump == Card.Suit.SPADES && card.suit == Card.Suit.CLUBS) ||
               (trump == Card.Suit.HEARTS && card.suit == Card.Suit.DIAMONDS) ||
               (trump == Card.Suit.DIAMONDS && card.suit == Card.Suit.HEARTS);
    }

    // Simplified rank order for Euchre cards (higher number = better)
    private int cardRank(Card card) {
        // If card is trump suit...
        if (card.value == 11 && card.suit == this.trump) return 21;  // Right Bower
        if (isLeftBower(card)) return 20; // Left Bower
        if (card.suit == trump) {
            switch(card.value) {
                case 14: return 19;
                case 13: return 18;
                case 12: return 17;
                case 10: return 16;
                case 9: return 15;
            }
        // If it's not trump and not following suit, it's junk
        } else if (card.suit != this.led) {
            return 0;
        // If it's not trump and it's not junk, it's a valid card that follows suit, do not alter value
        } else {
            return card.value;
        }
        return 0;
    }

    /**
     * Returns round winner information.
     * @return [teamToBeAwardedPoints, #ofPoints]
     */
    public int[] getRoundWinner() {
        if (offense == 0) {
            if (this.trickScore[0] > this.trickScore[1]) { // Win condition
                if (this.trickScore[0] == 5) { // Sweep condition
                    return new int[] {0, 2};
                } else { // Non-sweep condition
                    return new int[] {0, 1};
                }
            } else { // Set condition
                return new int[] {1, 2};
            }
        } else {
            if (this.trickScore[1] > this.trickScore[0]) { // Win condition
                if (this.trickScore[1] == 5) { // Sweep condition
                    return new int[] {1, 2};
                } else { // Non-sweep condition
                    return new int[] {1, 1};
                }
            } else { // Set condition
                return new int[] {0, 2};
            }
        }
    }

    /**
     * Assuming a current giveRound() and giveHand(), returns a boolean and card relating to the player being able to follow suit.
     * @return an Object[] composed of a boolean (true or false, can follow suit?) and a Deck of Cards that can follow suit.
     */
    public boolean canFollowSuit(Deck hand) {
        for (Card card : hand.getDeck()) {
            if (card.suit == led) return true;
        }
        return false;
    }
}
