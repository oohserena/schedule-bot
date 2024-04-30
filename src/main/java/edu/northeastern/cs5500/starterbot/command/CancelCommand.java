package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.BookingController;
import edu.northeastern.cs5500.starterbot.controller.InterviewController;
import edu.northeastern.cs5500.starterbot.controller.TimeZoneController;
import edu.northeastern.cs5500.starterbot.controller.UsersController;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.model.Users;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class CancelCommand implements SlashCommandHandler, ButtonHandler {

    static final String NAME = "cancel";
    @Inject InterviewController interviewController;
    @Inject UsersController usersController;
    @Inject BookingController bookingController;
    @Inject TimeZoneController timeZoneController;

    @Inject
    public CancelCommand() {
        // Empty and public for Dagger
    }

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Nonnull
    @Override
    public CommandData getCommandData() {
        // List all booking and choose to cancel
        return Commands.slash(getName(), "List and cancel upcoming interviews");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /cancel");
        String userId = event.getUser().getId();
        Users user = usersController.findUserByDiscordId(userId);
        if (user == null) {
            event.reply("You have no interviews scheduled.").setEphemeral(true).queue();
            return;
        }
        List<Interviews> userInterviews =
                interviewController.findInterviewsByIds(user.getInterviewIds());
        List<Interviews> filteredInterviews =
                userInterviews.stream()
                        .filter(interview -> "Booked".equals(interview.getStatus()))
                        .collect(Collectors.toList());

        if (filteredInterviews.isEmpty()) {
            event.reply("You have no booked interviews to cancel.").setEphemeral(true).queue();
        } else {
            MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
            List<ActionRow> actionRows = new ArrayList<>();
            for (Interviews interview : filteredInterviews) {
                String interviewId = interview.getId().toString();
                String interviewerId = interview.getInterviewerId();
                // convert interview start time to PST for user display
                LocalDateTime startTimePST =
                        timeZoneController.toUserLocalTime(interview.getStart());
                String label =
                        startTimePST.format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"))
                                + " with "
                                + interviewController.getInterviewerNameById(
                                        interview.getInterviewerId());
                String buttonId = "cancel:" + interviewId + ":" + interviewerId;
                Button button = Button.danger(buttonId, label);
                actionRows.add(ActionRow.of(button));
            }
            messageCreateBuilder.setContent("Select an interview to cancel:");
            messageCreateBuilder.setComponents(actionRows);
            event.reply(messageCreateBuilder.build()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId(); // "cancel:interviewId:interviewerId"
        String[] parts = buttonId.split(":");
        if (parts.length < 3) {
            event.reply("Invalid button interaction.").setEphemeral(true).queue();
            return;
        }
        String interviewId = parts[1];
        String interviewerId = parts[2];

        // update interview status in interviews collection to "Canceled"
        boolean updateSuccess = interviewController.updateInterviewStatus(interviewId, "Canceled");
        if (!updateSuccess) {
            event.reply("Failed to cancel the interview. Please try again later.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        String interviewerName = interviewController.getInterviewerNameById(interviewerId);
        log.info("interviewerName: {}", interviewerName);
        // convert interview start time to PST for user display
        LocalDateTime startTimeUTC = interviewController.getInterviewDateTimeById(interviewId);
        LocalDateTime startTimePST = timeZoneController.toUserLocalTime(startTimeUTC);
        String startTime = startTimePST.format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
        String confirmationMessage =
                "Your interview scheduled with "
                        + interviewerName
                        + " scheduled for  "
                        + startTime
                        + " has been successfully canceled.";
        // update interviewer time slot isBooked to false
        bookingController.cancelBooking(interviewerId, startTimeUTC);
        event.reply(confirmationMessage).setEphemeral(true).queue();
    }
}
