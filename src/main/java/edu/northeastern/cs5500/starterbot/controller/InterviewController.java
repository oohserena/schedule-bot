package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.Interviewer;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.FakeOpenTelemetryService;
import edu.northeastern.cs5500.starterbot.service.MongoDBService;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.jetty.util.log.Log;

@Singleton
@Slf4j
public class InterviewController {

    GenericRepository<Interviews> interviewRepository;
    GenericRepository<Interviewer> interviewerRepository;
    TimeZoneController timeZoneController;
    @Inject OpenTelemetry openTelemetry;

    @Inject
    public InterviewController(
            GenericRepository<Interviews> interviewRepository,
            GenericRepository<Interviewer> interviewerRepository,
            TimeZoneController timeZoneController) {
        this.interviewRepository = interviewRepository;
        this.interviewerRepository = interviewerRepository;
        this.timeZoneController = timeZoneController;
        openTelemetry = new FakeOpenTelemetryService();
    }

    public InterviewController(MongoDBService mongoDBService) {
        // TODO Auto-generated constructor stub
    }

    /**
     * Add new booked interview into interviews collection, if already exists, update said
     * interview, status to "Booked".
     *
     * @param interviewerId, interviewer ID
     * @param startTime, start time passed in as UTC
     * @param endTime, end time passed in as UTC
     * @return, interview ID
     */
    public String addOrUpdateInterview(
            String interviewerId, LocalDateTime startTime, LocalDateTime endTime) {
        // check if the interview already exists
        java.util.Optional<Interviews> exisInterview =
                interviewRepository.getAll().stream()
                        .filter(
                                interview ->
                                        interview.getInterviewerId().equals(interviewerId)
                                                && interview.getStart().equals(startTime)
                                                && interview.getEnd().equals(endTime))
                        .findFirst();

        Interviews interview;
        if (exisInterview.isPresent()) {
            interview = exisInterview.get();
        } else {
            interview = new Interviews();
            interview.setInterviewerId(interviewerId);
            interview.setStart(startTime);
            interview.setEnd(endTime);
        }
        interview.setStatus("Booked");
        interview.setNotificationStatus("Not set yet");

        // add or update the interview to the database
        try {
            if (interview.getId() != null) {
                interviewRepository.update(interview);
            } else {
                interview.setId(new ObjectId());
                interviewRepository.add(interview);
            }
        } catch (Exception e) {
            Log.getRootLogger().warn("Failed to add or update interview to database");
            throw new RuntimeException("Failed to add or update interview", e);
        }
        return interview.getId().toString();
    }

    /**
     * Update interview status in interviews collection
     *
     * @param interviewId, interview ID
     * @param status, new status
     * @return, true if update is successful, false otherwise
     */
    public boolean updateInterviewStatus(String interviewId, String status) {
        try {
            Interviews interview = interviewRepository.get(new ObjectId(interviewId));
            if (interview == null) {
                Log.getRootLogger().warn("Interview not found in database");
                return false;
            }
            interview.setStatus(status);
            interviewRepository.update(interview);
            Log.getRootLogger()
                    .info("Interview status updated to " + status + " for ID: " + interviewId);
            return true;
        } catch (Exception e) {
            Log.getRootLogger().warn("Failed to update interview status in database");
            throw new RuntimeException("Failed to update interview status", e);
        }
    }

    /**
     * Update completed interview in interviews collection Convert interview start time to PST to
     * compare with current time
     */
    public void markCompletedInterviewsAutomatically() {
        LocalDateTime now = LocalDateTime.now();
        List<Interviews> bookedInterviews =
                interviewRepository.getAll().stream()
                        .filter(interview -> "Booked".equals(interview.getStatus()))
                        .collect(Collectors.toList());

        for (Interviews interview : bookedInterviews) {
            // convert interview start time to PST to compare with current time
            LocalDateTime startTimePST = timeZoneController.toUserLocalTime(interview.getStart());
            if (startTimePST.isBefore(now)) {
                interview.setStatus("Completed");
                interviewRepository.update(interview);
            }
        }
        log.info("Completed interviews auto updated at: {}", now);
    }

    /**
     * Get all interviews the current user has
     *
     * @param interviewIdList, List of interviewIds the user has
     * @return, List of interviews
     */
    public List<Interviews> findInterviewsByIds(List<String> interviewIdList) {
        return interviewRepository.getAll().stream()
                .filter(interview -> interviewIdList.contains(interview.getId().toString()))
                .collect(Collectors.toList());
    }

    /**
     * Get interviwer name by interviewer ID
     *
     * @param interviewerId
     * @return, interviewer name
     */
    public String getInterviewerNameById(String interviewerId) {
        ObjectId interviewerIdObj = new ObjectId(interviewerId);
        Interviewer interviewer = interviewerRepository.get(interviewerIdObj);
        return interviewer != null ? interviewer.getName() : "Unknown Interviewer";
    }

    public String getInterviewStartTimeByInterviewId(String interviewId) {
        Interviews interview = interviewRepository.get(new ObjectId(interviewId));
        return interview != null
                ? interview.getStart().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"))
                : "Unknown Start Time";
    }

    /**
     * Update interview notes
     *
     * @param interviewId, interview ID
     * @param title, new title
     * @param notes, new notes
     */
    public void updateInterviewNotes(String interviewId, String title, String notes) {
        Interviews interview = interviewRepository.get(new ObjectId(interviewId));
        log.info("Before updating interview notes: {}", interview.getNotes());
        interview.setTitle(title);
        interview.setNotes(notes);
        interviewRepository.update(interview);
        Interviews new_interview = interviewRepository.get(interview.getId());
        log.info("Updated interview notes: {}", new_interview.getNotes());
    }

    /**
     * Get interview status by interview ID
     *
     * @param interviewId, interview ID
     * @return, interview status
     */
    public String getInterviewStatusByInterviewId(String interviewId) {
        Interviews interview = interviewRepository.get(new ObjectId(interviewId));
        return interview != null ? interview.getStatus() : "Interview not found in database.";
    }

    /**
     * Get interview start time and end time by interview ID
     *
     * @param interviewId, interview ID
     * @return, interview start time and end time
     */
    public LocalDateTime getInterviewDateTimeById(String interviewId) {
        Interviews interview = interviewRepository.get(new ObjectId(interviewId));
        if (interview != null) {
            return interview.getStart();
        } else {
            return null;
        }
    }

    /**
     * Get interview by interview ID
     *
     * @param interviewId, interview ID
     * @return, interview
     */
    public Interviews getInterviewById(String interviewId) {
        Interviews interview = interviewRepository.get(new ObjectId(interviewId));
        return interview != null ? interview : null;
    }

    /**
     * Format the interview for display in the user's timezone
     *
     * @param interview The interview to format
     * @return The formatted interview string
     */
    public String formatInterviewForDisplay(Interviews interview) {
        // Convert the interview start and end time to PST for user display
        LocalDateTime startPST = timeZoneController.toUserLocalTime(interview.getStart());
        LocalDateTime endPST = timeZoneController.toUserLocalTime(interview.getEnd());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedStart = startPST.format(formatter);
        String formattedEnd = endPST.format(formatter);
        return String.format(
                "%s (Time: %s to %s)",
                getInterviewerNameById(interview.getInterviewerId()), formattedStart, formattedEnd);
    }
}
