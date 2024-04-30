package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.AvailableInterviewer;
import edu.northeastern.cs5500.starterbot.model.Interviewer;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.model.TimeSlot;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.FakeOpenTelemetryService;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

@Singleton
@Slf4j
public class BookingController {

    // private final MongoDBRepository<Interviewer> interviewerRepository;
    private static final int MAX_AVAILABLE_SLOTS = 5;

    GenericRepository<Interviews> interviewRepository;
    GenericRepository<Interviewer> interviewerRepository;
    @Inject OpenTelemetry openTelemetry;

    @Inject
    public BookingController(
            GenericRepository<Interviewer> interviewerRepository,
            GenericRepository<Interviews> interviewRepository) {
        this.interviewerRepository = interviewerRepository;
        this.interviewRepository = interviewRepository;
        openTelemetry = new FakeOpenTelemetryService();
    }

    /**
     * Find available time slots for interviews
     *
     * @param startTime, user input start time passed in as UTC
     * @param endTime, user input end time passed in as UTC
     * @param experienceLevel, user input experience level
     * @return, list of available interviewers with available time slots
     */
    public List<AvailableInterviewer> findAvailableTimeSlots(
            LocalDateTime startTime, LocalDateTime endTime, String experienceLevel) {
        Collection<Interviewer> interviewers = interviewerRepository.getAll();
        List<AvailableInterviewer> available = new ArrayList<>();
        for (Interviewer interviewer : interviewers) {
            // filter interviewer by experience level
            if (!"no_preference".equals(experienceLevel)
                    && !experienceLevel.equals(interviewer.getExperienceLevel())) {
                continue;
            }
            // filter interviewer by time slot
            List<TimeSlot> matchingSlots =
                    interviewer.getTimeSlots().stream()
                            .filter(
                                    slot ->
                                            !slot.isBooked()
                                                    && ((slot.getStartTime().isAfter(startTime)
                                                                    || slot.getStartTime()
                                                                            .isEqual(startTime))
                                                            && (slot.getEndTime().isBefore(endTime)
                                                                    || slot.getEndTime()
                                                                            .isEqual(endTime))))
                            .collect(Collectors.toList());
            // add interviewer to available list if there are available time slots
            if (!matchingSlots.isEmpty()) {
                available.add(new AvailableInterviewer(interviewer, matchingSlots));
            }
        }
        return limitTimeSlots(available, MAX_AVAILABLE_SLOTS);
    }

    /**
     * Helpler method to return top 5 available interviews time slot
     *
     * @param availableInterviewers, list of available interviewers with available time slots
     * @param maxSlots, maximum number of available time slots
     * @return, list of available interviewers with limited time slots
     */
    public List<AvailableInterviewer> limitTimeSlots(
            List<AvailableInterviewer> availableInterviewers, int maxSlots) {
        List<AvailableInterviewer> limitedAvailableInterviewers = new ArrayList<>();
        int slotsCount = 0;
        for (AvailableInterviewer availableInterviewer : availableInterviewers) {
            List<TimeSlot> limitedSlots = new ArrayList<>();
            for (TimeSlot slot : availableInterviewer.getAvailableTimeSlots()) {
                if (slotsCount < MAX_AVAILABLE_SLOTS) {
                    limitedSlots.add(slot);
                    slotsCount++;
                } else {
                    break;
                }
            }
            if (!limitedSlots.isEmpty()) {
                limitedAvailableInterviewers.add(
                        new AvailableInterviewer(
                                availableInterviewer.getInterviewer(), limitedSlots));
            }
            if (slotsCount >= MAX_AVAILABLE_SLOTS) {
                break;
            }
        }
        return limitedAvailableInterviewers;
    }

    /**
     * Determine if the selected time slot is available, if so, book it. with the selectedStartTime
     * passed in as UTC
     *
     * @param interviewerId, interviewer id
     * @param selectedStartTime, selected start time in UTC
     * @return, true if the booking is successful, false otherwise
     */
    public boolean canBook(String interviewerId, LocalDateTime selectedStartTime) {
        ObjectId id = new ObjectId(interviewerId);
        Interviewer interviewer = interviewerRepository.get(id);

        if (interviewer == null) {
            return false;
        }
        boolean slotFoundAndBooked = false;
        // mark the selected time slot as booked
        for (TimeSlot timeSlot : interviewer.getTimeSlots()) {
            if (timeSlot.getStartTime().equals(selectedStartTime) && !timeSlot.isBooked()) {
                timeSlot.setBooked(true);
                slotFoundAndBooked = true;
                break;
            }
        }
        if (!slotFoundAndBooked) {
            return false; // Suitable time slot not found or it's already booked
        }
        // Update the interviewer in the database
        interviewerRepository.update(interviewer);
        return true;
    }

    /**
     * Cancel the booking for the selected time slot
     *
     * @param interviewerId, interviewer id
     * @param startTime, selected start time in UTC
     */
    public void cancelBooking(String interviewerId, LocalDateTime startTime) {
        ObjectId id = new ObjectId(interviewerId);
        Interviewer interviewer = interviewerRepository.get(id);

        if (interviewer == null) {
            return;
        }
        boolean slotUpdated = false;
        for (TimeSlot timeSlot : interviewer.getTimeSlots()) {
            if (timeSlot.getStartTime().equals(startTime) && timeSlot.isBooked()) {
                timeSlot.setBooked(false);
                slotUpdated = true;
                break;
            }
        }
        // Update the interviewer in the database
        if (slotUpdated) interviewerRepository.update(interviewer);
    }
}
