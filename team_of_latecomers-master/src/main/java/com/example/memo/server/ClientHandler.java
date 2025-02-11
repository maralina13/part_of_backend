package com.example.memo.server;

import com.example.memo.protocol.Message;
import com.example.memo.protocol.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameRoom gameRoom;
    private String playerId;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            while (true) {
                Message message = (Message) in.readObject();
                switch (message.getType()) {
                    case CREATE_ROOM:
                        String roomCode = (String) message.getPayload();
                        gameRoom = ServerMain.createRoom(roomCode);
                        playerId = message.getSenderId();
                        gameRoom.getGameLogic().addPlayer(this);
                        break;
                    case JOIN_ROOM:
                        roomCode = (String) message.getPayload();
                        gameRoom = ServerMain.getRoom(roomCode);
                        if (gameRoom != null) {
                            playerId = message.getSenderId();
                            gameRoom.getGameLogic().addPlayer(this);
                        }
                        break;
                    case PLAYER_ACTION:
                        if (gameRoom != null) {
                            gameRoom.getGameLogic().processPlayerAction(this, message.getPayload());
                        }
                        break;
                    case DISCONNECT:
                        if (gameRoom != null) {
                            gameRoom.getGameLogic().removePlayer(this);
                        }
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerId() {
        return playerId;
    }
}