package com.example.memo.server;

public class Card {
    private int value; // Значение карточки (от 1 до 8)
    private boolean isOpen; // Открыта ли карточка
    private boolean isFound; // Найдена ли карточка (пара)

    public Card(int value) {
        this.value = value;
        this.isOpen = false;
        this.isFound = false;
    }

    public int getValue() {
        return value;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isFound() {
        return isFound;
    }

    public void setFound(boolean found) {
        isFound = found;
    }
}