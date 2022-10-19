package ru.practicum.shareit.item.dto;

/*
Dto объект для добавления нового комментария.
Используется только при получении данных
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentInputDto {
    private String text;
    private LocalDateTime created;

    public static Comment toComment(CommentInputDto commentDto, int itemId, int userId) {
        return Comment.builder()
                .text(commentDto.getText())
                .item(itemId)
                .author(userId)
                .created(commentDto.getCreated())
                .build();
    }
}
