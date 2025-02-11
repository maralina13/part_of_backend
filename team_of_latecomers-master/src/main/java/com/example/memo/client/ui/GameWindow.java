package com.example.memo.client.ui;

import com.example.memo.client.ClientNetworkHandler;
import com.example.memo.client.IGameClientUI;
import com.example.memo.protocol.Message;
import com.example.memo.protocol.MessageType;
import com.example.memo.server.Card;
import com.example.memo.server.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GameWindow extends JFrame implements IGameClientUI {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999;

    private ClientNetworkHandler networkHandler;
    private JPanel cardsPanel;
    private JButton[] cardButtons;
    private String clientId;
    private String roomCode;
    private Timer turnTimer;
    private JLabel timerLabel;
    private JLabel currentPlayerLabel;

    // Массив изображений для карточек
    private ImageIcon[] cardImages = {
            new ImageIcon("images/image1.jpg"),
            new ImageIcon("images/image2.jpg"),
            new ImageIcon("images/image3.jpg"),
            new ImageIcon("images/image4.jpg"),
            new ImageIcon("images/image5.jpg"),
            new ImageIcon("images/image6.jpg"),
            new ImageIcon("images/image7.jpg"),
            new ImageIcon("images/image8.jpg")
    };
    private ImageIcon backImage = new ImageIcon("images/back.jpg"); // Рубашка карточки // Рубашка карточки
    private int[] openCardIndices = new int[2]; // Индексы открытых карточек
    private int openCount = 0; // Количество открытых карточек

    public GameWindow(String roomCode, boolean isHost) {
        super("Memo Game Client");
        this.roomCode = roomCode;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Запрашиваем у пользователя ID
        clientId = JOptionPane.showInputDialog(this, "Введите ваш ID:", "Player", JOptionPane.PLAIN_MESSAGE);
        if (clientId == null || clientId.trim().isEmpty()) {
            clientId = "Player" + (int)(Math.random() * 1000);
        }

        // Запускаем сетевое взаимодействие
        networkHandler = new ClientNetworkHandler(SERVER_HOST, SERVER_PORT, clientId, this);
        networkHandler.start();

        // Отправляем сообщение на сервер в зависимости от роли (хост или игрок)
        if (isHost) {
            networkHandler.sendMessage(new Message(MessageType.CREATE_ROOM, clientId, roomCode));
        } else {
            networkHandler.sendMessage(new Message(MessageType.JOIN_ROOM, clientId, roomCode));
        }

        // Панель для отображения карточек
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new GridLayout(4, 4)); // 4x4 сетка для 16 карточек
        add(cardsPanel, BorderLayout.CENTER);

        // Создаём 16 карточек (пока что просто кнопки с рубашкой)
        cardButtons = new JButton[16];
        for (int i = 0; i < 16; i++) {
            cardButtons[i] = createCardButton(i, null);
            cardsPanel.add(cardButtons[i]);
        }

        // Панель для таймера и текущего игрока
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout());
        currentPlayerLabel = new JLabel("Текущий ход: " + clientId);
        infoPanel.add(currentPlayerLabel);
        timerLabel = new JLabel("Осталось времени: 15 сек");
        infoPanel.add(timerLabel);
        add(infoPanel, BorderLayout.NORTH);

        // Кнопка для выхода из игры
        JButton exitButton = new JButton("Выйти");
        exitButton.addActionListener(e -> {
            Message disconnectMsg = new Message(MessageType.DISCONNECT, clientId, null);
            networkHandler.sendMessage(disconnectMsg);
            dispose();
            System.exit(0);
        });
        add(exitButton, BorderLayout.SOUTH);
    }

    @Override
    public void updateGameState(GameState state) {
        // Обновляем интерфейс в потоке обработки событий Swing
        SwingUtilities.invokeLater(() -> {
            rebuildCardPanel(state);
            currentPlayerLabel.setText("Текущий ход: " + state.getCurrentPlayer());
            startTurnTimer(); // Перезапускаем таймер при обновлении состояния
        });
    }

    @Override
    public void showGameResult(GameState finalState) {
        // Отображаем итоги игры в диалоговом окне
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("Итоги игры:\n");
            for (Map.Entry<String, Integer> entry : finalState.getScores().entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void rebuildCardPanel(GameState state) {
        // Очищаем панель с карточками
        cardsPanel.removeAll();

        // Получаем карточки из состояния игры
        Card[] cards = state.getCards();
        if (cards == null) return;

        // Создаём кнопки для карточек
        cardButtons = new JButton[cards.length];
        for (int i = 0; i < cards.length; i++) {
            cardButtons[i] = createCardButton(i, cards[i]);
            cardsPanel.add(cardButtons[i]);
        }

        // Обновляем панель
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JButton createCardButton(int index, Card card) {
        JButton button = new JButton();
        button.setIcon(backImage); // Устанавливаем рубашку карточки по умолчанию

        if (card != null) {
            if (card.isFound()) {
                // Карточка найдена, показываем её изображение
                button.setIcon(cardImages[card.getValue() - 1]);
                button.setEnabled(false); // Карточка найдена, кнопка неактивна
            } else if (card.isOpen()) {
                // Карточка открыта, показываем её изображение
                button.setIcon(cardImages[card.getValue() - 1]);
            } else {
                // Карточка закрыта, показываем рубашку
                button.setIcon(backImage);
            }
        }

        // Обработка клика по карточке
        button.addActionListener(e -> {
            flipCard(button, index);
        });

        return button;
    }

    private void flipCard(JButton button, int cardIndex) {
        // Проверяем, можно ли перевернуть карточку
        if (openCount >= 2 || openCardIndices[0] == cardIndex) {
            return; // Не переворачиваем, если уже две карточки открыты или кликнули на ту же карточку
        }

        // Сохраняем индекс открытой карточки
        openCardIndices[openCount] = cardIndex;
        openCount++;

        // Показываем изображение карточки с анимацией
        button.setIcon(cardImages[cardButtons[cardIndex].getText().charAt(0) - '1']);
        try {
            Thread.sleep(200); // Задержка 200 мс
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        // Обновляем состояние карточки на сервере
        Message actionMsg = new Message(MessageType.PLAYER_ACTION, clientId, cardIndex);
        networkHandler.sendMessage(actionMsg);

        // Если открыто две карточки, проверяем их
        if (openCount == 2) {
            checkCards();
        }
    }

    private void checkCards() {
        // Получаем индексы открытых карточек
        int index1 = openCardIndices[0];
        int index2 = openCardIndices[1];

        // Отправляем сообщение на сервер о действии игрока
        Message actionMsg = new Message(MessageType.PLAYER_ACTION, clientId, index1);
        networkHandler.sendMessage(actionMsg);
        actionMsg = new Message(MessageType.PLAYER_ACTION, clientId, index2);
        networkHandler.sendMessage(actionMsg);

        // Сбрасываем счётчик открытых карточек
        openCount = 0;
    }

    private int getCardIndex(JButton button) {
        for (int i = 0; i < cardButtons.length; i++) {
            if (cardButtons[i] == button) {
                return i;
            }
        }
        return -1;
    }

    private void startTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        turnTimer = new Timer();
        turnTimer.scheduleAtFixedRate(new TimerTask() {
            int timeLeft = 15;

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    timerLabel.setText("Осталось времени: " + timeLeft + " сек");
                    if (timeLeft == 0) {
                        // Ход переходит другому игроку
                        networkHandler.sendMessage(new Message(MessageType.TURN_SWITCH, clientId, null));
                        turnTimer.cancel();
                    }
                    timeLeft--;
                });
            }
        }, 0, 1000); // Обновляем каждую секунду
    }
}