package ru.practicum.shareit.item.dto;

/*
Dto объект для добавления нового комментария.
Используется только при получении данных
 */

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentInputDto {
    private String text;
    private LocalDateTime created;
}
