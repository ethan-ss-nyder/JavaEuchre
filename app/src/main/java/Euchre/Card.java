package Euchre;

public class Card {

    public Suit suit;
    public int value;
    public String name;

    public enum Name {
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13),
        ACE(14);

        public final int value;

        private Name(int value) {
            this.value = value;
        }
    }

    public enum Suit {
        SPADES,
        CLUBS,
        HEARTS,
        DIAMONDS
    }

    Card(Card.Name name, Card.Suit suit, int value) {
        this.name = name.toString() + " OF " + suit.toString();
        this.suit = suit;
        this.value = value;
    }
}
