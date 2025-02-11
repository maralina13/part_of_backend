package com.example.memo.server;

import com.example.memo.protocol.Message;
import com.example.memo.protocol.MessageType;
import com.example.memo.server.Card;

import java.util.Timer;
import java.util.TimerTask;

public class GameRoom {
    private GameState gameState;
    private GameLogic gameLogic;
    private Timer turnTimer;
    private String[] players;

    public GameRoom(String[] players, Card[] cards) {
        this.players = players; // Инициализируем players
        this.gameState = new GameState(cards, players);
        this.gameLogic = new GameLogic(gameState);
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void handlePlayerAction(String player, int cardIndex) {
        if (!player.equals(gameState.getCurrentPlayer())) {
            return; // Не ход этого игрока
        }

        // Проверяем, открыта ли уже эта карточка
        if (gameState.getCards()[cardIndex].isOpen() || gameState.getCards()[cardIndex].isFound()) {
            return;
        }

        gameState.setCardOpen(cardIndex, true);

        // Проверяем, открыты ли две карточки
        int[] openIndices = getOpenCardIndices();
        if (openIndices.length == 2) {
            Card card1 = gameState.getCards()[openIndices[0]];
            Card card2 = gameState.getCards()[openIndices[1]];

            if (card1.getValue() == card2.getValue()) {
                // Карточки совпали
                gameState.incrementScore(player);
                card1.setFound(true);
                card2.setFound(true);
            } else {
                // Карточки не совпали
                gameState.setCardOpen(openIndices[0], false);
                gameState.setCardOpen(openIndices[1], false);
                switchTurn();
            }
        }

        // Передаём состояние игры клиентам
        broadcastGameState();
    }

    private int[] getOpenCardIndices() {
        int count = 0;
        for (boolean isOpen : gameState.getOpenCards()) {
            if (isOpen) count++;
        }
        int[] indices = new int[count];
        int index = 0;
        for (int i = 0; i < gameState.getOpenCards().length; i++) {
            if (gameState.getOpenCards()[i]) {
                indices[index++] = i;
            }
        }
        return indices;
    }

    private void switchTurn() {
        String currentPlayer = gameState.getCurrentPlayer();
        String nextPlayer = players[0].equals(currentPlayer) ? players[1] : players[0];
        gameState.setCurrentPlayer(nextPlayer);
        startTurnTimer();
    }

    private void startTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                switchTurn();
            }
        }, 15000); // 15 секунд
    }

    private void broadcastGameState() {
        // Отправляем состояние игры всем клиентам
        Message message = new Message(MessageType.GAME_STATE, null, gameState);
        // Реализуй отправку сообщения клиентам
        gameLogic.broadcastMessage(message);
    }
}