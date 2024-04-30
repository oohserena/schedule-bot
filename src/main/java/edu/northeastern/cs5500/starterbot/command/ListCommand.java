package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.InterviewController;
import edu.northeastern.cs5500.starterbot.controller.TimeZoneController;
import edu.northeastern.cs5500.starterbot.controller.UsersController;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.model.Users;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class ListCommand implements SlashCommandHandler, ButtonHandler {

    static final String NAME = "list";
    @Inject InterviewController interviewController;
    @Inject UsersController usersController;
    @Inject TimeZoneController timeZoneController;

    @Inject
    public ListCommand() {
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
        return Commands.slash(getName(), "List all scheduled interviews");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /list");
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder =
                messageCreateBuilder.addActionRow(
                        Button.primary(
                                getName() + ":booked", "Booked"), // booked means upcoming event
                        Button.primary(getName() + ":completed", "Completed"),
                        Button.primary(getName() + ":canceled", "Canceled"));
        messageCreateBuilder =
                messageCreateBuilder.setContent(
                        "Please choose a category to list: Booked, Completed or Canceled event.");
        event.reply(messageCreateBuilder.build()).queue();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String userId = event.getUser().getId();
        Users user = usersController.findUserByDiscordId(userId);
        if (user == null) {
            event.reply("You have no interviews scheduled.").setEphemeral(true).queue();
            return;
        }
        List<Interviews> userInterviews =
                interviewController.findInterviewsByIds(user.getInterviewIds());
        String label = event.getButton().getLabel();
        // auto update completed interviews based on current time
        interviewController.markCompletedInterviewsAutomatically();
        List<Interviews> filteredInterviews =
                userInterviews.stream()
                        .filter(interview -> label.equals(interview.getStatus()))
                        .collect(Collectors.toList());

        String response = "";
        if (filteredInterviews.isEmpty()) {
            response = "There are no " + label.toLowerCase() + " interviews.";
        } else {
            response =
                    filteredInterviews.stream()
                            .map(
                                    interview -> {
                                        LocalDateTime startPST =
                                                timeZoneController.toUserLocalTime(
                                                        interview.getStart());
                                        LocalDateTime endPST =
                                                timeZoneController.toUserLocalTime(
                                                        interview.getEnd());
                                        DateTimeFormatter formatter =
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                        String formattedStart = startPST.format(formatter);
                                        String formattedEnd = endPST.format(formatter);
                                        String interviewerName =
                                                interviewController.getInterviewerNameById(
                                                        interview.getInterviewerId());
                                        if (startPST == null
                                                || endPST == null
                                                || interviewerName == null) {
                                            throw new IllegalStateException(
                                                    "One of the required fields is null");
                                        }
                                        return String.format(
                                                "%s (Time: %s to %s)",
                                                interviewerName, formattedStart, formattedEnd);
                                    })
                            .collect(Collectors.joining("\n"));
        }
        event.reply(response).setEphemeral(true).queue();
    }
}
