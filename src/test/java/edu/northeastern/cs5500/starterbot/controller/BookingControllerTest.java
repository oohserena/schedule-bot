package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.AvailableInterviewer;
import edu.northeastern.cs5500.starterbot.model.Interviewer;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.model.TimeSlot;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class BookingControllerTest {
    private BookingController bookingController;
    private InMemoryRepository<Interviewer> interviewerRepository;
    private InMemoryRepository<Interviews> interviewRepository;

    @BeforeEach
    public void setup() {
        interviewerRepository = new InMemoryRepository<>();
        interviewRepository = new InMemoryRepository<>();
        bookingController = new BookingController(interviewerRepository, interviewRepository);
    }

    /** helper method to create an interviewer */
    private Interviewer createInterviewer(String experienceLevel, List<TimeSlot> timeSlots) {
        Interviewer interviewer = new Interviewer();
        interviewer.setId(new ObjectId());
        interviewer.setExperienceLevel(experienceLevel);
        interviewer.setTimeSlots(timeSlots);
        interviewerRepository.add(interviewer);
        return interviewer;
    }

    @Test
    public void testFindAvailableTimeSlots() {
        List<TimeSlot> timeSlots1 = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.of(2024, 04, 01, 10, 00);
        LocalDateTime endTime = LocalDateTime.of(2024, 04, 01, 11, 00);
        timeSlots1.add(new TimeSlot(startTime, endTime, false));
        timeSlots1.add(new TimeSlot(startTime.plusDays(1), endTime.plusDays(1), true));
        Interviewer interviewer = createInterviewer("peer", timeSlots1);
        // test for peer w/ available time slots
        List<AvailableInterviewer> availableInterviewers1 =
                bookingController.findAvailableTimeSlots(startTime, endTime, "peer");
        assertThat(interviewerRepository.getAll().size()).isEqualTo(1);
        assertThat(availableInterviewers1.size()).isEqualTo(1);
        assertThat(availableInterviewers1.get(0).getAvailableTimeSlots().size()).isEqualTo(1);
        assertThat(availableInterviewers1.get(0).getInterviewer().getId())
                .isEqualTo(interviewer.getId());
        assertThat(availableInterviewers1.get(0).getAvailableTimeSlots().get(0).getStartTime())
                .isEqualTo(startTime);
        assertThat(availableInterviewers1.get(0).getAvailableTimeSlots().get(0).getEndTime())
                .isEqualTo(endTime);
        assertThat(availableInterviewers1.get(0).getInterviewer().getExperienceLevel())
                .isEqualTo("peer");

        // test for professional w/ no available time slots
        List<AvailableInterviewer> availableInterviewers2 =
                bookingController.findAvailableTimeSlots(startTime, endTime, "professional");
        assertThat(availableInterviewers2.size()).isEqualTo(0);
        // test for no preference w/ available time slots
        List<AvailableInterviewer> availableInterviewers3 =
                bookingController.findAvailableTimeSlots(startTime, endTime, "no_preference");
        assertThat(availableInterviewers3.size()).isEqualTo(1);
        // test for no preference w/ end time on the next day
        List<AvailableInterviewer> availableInterviewers4 =
                bookingController.findAvailableTimeSlots(
                        startTime, endTime.plusDays(1), "no_preference");
        assertThat(availableInterviewers4.size()).isEqualTo(1);
        // test for no preference w/ start time on the next day
        List<AvailableInterviewer> availableInterviewers5 =
                bookingController.findAvailableTimeSlots(
                        startTime.plusDays(1), endTime, "no_preference");
        assertThat(availableInterviewers5.size()).isEqualTo(0);
    }

    @Test
    public void testlimitTimeSlots() {
        List<AvailableInterviewer> availableInterviewers = new ArrayList<>();
        Interviewer interviewer1 = createInterviewer("peer", new ArrayList<>());
        Interviewer interviewer2 = createInterviewer("professional", new ArrayList<>());
        List<TimeSlot> timeSlots1 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            LocalDateTime start = LocalDateTime.now().plusDays(i);
            LocalDateTime end = start.plusHours(1);
            timeSlots1.add(new TimeSlot(start, end, false));
        }
        availableInterviewers.add(new AvailableInterviewer(interviewer1, timeSlots1));
        List<TimeSlot> timeSlots2 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            LocalDateTime start = LocalDateTime.now().plusDays(i + 3);
            LocalDateTime end = start.plusHours(1);
            timeSlots2.add(new TimeSlot(start, end, false));
        }
        availableInterviewers.add(new AvailableInterviewer(interviewer2, timeSlots2));

        List<AvailableInterviewer> limitedAvailableInterviewers =
                bookingController.limitTimeSlots(availableInterviewers, 5);
        assertThat(limitedAvailableInterviewers).isNotNull();
        assertThat(limitedAvailableInterviewers.size()).isEqualTo(2);
        // check if all time slots(3) for interviewer1 are included
        assertThat(limitedAvailableInterviewers.get(0).getAvailableTimeSlots().size()).isEqualTo(3);
        // check if only 2 time slots for interviewer2 are included
        assertThat(limitedAvailableInterviewers.get(1).getAvailableTimeSlots().size()).isEqualTo(2);
    }

    @Test
    public void testCanbook() {
        // test if no interviewer found
        assertThat(bookingController.canBook(new ObjectId().toString(), LocalDateTime.now()))
                .isFalse();
        List<TimeSlot> timeSlots1 = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.of(2024, 04, 01, 10, 00);
        LocalDateTime endTime = LocalDateTime.of(2024, 04, 01, 11, 00);
        timeSlots1.add(new TimeSlot(startTime, endTime, false));
        Interviewer interviewer = createInterviewer("peer", timeSlots1);
        ObjectId interviewerId = interviewer.getId();
        boolean successful = bookingController.canBook(interviewerId.toString(), startTime);
        boolean Failed = bookingController.canBook(interviewerId.toString(), startTime.plusDays(1));
        assertThat(successful).isTrue();
        assertThat(Failed).isFalse();
        Interviewer updatedInterviewer = interviewerRepository.get(interviewerId);
        assertThat(updatedInterviewer.getTimeSlots().get(0).isBooked()).isTrue();
    }

    @Test
    public void testCancelBooking() {
        LocalDateTime startTime = LocalDateTime.of(2024, 04, 01, 10, 00);
        LocalDateTime endTime = LocalDateTime.of(2024, 04, 01, 11, 00);
        List<TimeSlot> timeSlots1 = new ArrayList<>();
        timeSlots1.add(new TimeSlot(startTime, endTime, true));
        timeSlots1.add(new TimeSlot(startTime.plusDays(1), endTime.plusDays(1), true));
        Interviewer interviewer = createInterviewer("peer", timeSlots1);
        ObjectId interviewerId = interviewer.getId();
        bookingController.cancelBooking(interviewerId.toString(), startTime);
        Interviewer updatedInterviewer = interviewerRepository.get(interviewerId);

        assertThat(updatedInterviewer).isNotNull();
        assertThat(updatedInterviewer.getTimeSlots().size()).isEqualTo(2);
        assertThat(updatedInterviewer.getTimeSlots().get(0).isBooked()).isFalse();

        String nonExistingInterviewerId = new ObjectId().toString();
        assertThat(bookingController.canBook(nonExistingInterviewerId, startTime)).isFalse();
    }
}
