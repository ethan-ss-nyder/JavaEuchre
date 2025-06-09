package Logging;

import Euchre.Card;
import Euchre.Deck;

public interface MasterLogger {
    boolean isLogging();
    void finishRound();
    void finishTrick();
    void recordTrickStateAtBotTurn(double tricksPlayed, double tricksWon, Deck hand, Deck playedCards, Card teamPlayedCard, boolean teamWon, int playOrder);
    void recordBotPlayInfo(Card playedCard, boolean selfWonTrick, boolean teamWonTrick);
    void recordRoundInfo(Card.Suit trumpSuit, Card pickedUpCard, boolean selfPickedUp, boolean teamPickedUp, boolean teamWon);
}
