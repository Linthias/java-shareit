package ru.practicum.shareit.item.dto;

/*
Класс-маппер для Comment:
Comment -> CommentDto
Comment <- CommentInputDto
 */

import ru.practicum.shareit.item.model.Comment;

public class CommentDtoMapper {
    public static CommentDto toCommentDto(Comment comment, String authorName) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(authorName)
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(CommentInputDto commentDto, int itemId, int userId) {
        return Comment.builder()
                .text(commentDto.getText())
                .item(itemId)
                .author(userId)
                .created(commentDto.getCreated())
                .build();
    }
}
