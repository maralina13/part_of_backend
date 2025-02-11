package com.example.memo.server;

import java.util.HashMap;
import java.util.Map;
import com.example.memo.server.Card;

public class GameState {
    private Card[] cards;
    private Map<String, Integer> scores;
    private String currentPlayer;
    private boolean[] openCards;
    private String[] players;

    public GameState(Card[] cards, String[] players) {
        this.cards = cards;
        this.scores = new HashMap<>();
        for (String player : players) {
            scores.put(player, 0);
        }
        this.currentPlayer = players[0];
        this.openCards = new boolean[cards.length];
        this.players = players;
    }

    public Card[] getCards() {
        return cards;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean[] getOpenCards() {
        return openCards;
    }

    public String[] getPlayers() {
        return players;
    }

    public void setCurrentPlayer(String player) {
        this.currentPlayer = player;
    }

    public void setCardOpen(int index, boolean isOpen) {
        this.openCards[index] = isOpen;
    }

    public void incrementScore(String player) {
        scores.put(player, scores.get(player) + 1);
    }

    public boolean hasTwoCardsOpen() {
        int count = 0;
        for (boolean isOpen : openCards) {
            if (isOpen) count++;
        }
        return count == 2;
    }

    public boolean isGameOver() {
        for (Card card : cards) {
            if (!card.isFound()) return false;
        }
        return true;
    }
}