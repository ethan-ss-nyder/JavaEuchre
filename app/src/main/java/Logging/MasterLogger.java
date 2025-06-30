package Logging;

import Euchre.Card;
import Euchre.Deck;

public interface MasterLogger {
    void finishRound();
    void finishTrick();
    void recordTrickStateAtBotTurn(int tricksPlayed, int tricksWon, Deck hand, Deck playedCards, Card teamPlayedCard, boolean teamWon, int playOrder);
    void recordBotPlayInfo(Card playedCard, boolean selfWonTrick, boolean teamWonTrick);
    void recordRoundInfo(Card.Suit trumpSuit, Card pickedUpCard, boolean selfPickedUp, boolean teamPickedUp, boolean teamWon);
}
