package com.example.memo.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.example.memo.server.Card;

public class ServerMain {
    private static Map<String, GameRoom> rooms = new HashMap<>(); // Хранилище комнат

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            System.out.println("Сервер запущен на порту 9999");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новый клиент подключён: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Создание новой комнаты
    public static GameRoom createRoom(String roomCode) {
        Card[] cards = generateCards(); // Генерация карточек
        String[] players = new String[2]; // Игроки будут добавлены позже
        GameRoom gameRoom = new GameRoom(players, cards);
        rooms.put(roomCode, gameRoom);
        return gameRoom;
    }

    // Получение комнаты по коду
    public static GameRoom getRoom(String roomCode) {
        return rooms.get(roomCode);
    }

    private static Card[] generateCards() {
        Card[] cards = new Card[16];
        Random random = new Random();
        int[] values = new int[8];
        for (int i = 0; i < 8; i++) {
            values[i] = i + 1;
        }
        for (int i = 0; i < 16; i++) {
            int valueIndex = random.nextInt(values.length);
            cards[i] = new Card(values[valueIndex]);
            values[valueIndex] = values[values.length - 1];
            values = java.util.Arrays.copyOf(values, values.length - 1);
        }
        return cards;
    }
}