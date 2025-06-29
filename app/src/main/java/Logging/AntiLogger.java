package Logging;

import Euchre.Deck;
import Euchre.Card;

/**
 * Evil counterpart to Logger. This is meant as an off switch to Logger in case a player doesn't want games logged.
 */
public class AntiLogger implements MasterLogger {

    @Override
    public void finishRound() {
    }

    @Override
    public void finishTrick() {
    }

    @Override
    public void recordTrickStateAtBotTurn(double tricksPlayed, double tricksWon, Deck hand, Deck playedCards, Card teamPlayedCard, boolean teamWon, int playOrder) {
    }

    @Override
    public void recordBotPlayInfo(Card playedCard, boolean selfWonTrick, boolean teamWonTrick) {
    }

    @Override
    public void recordRoundInfo(Card.Suit trumpSuit, Card pickedUpCard, boolean selfPickedUp, boolean teamPickedUp, boolean teamWon) {
    }
}
