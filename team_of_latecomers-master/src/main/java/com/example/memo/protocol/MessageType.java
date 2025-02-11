package com.example.memo.protocol;

public enum MessageType {
    CONNECT,        // Подключение клиента
    CREATE_ROOM,    // Создание новой комнаты
    JOIN_ROOM,      // Подключение к существующей комнате
    ROOM_CREATED,   // Комната успешно создана
    ROOM_EXISTS,    // Комната с таким кодом уже существует
    ROOM_JOINED,    // Успешное подключение к комнате
    ROOM_NOT_FOUND, // Комната не найдена
    GAME_STATE,     // Текущее состояние игры (от сервера клиенту)
    PLAYER_ACTION,  // Действие игрока (от клиента серверу)
    GAME_RESULT,    // Итог игры (от сервера клиенту)
    TURN_SWITCH,    // Смена хода
    DISCONNECT      // Отключение клиента
}