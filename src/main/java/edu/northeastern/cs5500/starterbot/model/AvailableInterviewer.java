package edu.northeastern.cs5500.starterbot.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableInterviewer {
    private Interviewer interviewer;
    private List<TimeSlot> availableTimeSlots;
}
