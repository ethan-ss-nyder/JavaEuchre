package Logging;

/**
 * Logger is a class that handles IO to a .jsonl file that contains information about tricks.
 * Logger can create a new .jsonl or append to an existing one. It may be wise to create new .jsonl files when different training techniques are tried.
 * 
 * All functions beginning with "record" should be written to before running all functions beginning with "finish".
 */

import Euchre.Card;
import Euchre.Deck;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Logger implements MasterLogger {

    String fileName = "";
    int ID; // ID is synonymous with line number in the .jsonl file
    
    JSONObject round;
    JSONArray tricks;
    JSONObject trick;
    
    Logger(String fileName) {
        this.fileName = fileName;
        this.ID = this.getLineCount();
        this.round = new JSONObject();
        this.tricks = new JSONArray();
        this.trick = new JSONObject();
    }

    /**
     * This helps Prompter tell which logger is active.
     */
    public boolean isLogging() {
        return true;
    }

    /**
     * Ends the current round and writes round and all trick data to .jsonl file.
     */
    public void finishRound() {
        round.put("Tricks", tricks);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.fileName, true))) {
            bw.write(this.round.toString());
            bw.newLine();
            bw.close();
            this.ID++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ends the current trick and adds trick data to the current round.
     */
    public void finishTrick() {
        tricks.put(trick);
        trick.clear();
    }

    /**
     * Records trick state information at the time of the bot's turn.
     * 
     * @param tricksPlayed Total number of tricks played previous to the current trick being built divided by 5.
     * @param tricksWon Total number of tricks won previous to the current trick being built divided by 5.
     * @param hand The bot's hand before play.
     * @param playedCards All played cards present in play at the time of the bot's turn.
     * @param teamPlayedCard The card the bot's teammate has played. Can be null.
     * @param teamWon True is bot's teammate has won the trick so far. False if not.
     * @param playOrder An integer representing the bot's play turn, 0-3.
     */
    public void recordTrickStateAtBotTurn(double tricksPlayed, double tricksWon, Deck hand, Deck playedCards, Card teamPlayedCard, boolean teamWon, int playOrder) {
        trick.put("TricksTotal", tricksPlayed);
        trick.put("TricksWon", tricksWon);
        trick.put("Hand", hand);
        trick.put("PlayedCards", playedCards);
        trick.put("HasTeamPlayed", teamPlayedCard);
        trick.put("HasTeamWon", teamWon);
        trick.put("PlayOrder", playOrder);
    }

    /**
     * Records trick state information pertaining to the bot's play after their turn.
     * 
     * @param playedCard The card the bot played during their turn.
     * @param selfWonTrick True if the bot won the trick. False if not.
     * @param teamWonTrick True if the bot's teammate won the trick. False if not.
     */
    public void recordBotPlayInfo(Card playedCard, boolean selfWonTrick, boolean teamWonTrick) {
        trick.put("BotPlayedCard", playedCard);
        trick.put("BotWonTrick", selfWonTrick);
        trick.put("DidTeamWinTrick", teamWonTrick);
    }

    /**
     * Records round information after round has been played.
     * 
     * @param trumpSuit A Card.Suit suit objects representing the trump suit played in the game.
     * @param pickedUpCard A Card object corresponding to the card picked up when trump was called.
     * @param selfPickedUp True if the bot called trump. False if not.
     * @param teamPickedUp True if the bot's teammate called trump. False if not.
     * @param teamWon True if the bot's team won the round. False if not.
     */
    public void recordRoundInfo(Card.Suit trumpSuit, Card pickedUpCard, boolean selfPickedUp, boolean teamPickedUp, boolean teamWon) {
        round.put("ID", this.ID);
        round.put("TrumpSuit", trumpSuit);
        round.put("PickedUpCard", pickedUpCard);
        round.put("DidBotPickUp", selfPickedUp);
        round.put("DidTeammatePickUp", teamPickedUp);
        round.put("DidTeamWin", teamWon);
    }

    /**
     * Reads the file given to Logger during creation and returns the line count.
     * @return the line count of the file name given to Logger as an integer.
     */
    private int getLineCount() {
        int i = 0;
        try (BufferedReader bw = new BufferedReader(new FileReader(this.fileName))) {
            while (bw.readLine() != null) i++;
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }
}


// Log trick info
//this.logger.recordBotPlayInfo(this.engine.trickPlayedCards.getDeck()[2], winner == 2, winner == 2 || winner == 0);