package com.example.memo.client;

import com.example.memo.protocol.Message;
import com.example.memo.protocol.MessageType;
import com.example.memo.server.GameState;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ClientNetworkHandler extends Thread {
    private String serverHost;
    private int serverPort;
    private String clientId;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private IGameClientUI ui;

    public ClientNetworkHandler(String serverHost, int serverPort, String clientId, IGameClientUI ui) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clientId = clientId;
        this.ui = ui;
    }

    @Override
    public void run() {
        try {
            connectToServer(); // Устанавливаем соединение с сервером
            while (!interrupted()) {
                Message message = (Message) in.readObject();
                processMessage(message);
            }
        } catch (EOFException | StreamCorruptedException eof) {
            // Сервер закрыл соединение
            System.out.println("Сервер закрыл соединение.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException {
        socket = new Socket(serverHost, serverPort);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Подключение к серверу установлено.");
    }

    private void processMessage(Message msg) {
        switch (msg.getType()) {
            case GAME_STATE:
                GameState state = (GameState) msg.getPayload();
                SwingUtilities.invokeLater(() -> ui.updateGameState(state));
                break;
            case GAME_RESULT:
                GameState finalState = (GameState) msg.getPayload();
                SwingUtilities.invokeLater(() -> ui.showGameResult(finalState));
                break;
            case ROOM_CREATED:
            case ROOM_JOINED:
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Комната успешно создана/подключена!"));
                break;
            case ROOM_EXISTS:
            case ROOM_NOT_FOUND:
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Ошибка: комната уже существует или не найдена!"));
                break;
            default:
                break;
        }
    }

    public void sendMessage(Message msg) {
        if (out == null) {
            System.err.println("Ошибка: соединение с сервером не установлено.");
            return;
        }
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}