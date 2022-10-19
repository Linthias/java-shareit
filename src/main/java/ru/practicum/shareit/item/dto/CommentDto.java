package ru.practicum.shareit.item.dto;

/*
Вспомогательный dto объект для комментариев
внутри ItemWBookingsDto
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class CommentDto {
    private int id;
    private String text;
    private String authorName;
    private LocalDateTime created;

    public static CommentDto toCommentDto(Comment comment, String authorName) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(authorName)
                .created(comment.getCreated())
                .build();
    }
}
