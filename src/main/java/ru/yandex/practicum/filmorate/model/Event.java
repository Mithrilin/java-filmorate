package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Event {
    private Integer eventId;
    private Integer userId;
    private String eventType;
    private String operation;
    private Integer entityId;
    private final Timestamp timeStamp;

    public Event(Integer eventId, Integer userId, String eventType, String operation, Integer entityId) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
        this.timeStamp = new Timestamp(System.currentTimeMillis());
    }

    public Event(Integer userId, String eventType, String operation, Integer entityId) {
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
        this.timeStamp = new Timestamp(System.currentTimeMillis());
    }
}
