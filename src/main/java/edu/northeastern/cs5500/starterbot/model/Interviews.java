package edu.northeastern.cs5500.starterbot.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interviews implements Model {
    private ObjectId id;
    private String interviewerId;
    private Double rating;
    private String notes;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
    private String notificationStatus;
}
