package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.InterviewController;
import edu.northeastern.cs5500.starterbot.controller.NotificationController;
import edu.northeastern.cs5500.starterbot.controller.UsersController;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.model.Users;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class CancelNotificationCommand implements SlashCommandHandler, ButtonHandler {

    static final String NAME = "cancelnotification";

    @Inject InterviewController interviewController;
    @Inject UsersController usersController;
    @Inject NotificationController notificationController;

    @Inject
    public CancelNotificationCommand() {
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
        return Commands.slash(getName(), "Cancel notifications for your interviews");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /cancelNotifications");

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
                                        "Already set".equals(interview.getNotificationStatus()))
                        .collect(Collectors.toList());

        if (userInterviews.isEmpty()) {
            event.deferReply(true)
                    .setContent("You have no interviews with notifications 'Already set'.")
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
                                                        + ":cancel-notifications:"
                                                        + interview.getId(),
                                                "Cancel Notification");

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

        if ("Already set".equals(notificationStatus)) {
            boolean updated =
                    notificationController.updateNotificationStatus(interviewId, "Not set yet");

            if (updated) {
                // Respond to the interaction
                event.reply("Notification for the interview has been successfully cancelled.")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
