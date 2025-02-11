package com.example.memo.client;

import com.example.memo.server.GameState;

public interface IGameClientUI {
    void updateGameState(GameState state);
    void showGameResult(GameState finalState);
}