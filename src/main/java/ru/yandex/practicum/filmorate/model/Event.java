package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Event {
    private Integer eventId;
    @NotNull
    private Integer userId;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NotNull
    private Integer entityId;
    @NotNull
    private final Long timestamp;

    public Event(Integer eventId, Integer userId, String eventType, String operation, Integer entityId, Long timeStamp) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
        this.timestamp = timeStamp;
    }

    public Event(Integer userId, String eventType, String operation, Integer entityId) {
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
        this.timestamp = System.currentTimeMillis();
    }
}
