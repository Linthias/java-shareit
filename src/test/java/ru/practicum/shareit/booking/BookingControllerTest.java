package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.exceptions.BookingBadPageParamsException;
import ru.practicum.shareit.booking.exceptions.BookingIncompleteDataException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.BookingUnsupportedStatusException;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.MinItemDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService service;

    @Autowired
    private MockMvc mvc;

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    BookingInputDto input = new BookingInputDto(2,
            LocalDateTime.of(2007, 6, 29, 10, 15),
            LocalDateTime.of(2007, 6, 29, 12, 15));

    BookingDto output = BookingDto.builder()
            .id(1)
            .start(LocalDateTime.of(2007, 6, 29, 10, 15))
            .end(LocalDateTime.of(2007, 6, 29, 12, 15))
            .item(new MinItemDto(2, "item"))
            .booker(new BookerDto(3, "booker"))
            .status(BookingStatus.WAITING)
            .build();

    @Test
    void postBookingTest() throws Exception {
        when(service.addBooking(any(), anyInt())).thenReturn(output);

        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(input))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1), Integer.class))
                .andExpect(jsonPath("$.start", is(output.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(output.getEnd().format(formatter))))
                .andExpect(jsonPath("$.item.id", is(output.getItem().getId())))
                .andExpect(jsonPath("$.booker.id", is(output.getBooker().getId())))
                .andExpect(jsonPath("$.status", is(output.getStatus().toString())));
    }

    @Test
    void postBookingTestFail() throws Exception {
        when(service.addBooking(any(), anyInt())).thenThrow(new BookingIncompleteDataException(""));

        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(input))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBookingTestUserNotFound() throws Exception {
        when(service.addBooking(any(), anyInt())).thenThrow(new UserNotFoundException(""));

        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(input))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveBookingTest() throws Exception {
        BookingDto localOutput = BookingDto.builder()
                .id(1)
                .start(LocalDateTime.of(2007, 6, 29, 10, 15))
                .end(LocalDateTime.of(2007, 6, 29, 12, 15))
                .item(new MinItemDto(2, "item"))
                .booker(new BookerDto(3, "booker"))
                .status(BookingStatus.APPROVED)
                .build();

        when(service.approveBooking(anyInt(), anyInt(), anyBoolean())).thenReturn(localOutput);

        mvc.perform(patch("/bookings/" + output.getId())
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1), Integer.class))
                .andExpect(jsonPath("$.start", is(output.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(output.getEnd().format(formatter))))
                .andExpect(jsonPath("$.item.id", is(output.getItem().getId())))
                .andExpect(jsonPath("$.booker.id", is(output.getBooker().getId())))
                .andExpect(jsonPath("$.status", is(localOutput.getStatus().toString())));
    }

    @Test
    void approveBookingTestFail() throws Exception {
        when(service.approveBooking(anyInt(), anyInt(), anyBoolean())).thenThrow(new ItemNotFoundException(""));

        mvc.perform(patch("/bookings/" + output.getId())
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingTest() throws Exception {
        when(service.getBooking(anyInt(), anyInt())).thenReturn(output);

        mvc.perform(get("/bookings/" + output.getId())
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1), Integer.class))
                .andExpect(jsonPath("$.start", is(output.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(output.getEnd().format(formatter))))
                .andExpect(jsonPath("$.item.id", is(output.getItem().getId())))
                .andExpect(jsonPath("$.booker.id", is(output.getBooker().getId())))
                .andExpect(jsonPath("$.status", is(output.getStatus().toString())));
    }

    @Test
    void getBookingTestFail() throws Exception {
        when(service.getBooking(anyInt(), anyInt())).thenThrow(new BookingNotFoundException(""));

        mvc.perform(get("/bookings/" + output.getId())
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookingsTest() throws Exception {
        when(service.getUserBookings(anyInt(), anyString(), anyInt(), anyInt())).thenReturn(List.of(output));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookingsTestFail() throws Exception {
        when(service.getUserBookings(anyInt(), anyString(), anyInt(), anyInt()))
                .thenThrow(new BookingBadPageParamsException(""));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "null")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserItemsBookingsTest() throws Exception {
        when(service.getUserItemsBookings(anyInt(), anyString(), anyInt(), anyInt())).thenReturn(List.of(output));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUserItemsBookingsTestFail() throws Exception {
        when(service.getUserItemsBookings(anyInt(), anyString(), anyInt(), anyInt()))
                .thenThrow(new BookingUnsupportedStatusException(""));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "NOT ALL")
                        .param("from", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
