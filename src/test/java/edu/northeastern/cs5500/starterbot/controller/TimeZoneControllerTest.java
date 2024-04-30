package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TimeZoneControllerTest {
    TimeZoneController timeZoneController = new TimeZoneController();

    @BeforeEach
    @Test
    public void testToDatabaseTime() {
        // Setup: Define a PST time
        LocalDateTime pstTime = LocalDateTime.of(2024, 4, 1, 10, 0);
        LocalDateTime expectedUtcTime = LocalDateTime.of(2024, 4, 1, 17, 0); // PST is UTC-7
        LocalDateTime actualUtcTime = timeZoneController.toDatabaseTime(pstTime);

        assertThat(actualUtcTime).isEqualTo(expectedUtcTime);
    }

    @Test
    public void testToUserLocalTime() {
        // Setup: Define a UTC time
        LocalDateTime utcTime = LocalDateTime.of(2024, 4, 1, 17, 0);
        LocalDateTime expectedPstTime = LocalDateTime.of(2024, 4, 1, 10, 0); // PST is UTC-7
        LocalDateTime actualPstTime = timeZoneController.toUserLocalTime(utcTime);

        assertThat(actualPstTime).isEqualTo(expectedPstTime);
    }
}
