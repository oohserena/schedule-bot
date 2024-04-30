package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.InterviewController;
import edu.northeastern.cs5500.starterbot.controller.NotificationController;
import edu.northeastern.cs5500.starterbot.controller.TimeZoneController;
import edu.northeastern.cs5500.starterbot.controller.UsersController;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.model.Users;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class SetNotificationCommand implements SlashCommandHandler, ButtonHandler {

    static final String NAME = "setnotification";

    @Inject InterviewController interviewController;
    @Inject UsersController usersController;
    @Inject NotificationController notificationController;
    @Inject TimeZoneController timeZoneController;

    @Inject
    public SetNotificationCommand() {
        // Empty and public for Dagger
    }

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    @Nonnull
    public CommandData getCommandData() {
        return Commands.slash(
                getName(), "Set a reminder notification for 15 minutes before your interviews.");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /setNotifications");

        String userId = event.getUser().getId();

        Users user = usersController.findUserByDiscordId(userId);
        if (user == null) {
            event.deferReply(true).setContent("You have no interviews scheduled.").queue();
            return;
        }

        List<Interviews> userInterviews =
                interviewController.findInterviewsByIds(user.getInterviewIds()).stream()
                        .filter(
                                interview ->
                                        "Not set yet".equals(interview.getNotificationStatus())
                                                && "Booked".equals(interview.getStatus()))
                        .collect(Collectors.toList());

        if (userInterviews.isEmpty()) {
            event.deferReply(true)
                    .setContent("You have no interviews with notifications 'Not set yet'.")
                    .queue();
            return;
        }

        event.deferReply().queue();

        new Thread(
                        () -> {
                            for (Interviews interview : userInterviews) {
                                MessageCreateBuilder messageCreateBuilder =
                                        new MessageCreateBuilder();
                                String formattedInterview =
                                        interviewController.formatInterviewForDisplay(interview);

                                // Create an embed for the interview details
                                MessageEmbed interviewEmbed =
                                        new EmbedBuilder()
                                                .setDescription(formattedInterview)
                                                .build();

                                Button notifyButton =
                                        Button.primary(
                                                getName()
                                                        + ":set-notifications:"
                                                        + interview.getId(),
                                                "Set Notification");

                                messageCreateBuilder
                                        .addEmbeds(interviewEmbed)
                                        .addActionRow(notifyButton);

                                event.getHook().sendMessage(messageCreateBuilder.build()).queue();
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        })
                .start();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String customId = event.getButton().getId();
        String[] parts = customId.split(":");
        String interviewId = parts.length > 2 ? parts[2] : "";
        log.info("interview id: {}", interviewId);

        Users user = usersController.findUserByDiscordId(event.getUser().getId());
        if (user == null) {
            event.reply("You have no interviews scheduled.").setEphemeral(true).queue();
            return;
        }

        String notificationStatus = notificationController.getNotificationStatusById(interviewId);
        log.info("notification status: {}", notificationStatus);

        if ("Not set yet".equals(notificationStatus)) {
            // convert the interview start time to PST to compare with the current time
            LocalDateTime startTime =
                    timeZoneController.toUserLocalTime(
                            interviewController.getInterviewDateTimeById(interviewId));
            LocalDateTime notificationTime = startTime.minusMinutes(15);

            // Get the current time
            LocalDateTime currentTime = LocalDateTime.now();

            // Check if it's time to send the notification
            if (currentTime.isBefore(notificationTime)) {
                // Calculate the formatted notification time
                String formattedNotificationTime =
                        startTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
                String notificationMessage =
                        "Notification: Your interview will start at " + formattedNotificationTime;

                // Calculate delay until the notification time
                long delayInSeconds = Duration.between(currentTime, notificationTime).getSeconds();

                // Schedule the notification to be sent at the correct time
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.schedule(
                        () -> sendNotification(event.getUser(), notificationMessage),
                        delayInSeconds,
                        TimeUnit.SECONDS);

                boolean updated =
                        notificationController.updateNotificationStatus(interviewId, "Already set");

                if (updated) {
                    // Respond to the interaction
                    event.reply(
                                    "Your interview reminder is set. I'll notify you 15 minutes before it starts")
                            .setEphemeral(true)
                            .queue();
                } else {
                    // If it's already past the notification time, inform the user
                    event.reply("It's too late to set a reminder notification for this interview.")
                            .setEphemeral(true)
                            .queue();
                }
            }
        }
    }

    private void sendNotification(User user, String message) {
        user.openPrivateChannel()
                .queue(
                        privateChannel -> {
                            privateChannel.sendMessage(message).queue();
                        });
    }
}
