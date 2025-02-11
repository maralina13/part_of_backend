package com.example.memo.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameStartWindow extends JFrame {
    private JTextField codeField;
    private JButton createButton;
    private JButton joinButton;

    public GameStartWindow() {
        super("Memo Game - Start");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JLabel label = new JLabel("Введите код комнаты:");
        panel.add(label);

        codeField = new JTextField();
        panel.add(codeField);

        JPanel buttonPanel = new JPanel();
        createButton = new JButton("Создать игру");
        joinButton = new JButton("Присоединиться к игре");
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);
        panel.add(buttonPanel);

        add(panel, BorderLayout.CENTER);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomCode = codeField.getText();
                if (!roomCode.isEmpty()) {
                    dispose();
                    new GameWindow(roomCode, true).setVisible(true);
                }
            }
        });

        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomCode = codeField.getText();
                if (!roomCode.isEmpty()) {
                    dispose();
                    new GameWindow(roomCode, false).setVisible(true);
                }
            }
        });
    }
}