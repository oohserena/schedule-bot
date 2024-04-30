package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class TimeZoneController {
    public static final ZoneId DB_ZONE_ID = ZoneOffset.UTC;
    public static final ZoneId USER_ZONE_ID = ZoneId.of("America/Los_Angeles");

    @Inject OpenTelemetry openTelemetry;

    @Inject
    public TimeZoneController() {
        // Empty and public for Dagger
    }

    /**
     * Convert a user's local time to the database time
     *
     * @param userLocalDateTime the user's local time
     * @return the database time
     */
    public LocalDateTime toDatabaseTime(LocalDateTime userLocalDateTime) {
        ZonedDateTime zonedUserTime = userLocalDateTime.atZone(USER_ZONE_ID);
        return zonedUserTime.withZoneSameInstant(DB_ZONE_ID).toLocalDateTime();
    }

    /**
     * Convert a database time to the user's local time
     *
     * @param dbLocalDateTime the database time
     * @return the user's local time
     */
    public LocalDateTime toUserLocalTime(LocalDateTime dbLocalDateTime) {
        ZonedDateTime zonedDBTime = dbLocalDateTime.atZone(DB_ZONE_ID);
        return zonedDBTime.withZoneSameInstant(USER_ZONE_ID).toLocalDateTime();
    }
}
