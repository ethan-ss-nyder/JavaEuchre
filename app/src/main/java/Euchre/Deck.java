package Euchre;

import java.util.Arrays;

public class Deck {

    private Card[] deck;

    public Deck() {
        this.deck = new Card[0];
    }

    /**
     * Returns the deck as an array of Cards.
     * @return the deck as an array of Cards.
     */
    public Card[] getDeck() {
        return deck;
    }

    /**
     * Prints the current deck to the terminal in a neat array format.
     */
    public void print() {
        String[] temp = new String[this.deck.length];

        int i = 0;
        for (Card card : this.deck) {
            temp[i] = card.name;
            i++;
        }

        System.out.println(Arrays.toString(temp));
    }

    /**
     * Removes top card from deck and returns it.
     * 
     * @return top card of the deck.
     */
    public Card takeTopCard() {
        return pop(this.deck.length - 1);
    }

    public void buildStandardDeck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Name name : Card.Name.values()) {
                this.add(new Card(name, suit, name.value));
            }
        }
    }

    public void buildEuchreDeck() {
        this.buildStandardDeck();

        int i = 0;
        for (Card card : deck) {
            if (card.value < 9) {
                this.pop(i);
            } else {
                i++;
            }
        }
    }

    public void buildCustomDeck(Card[] cards) {
        int i = 0;
        for (Card card : cards) {
            this.deck[i] = card;
            i++;
        }
    }

    public void shuffle() {
        Card[] copyDeck = new Card[this.deck.length];

        int i = 0;
        while (this.deck.length > 0) {
            int randIndex = (int)Math.round(Math.random() * (this.deck.length - 1)); // Generate a random number between 0 and the deck size.
            copyDeck[i] = this.pop(randIndex);

            i++;
        }

        this.deck = copyDeck;
    }

    /**
     * Finds the index of a given card.
     * @param card The card whose index you want
     * @return The index of the card. -1 if not found.
     */
    public int getIndex(Card card) {
        int i = 0;
        for (Card x : this.deck) {
            if (x.equals(card)) {
                return i;
            } else {
                i++;
            }
        }
        return -1;
    }

    public Card pop(int index) {

        Card[] copyDeck;

        // If a user is trying to pop from an array length 1, just return that last element.
        if (this.deck.length == 1) {
            return this.pop();
        // Otherwise define a new length array for copyDeck
        } else {
            copyDeck = new Card[this.deck.length - 1];
        }

        // Copy the deck before the poppable index
        for (int i = 0; i < index; i++) {
            copyDeck[i] = this.deck[i];
        }

        // Copy the deck after the poppable index
        for (int i = index + 1; i < this.deck.length; i++) {
            copyDeck[i - 1] = this.deck[i];
        }

        Card returnable = this.deck[index]; // Store the index that didn't get copied
        this.deck = copyDeck; // Overwrite the old deck with the new one
        return returnable;
    }

    public Card pop() {
        Card[] copyDeck = new Card[this.deck.length - 1];

        for (int i = 0; i < this.deck.length - 1; i++) {
            copyDeck[i] = this.deck[i];
        }

        Card returnable = this.deck[this.deck.length - 1];
        this.deck = copyDeck;
        return returnable;
    }

    public void add(Card card) {
        Card[] copyDeck = new Card[this.deck.length + 1];

        int i = 0;
        for (Card x : this.deck) {
            copyDeck[i] = x;
            i++;
        }

        copyDeck[i] = card;
        this.deck = copyDeck;
    }

    public void add(Card[] cards) {
        Card[] copyDeck = new Card[this.deck.length + cards.length];

        int i = 0;
        for (Card x : this.deck) {
            copyDeck[i] = x;
            i++;
        }

        i = 0;
        for (Card x : cards) {
            copyDeck[i] = x;
            i++;
        }

        this.deck = copyDeck;
    }

    public boolean isEmpty() {
        for (Card card : this.deck) {
            if (card != null) {
                return false;
            }
        }
        return true;
    }
}