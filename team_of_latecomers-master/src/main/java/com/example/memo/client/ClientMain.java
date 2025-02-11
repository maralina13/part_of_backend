package com.example.memo.client;

import com.example.memo.client.ui.GameStartWindow;

import javax.swing.SwingUtilities;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameStartWindow window = new GameStartWindow();
            window.setVisible(true);
        });
    }
}