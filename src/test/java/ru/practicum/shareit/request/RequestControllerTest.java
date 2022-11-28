package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.exceptions.ItemRequestBadDataException;
import ru.practicum.shareit.request.exceptions.ItemRequestBadPageParams;
import ru.practicum.shareit.request.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class RequestControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService service;

    @Autowired
    private MockMvc mvc;

    ItemRequestInputDto input = new ItemRequestInputDto("description",
            LocalDateTime.of(2007, 6, 29, 10, 15));
    ItemRequestDto output = ItemRequestDto.builder()
            .id(1)
            .description("description")
            .created(LocalDateTime.of(2007, 6, 29, 10, 15))
            .items(new ArrayList<>())
            .build();


    @Test
    void postRequestTest() throws Exception {
        when(service.addRequest(any(), anyInt())).thenReturn(output);

        mvc.perform(post("/requests")
                .header("X-Sharer-User-Id", 1)
                .content(mapper.writeValueAsString(input))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1), Integer.class))
                .andExpect(jsonPath("$.description", is(output.getDescription())));
    }

    @Test
    void postRequestTestFail() throws Exception {
        when(service.addRequest(any(), anyInt())).thenThrow(new ItemRequestBadDataException(""));

        mvc.perform(post("/requests")
                .header("X-Sharer-User-Id", 1)
                .content(mapper.writeValueAsString(input))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestTest() throws Exception {
        when(service.getRequest(anyInt(), anyInt())).thenReturn(output);

        mvc.perform(get("/requests/" + output.getId())
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(input))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1), Integer.class))
                .andExpect(jsonPath("$.description", is(output.getDescription())));
    }

    @Test
    void getRequestTestFail() throws Exception {
        when(service.getRequest(anyInt(), anyInt())).thenThrow(new ItemRequestNotFoundException(""));

        mvc.perform(get("/requests" + output.getId())
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(input))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserRequestsTest() throws Exception {
        when(service.getAuthorRequests(anyInt())).thenReturn(List.of(output));

        mvc.perform(get("/requests")
                .header("X-Sharer-User-Id", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUserRequestsTestFail() throws Exception {
        when(service.getAuthorRequests(anyInt())).thenThrow(new UserNotFoundException(""));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOtherUsersRequestsTest() throws Exception {
        when(service.getOtherUsersRequests(anyInt(), anyInt(), anyInt())).thenReturn(List.of(output));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getOtherUsersRequestsTestFail() throws Exception {
        when(service.getOtherUsersRequests(anyInt(), any(), anyInt())).thenThrow(new ItemRequestBadPageParams(""));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
