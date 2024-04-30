package edu.northeastern.cs5500.starterbot.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interviewer implements Model {

    private ObjectId id;
    private String name;
    private List<TimeSlot> timeSlots;
    private String experienceLevel;
    private int averageRating;

    // public Interviewer() {
    //     // default constructor
    // }

    // public Interviewer(
    //         ObjectId id,
    //         String name,
    //         List<TimeSlot> timeSlots,
    //         String experienceLevel,
    //         int averageRating) {
    //     this.id = id;
    //     this.name = name;
    //     this.timeSlots = timeSlots;
    //     this.experienceLevel = experienceLevel;
    //     this.averageRating = averageRating;
    // }

    // public static class TimeSlot {
    //     private LocalDateTime startTime;
    //     private LocalDateTime endTime;
    //     private boolean isBooked;

    //     public TimeSlot() {
    //         // default constructor
    //     }

    //     public TimeSlot(LocalDateTime startTime, LocalDateTime endTime, boolean isBooked) {
    //         this.startTime = startTime;
    //         this.endTime = endTime;
    //         this.isBooked = isBooked;
    //     }

    //     // Getters and setters for startTime
    //     public LocalDateTime getStartTime() {
    //         return startTime;
    //     }

    //     public void setStartTime(LocalDateTime startTime) {
    //         this.startTime = startTime;
    //     }

    //     // Getters and setters for endTime
    //     public LocalDateTime getEndTime() {
    //         return endTime;
    //     }

    //     public void setEndTime(LocalDateTime endTime) {
    //         this.endTime = endTime;
    //     }

    //     // Getters and setters for isBooked
    //     public boolean getIsBooked() {
    //         return isBooked;
    //     }

    //     public void setIsBooked(boolean isBooked) {
    //         this.isBooked = isBooked;
    //     }

    //     @Override
    //     public String toString() {
    //         return "TimeSlot{"
    //                 + "startTime="
    //                 + startTime
    //                 + ", endTime="
    //                 + endTime
    //                 + ", isBooked="
    //                 + isBooked
    //                 + '}';
    //     }
    // }
}
