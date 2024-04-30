package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.FakeOpenTelemetryService;
import edu.northeastern.cs5500.starterbot.service.MongoDBService;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.jetty.util.log.Log;

@Singleton
@Slf4j
public class NotificationController {
    GenericRepository<Interviews> interviewRepository;
    @Inject OpenTelemetry openTelemetry;

    @Inject
    public NotificationController(GenericRepository<Interviews> interviewRepository) {
        this.interviewRepository = interviewRepository;
        openTelemetry = new FakeOpenTelemetryService();
    }

    public NotificationController(MongoDBService mongoDBService) {
        // TODO Auto-generated constructor stub
    }

    /**
     * Update interview's notification status
     *
     * @param interviewId interview id
     * @param notificationStatus notification Status, set as not set yet by defalut
     * @return boolean, show if the notification status is changed
     */
    public boolean updateNotificationStatus(String interviewId, String notificationStatus) {
        try {
            Interviews interview = interviewRepository.get(new ObjectId(interviewId));
            if (interview == null) {
                Log.getRootLogger().warn("Interview not found in database");
                return false;
            }
            interview.setNotificationStatus(notificationStatus);
            interviewRepository.update(interview);
            Log.getRootLogger()
                    .info(
                            "Notification status updated to "
                                    + notificationStatus
                                    + " for ID: "
                                    + interview.getInterviewerId());
            return true;
        } catch (Exception e) {
            Log.getRootLogger().warn("Failed to update interview status in database");
            throw new RuntimeException("Failed to update interview status", e);
        }
    }

    /**
     * get notification status by interview id
     *
     * @param interviewId interview id
     * @return notification status
     */
    public String getNotificationStatusById(String interviewId) {
        Interviews interview = interviewRepository.get(new ObjectId(interviewId));
        return interview != null
                ? interview.getNotificationStatus()
                : "Interview not found in database";
    }
}
