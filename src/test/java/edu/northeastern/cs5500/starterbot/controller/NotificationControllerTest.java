package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class NotificationControllerTest {

    private NotificationController notificationController;
    private InMemoryRepository<Interviews> interviewRepository;

    @BeforeEach
    public void setup() {
        interviewRepository = new InMemoryRepository<>();
        notificationController = new NotificationController(interviewRepository);
    }

    @Test
    public void testUpdateNotificationStatus() {
        Interviews interview = new Interviews();
        interview.setId(new ObjectId());
        interview.setNotificationStatus("Not set yet");
        interviewRepository.add(interview);
        String newNotificationStatus = "Already set";
        notificationController.updateNotificationStatus(
                interview.getId().toString(), newNotificationStatus);
        Interviews updatedInterview = interviewRepository.get(interview.getId());

        assertThat(updatedInterview).isNotNull();
        assertThat(updatedInterview.getNotificationStatus()).isEqualTo(newNotificationStatus);
    }

    @Test
    public void testUpdateNotificationStatus_InterviewNotFound() {
        String fakeId = new ObjectId().toString();
        assertThat(notificationController.updateNotificationStatus(fakeId, "Already set"))
                .isFalse();
    }

    private Interviews createInterview(String status, LocalDateTime starTime) {
        Interviews interview = new Interviews();
        interview.setId(new ObjectId());
        interview.setStatus(status);
        interview.setStart(starTime);
        interview.setEnd(starTime.plusHours(1));
        interviewRepository.add(interview);
        return interview;
    }

    @Test
    public void testGetNotificationStatusById() {
        Interviews interview = createInterview("Booked", LocalDateTime.now());
        interview.setNotificationStatus("Not set yet");
        String interviewId = interview.getId().toString();
        String expected = "Not set yet";
        String actual = notificationController.getNotificationStatusById(interviewId);
        assertThat(actual).isEqualTo(expected);

        String nonExistingInterviewId = new ObjectId().toString();
        String statusForNonExistingId =
                notificationController.getNotificationStatusById(nonExistingInterviewId);
        String expectedNotFound = "Interview not found in database";
        assertThat(statusForNonExistingId).isEqualTo(expectedNotFound);
    }
}
