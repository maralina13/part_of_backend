package com.example.memo.server;

import com.example.memo.protocol.Message;
import com.example.memo.protocol.MessageType;
import com.example.memo.server.Card;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private GameState gameState;
    private List<ClientHandler> clients;

    public GameLogic(GameState gameState) {
        this.gameState = gameState;
        this.clients = new ArrayList<>();
    }

    // Добавление игрока в комнату
    public void addPlayer(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    // Обработка действия игрока
    public void processPlayerAction(ClientHandler clientHandler, Object action) {
        if (action instanceof Integer) {
            int cardIndex = (Integer) action;
            String player = clientHandler.getPlayerId();
            handlePlayerAction(player, cardIndex);
        }
    }

    // Удаление игрока из комнаты
    public void removePlayer(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    // Отправка сообщения всем клиентам
    public void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void handlePlayerAction(String player, int cardIndex) {
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
        String nextPlayer = gameState.getPlayers()[0].equals(currentPlayer) ? gameState.getPlayers()[1] : gameState.getPlayers()[0];
        gameState.setCurrentPlayer(nextPlayer);
        startTurnTimer();
    }

    private void startTurnTimer() {
        // Логика запуска таймера
    }

    private void broadcastGameState() {
        // Отправляем состояние игры всем клиентам
        Message message = new Message(MessageType.GAME_STATE, null, gameState);
        broadcastMessage(message);
    }
}