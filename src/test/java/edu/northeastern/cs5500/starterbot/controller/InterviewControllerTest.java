package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Interviewer;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class InterviewControllerTest {

    private InterviewController interviewController;
    private InMemoryRepository<Interviews> interviewRepository;
    private InMemoryRepository<Interviewer> interviewerRepository;
    private TimeZoneController timeZoneController = new TimeZoneController();

    @BeforeEach
    public void setup() {
        interviewRepository = new InMemoryRepository<>();
        interviewerRepository = new InMemoryRepository<>();
        interviewController =
                new InterviewController(
                        interviewRepository, interviewerRepository, timeZoneController);
    }

    @Test
    public void testUpdateInterviewNotes() {
        Interviews interview = new Interviews();
        interview.setId(new ObjectId());
        interview.setNotes("test notes");
        interviewRepository.add(interview);
        String newNotes = "Updated notes";
        String title = "Updated title";
        interviewController.updateInterviewNotes(interview.getId().toString(), title, newNotes);
        Interviews updatedInterview = interviewRepository.get(interview.getId());
        assertThat(updatedInterview.getNotes()).isEqualTo(newNotes);
    }

    @Test
    public void testAddInterviewWhenNoneExists() {
        String interviewerId = "testInterviewerId01";
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        String interviewId =
                interviewController.addOrUpdateInterview(interviewerId, startTime, endTime);
        assertThat(interviewId).isNotNull();
        Interviews interview = interviewRepository.get(new ObjectId(interviewId));
        assertThat(interview).isNotNull();
        assertThat(interview.getInterviewerId()).isEqualTo(interviewerId);
        assertThat(interview.getStart()).isEqualTo(startTime);
        assertThat(interview.getEnd()).isEqualTo(endTime);
        assertThat(interview.getStatus()).isEqualTo("Booked");
    }

    @Test
    public void testUpdateInterviewWhenExists() {
        String interviewerId = "testInterviewerId02";
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        Interviews existingInterview = createInterview("Booked", startTime);
        existingInterview.setInterviewerId(interviewerId);

        String updatedInterviewId =
                interviewController.addOrUpdateInterview(interviewerId, startTime, endTime);
        Interviews updatedInterview = interviewRepository.get(new ObjectId(updatedInterviewId));
        assertThat(updatedInterview).isNotNull();
        assertThat(updatedInterview.getInterviewerId()).isEqualTo(interviewerId);
        assertThat(updatedInterview.getStart()).isEqualTo(startTime);
        assertThat(updatedInterview.getEnd()).isEqualTo(endTime);
        assertThat(updatedInterview.getStatus()).isEqualTo("Booked");
    }

    @Test
    public void testUpdateInterviewStatus() {
        assertThat(interviewController.updateInterviewStatus(new ObjectId().toString(), "Canceled"))
                .isFalse();

        String interviewerId = "testInterviewerId03";
        LocalDateTime startTime = LocalDateTime.now();
        Interviews interview = createInterview("Booked", startTime);
        interview.setInterviewerId(interviewerId);

        assertThat(
                        interviewController.updateInterviewStatus(
                                interview.getId().toString(), "Canceled"))
                .isTrue();
        assertThat(interview.getStatus()).isEqualTo("Canceled");
    }

    @Test
    public void testMarkCompletedInterviewsAutomatically() {
        Interviews pastBookedInterview =
                createInterview("Booked", LocalDateTime.now().minusDays(1));
        Interviews futureBookedInterview =
                createInterview("Booked", LocalDateTime.now().plusDays(1));
        Interviews pastCompletedInterview =
                createInterview("Completed", LocalDateTime.now().minusDays(1));

        interviewController.markCompletedInterviewsAutomatically();
        List<Interviews> updatedInterviews =
                interviewRepository.getAll().stream()
                        .filter(interview -> "Completed".equals(interview.getStatus()))
                        .collect(Collectors.toList());

        assertThat(pastBookedInterview.getStatus()).isEqualTo("Completed");
        assertThat(updatedInterviews.size()).isEqualTo(2);
        assertThat(updatedInterviews).containsExactly(pastBookedInterview, pastCompletedInterview);
        assertThat(updatedInterviews).doesNotContain(futureBookedInterview);
    }

    @Test
    public void testFindInterviewByInterviewerId() {
        LocalDateTime startTime = LocalDateTime.now();
        Interviews interview1 = createInterview("Completed", startTime);
        Interviews interview2 = createInterview("Booked", startTime.plusDays(1));
        Interviews interview3 = createInterview("Canceled", startTime.plusDays(2));

        List<String> testIds =
                Arrays.asList(interview1.getId().toString(), interview2.getId().toString());
        List<Interviews> foundInterviews = interviewController.findInterviewsByIds(testIds);
        assertThat(foundInterviews.size()).isEqualTo(2);
        assertThat(foundInterviews).containsExactly(interview1, interview2);
    }

    @Test
    public void testGetInterviewerNameById() {
        Interviewer interviewer = new Interviewer();
        interviewer.setName("John Doe");
        interviewer.setId(new ObjectId());
        interviewerRepository.add(interviewer);
        String name = interviewController.getInterviewerNameById(interviewer.getId().toString());
        assertThat(name).isEqualTo("John Doe");

        String unknownName = interviewController.getInterviewerNameById(new ObjectId().toString());
        assertThat(unknownName).isEqualTo("Unknown Interviewer");
    }

    @Test
    public void testGetInterviewDateTimeById() {
        LocalDateTime startTime = LocalDateTime.of(2024, 4, 1, 10, 0);
        Interviews interview = createInterview("Booked", startTime);
        String interviewId = interview.getId().toString();
        LocalDateTime actualStartTime = interviewController.getInterviewDateTimeById(interviewId);
        assertThat(actualStartTime).isEqualTo(startTime);
    }

    @Test
    public void testGetInterviewStatusByInterviewId() {
        Interviews interview = createInterview("Booked", LocalDateTime.now());
        String interviewId = interview.getId().toString();
        String status = interviewController.getInterviewStatusByInterviewId(interviewId);
        assertThat(status).isEqualTo("Booked");

        String nonExistingInterviewId = new ObjectId().toString();
        status = interviewController.getInterviewStatusByInterviewId(nonExistingInterviewId);
        assertThat(status).isEqualTo("Interview not found in database.");
    }

    @Test
    public void testGetInterviewById() {
        Interviews interview = createInterview("Booked", LocalDateTime.now());
        String interviewId = interview.getId().toString();
        Interviews foundInterview = interviewController.getInterviewById(interviewId);
        assertThat(foundInterview).isEqualTo(interview);

        Interviews nonExistingInterview =
                interviewController.getInterviewById(new ObjectId().toString());
        assertThat(nonExistingInterview).isNull();
    }

    @Test
    public void testFormatInterviewForDisplay() {
        Interviews interview = createInterview("Booked", LocalDateTime.now());
        interview.setEnd(LocalDateTime.now().plusHours(2));
        interview.setInterviewerId(new ObjectId().toString());
        TimeZoneController timeZoneController = new TimeZoneController();
        String start =
                timeZoneController
                        .toUserLocalTime(interview.getStart())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String end =
                timeZoneController
                        .toUserLocalTime(interview.getEnd())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        String name = interviewController.getInterviewerNameById(interview.getInterviewerId());
        String expected = name + " (Time: " + start + " to " + end + ")";
        String actual = interviewController.formatInterviewForDisplay(interview);

        assertThat(actual).isEqualTo(expected);
    }

    /** Helper method to create an interview */
    private Interviews createInterview(String status, LocalDateTime startTime) {
        Interviews interview = new Interviews();
        interview.setId(new ObjectId());
        interview.setStatus(status);
        interview.setStart(startTime);
        interview.setEnd(startTime.plusHours(1));
        interviewRepository.add(interview);
        return interview;
    }
}
