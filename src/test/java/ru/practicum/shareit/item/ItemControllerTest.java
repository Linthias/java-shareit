package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.MinBookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWBookingsDto;
import ru.practicum.shareit.item.exceptions.ItemAccessRestrictException;
import ru.practicum.shareit.item.exceptions.ItemBadPageParamsException;
import ru.practicum.shareit.item.exceptions.ItemIncompleteDataException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService service;

    @Autowired
    private MockMvc mvc;

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    CommentInputDto commentInput = new CommentInputDto("comment",
            LocalDateTime.of(2007, 6, 29, 10, 15));

    CommentDto commentOutput = CommentDto.builder()
            .id(1)
            .text("comment")
            .authorName("author")
            .created(LocalDateTime.of(2007, 6, 29, 10, 15))
            .build();

    ItemDto minItem = ItemDto.builder()
            .id(2)
            .name("item")
            .description("description")
            .available(true)
            .requestId(3)
            .build();

    ItemWBookingsDto fullItem = ItemWBookingsDto.builder()
            .id(2)
            .name("item")
            .description("description")
            .available(true)
            .lastBooking(new MinBookingDto(4, 5))
            .nextBooking(new MinBookingDto(6, 7))
            .comments(List.of(commentOutput))
            .build();

    @Test
    void postItemTest() throws Exception {
        when(service.addItem(any(), anyInt())).thenReturn(minItem);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(minItem))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(minItem.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(minItem.getName())))
                .andExpect(jsonPath("$.description", is(minItem.getDescription())))
                .andExpect(jsonPath("$.available", is(minItem.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(minItem.getRequestId())));
    }

    @Test
    void postItemTestFail() throws Exception {
        when(service.addItem(any(), anyInt())).thenThrow(new UserNotFoundException(""));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(minItem))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void postCommentTest() throws Exception {
        when(service.addComment(any(), anyInt(), anyInt())).thenReturn(commentOutput);

        mvc.perform(post("/items/" + minItem.getId() + "/comment")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(commentInput))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentOutput.getId()), Integer.class))
                .andExpect(jsonPath("$.text", is(commentOutput.getText())))
                .andExpect(jsonPath("$.authorName", is(commentOutput.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentOutput.getCreated().format(formatter))));
    }

    @Test
    void postCommentTestFail() throws Exception {
        when(service.addComment(any(), anyInt(), anyInt())).thenThrow(new ItemIncompleteDataException(""));

        mvc.perform(post("/items/" + minItem.getId() + "/comment")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(commentInput))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchItemTest() throws Exception {
        ItemDto localMinItem = ItemDto.builder()
                .id(2)
                .name("update item")
                .description("update description")
                .available(true)
                .requestId(3)
                .build();

        when(service.updateItem(any(), anyInt(), anyInt())).thenReturn(localMinItem);

        mvc.perform(patch("/items/" + localMinItem.getId())
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(minItem))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(localMinItem.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(localMinItem.getName())))
                .andExpect(jsonPath("$.description", is(localMinItem.getDescription())))
                .andExpect(jsonPath("$.available", is(localMinItem.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(localMinItem.getRequestId())));
    }

    @Test
    void patchItemTestFail() throws Exception {
        when(service.updateItem(any(), anyInt(), anyInt())).thenThrow(new ItemAccessRestrictException(""));

        mvc.perform(patch("/items/" + minItem.getId())
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(minItem))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getItemTest() throws Exception {
        when(service.getItem(anyInt(), anyInt())).thenReturn(fullItem);

        mvc.perform(get("/items/" + fullItem.getId())
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(fullItem.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(fullItem.getName())))
                .andExpect(jsonPath("$.description", is(fullItem.getDescription())))
                .andExpect(jsonPath("$.available", is(fullItem.getAvailable())));
    }

    @Test
    void getItemTestFail() throws Exception {
        when(service.getItem(anyInt(), anyInt())).thenThrow(new ItemNotFoundException(""));

        mvc.perform(get("/items/" + fullItem.getId())
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserItemsTest() throws Exception {
        when(service.getAllItems(anyInt(), anyInt(), anyInt())).thenReturn(List.of(fullItem));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUserItemsTestFail() throws Exception {
        when(service.getAllItems(anyInt(), anyInt(), anyInt())).thenThrow(new ItemBadPageParamsException(""));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "null")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchForItemsTest() throws Exception {
        when(service.searchItems(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(List.of(minItem));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "request")
                        .param("from", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
