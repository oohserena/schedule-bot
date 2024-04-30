package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.BookingController;
import edu.northeastern.cs5500.starterbot.controller.InterviewController;
import edu.northeastern.cs5500.starterbot.controller.TimeZoneController;
import edu.northeastern.cs5500.starterbot.controller.UsersController;
import edu.northeastern.cs5500.starterbot.model.AvailableInterviewer;
import edu.northeastern.cs5500.starterbot.model.TimeSlot;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class BookCommand implements SlashCommandHandler, ButtonHandler {

    static final String NAME = "book";
    static final Map<String, List<AvailableInterviewer>> userSlots = new ConcurrentHashMap<>();

    @Inject BookingController bookingController;
    @Inject UsersController usersController;
    @Inject InterviewController interviewController;
    @Inject TimeZoneController timeZoneController;

    @Inject
    public BookCommand() {
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
        return Commands.slash(getName(), "Book a mock interview")
                .addOptions(
                        new OptionData(
                                        OptionType.STRING,
                                        "title",
                                        "Mock interview title you want to name")
                                .setRequired(true),
                        new OptionData(
                                        OptionType.STRING,
                                        "start_time",
                                        "Available time start from(format: YYYY-MM-DD HH:MM)")
                                .setRequired(true),
                        new OptionData(
                                        OptionType.STRING,
                                        "end_time",
                                        "Available time end at(format: YYYY-MM-DD HH:MM)")
                                .setRequired(true),
                        new OptionData(
                                        OptionType.STRING,
                                        "experience_level",
                                        "Choose the interviewer's experience level")
                                .addChoice("Peer", "peer")
                                .addChoice("Professional", "professional")
                                .addChoice("No preference", "no_preference")
                                .setRequired(true));
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /book");
        // Extract the initial options from the event
        OptionMapping titleOption = event.getOption("title");
        OptionMapping startTimeOption = event.getOption("start_time");
        OptionMapping endTimeOption = event.getOption("end_time");
        String experienceLevelOption = event.getOption("experience_level").getAsString();

        // check if null
        if (titleOption == null
                || startTimeOption == null
                || endTimeOption == null
                || experienceLevelOption == null) {
            event.reply("Invalid input").setEphemeral(true).queue();
            return;
        }

        String title = event.getOption("title").getAsString();
        String startTimeInput = event.getOption("start_time").getAsString();
        String endTimeInput = event.getOption("end_time").getAsString();
        String experienceLevel = event.getOption("experience_level").getAsString();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime startTime;
        LocalDateTime endTime;
        // parse and valid the input time
        try {
            startTime = LocalDateTime.parse(startTimeInput, formatter);
            endTime = LocalDateTime.parse(endTimeInput, formatter);
            if (!endTime.isAfter(startTime)) {
                event.reply("The end time must be after the start time").setEphemeral(true).queue();
            }
        } catch (DateTimeParseException e) {
            event.reply(
                            "The date and time you provided are in the incorrect format. Please use YYYY-MM-DD HH:MM format.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // find available time slots, time zone for availableInterviewers are UTC
        // convet user time (PST) to database time (UTC)
        startTime = timeZoneController.toDatabaseTime(startTime);
        endTime = timeZoneController.toDatabaseTime(endTime);
        List<AvailableInterviewer> availableInterviewers =
                bookingController.findAvailableTimeSlots(startTime, endTime, experienceLevel);
        log.info("slots: {}", availableInterviewers);

        // convert time in availableInterviewers back to user time PST
        List<AvailableInterviewer> availableInterviewersPST = new ArrayList<>();
        for (AvailableInterviewer availableInterviewer : availableInterviewers) {
            List<TimeSlot> timeSlotsPST = new ArrayList<>();
            for (TimeSlot timeSlot : availableInterviewer.getAvailableTimeSlots()) {
                LocalDateTime startTimePST =
                        timeZoneController.toUserLocalTime(timeSlot.getStartTime());
                LocalDateTime endTimePST =
                        timeZoneController.toUserLocalTime(timeSlot.getEndTime());
                timeSlotsPST.add(new TimeSlot(startTimePST, endTimePST, timeSlot.isBooked()));
            }
            availableInterviewersPST.add(
                    new AvailableInterviewer(availableInterviewer.getInterviewer(), timeSlotsPST));
        }
        log.info("slots: {}", availableInterviewersPST);
        String userId = event.getUser().getId();
        // userSlots.put(userId, availableInterviewers);
        userSlots.put(userId, availableInterviewersPST);
        List<ActionRow> actionRows = new ArrayList<>();
        // if slots is empty, then return no available time slots
        if (availableInterviewersPST.isEmpty()) {
            event.reply(
                            "No available time slots found for the given time range and experience level.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        // if slots is not empty, then create buttons
        for (int i = 0; i < availableInterviewersPST.size(); i++) {
            // the i-th interviewer
            AvailableInterviewer curAvailableInterviewer = availableInterviewersPST.get(i);
            // the j-th time slot
            for (int j = 0; j < curAvailableInterviewer.getAvailableTimeSlots().size(); j++) {
                String buttonId =
                        "book:"
                                + i
                                + ":"
                                + j
                                + ":"
                                + userId
                                + ":"
                                + curAvailableInterviewer
                                        .getAvailableTimeSlots()
                                        .get(j)
                                        .getStartTime()
                                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                String buttonLabel =
                        curAvailableInterviewer
                                        .getAvailableTimeSlots()
                                        .get(j)
                                        .getStartTime()
                                        .format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"))
                                + " with "
                                + curAvailableInterviewer.getInterviewer().getName()
                                + " ("
                                + curAvailableInterviewer.getInterviewer().getExperienceLevel()
                                + ")";
                Button button = Button.primary(buttonId, buttonLabel);
                actionRows.add(ActionRow.of(button));
            }
        }
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder = messageCreateBuilder.addComponents(actionRows);
        messageCreateBuilder =
                messageCreateBuilder.setContent(
                        "Please select a time slot for your mock interview.");
        event.reply(messageCreateBuilder.build()).setEphemeral(true).queue();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String userId = event.getUser().getId();
        if (!userSlots.containsKey(userId)) {
            event.reply("Session expired or invalid selection.").setEphemeral(true).queue();
            return;
        }

        String buttonId = event.getButton().getId();
        List<AvailableInterviewer> availableInterviewersPST = userSlots.get(userId);

        if (buttonId != null && buttonId.startsWith("book:")) {
            try {
                int interviewIndex = Integer.parseInt(buttonId.split(":")[1]);
                int timeSlotIndex = Integer.parseInt(buttonId.split(":")[2]);
                TimeSlot selectedTimeSlot =
                        availableInterviewersPST
                                .get(interviewIndex)
                                .getAvailableTimeSlots()
                                .get(timeSlotIndex);
                String selectedStartTime =
                        selectedTimeSlot
                                .getStartTime()
                                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
                String selectedEndTime =
                        selectedTimeSlot
                                .getEndTime()
                                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
                String interviewerId =
                        availableInterviewersPST
                                .get(interviewIndex)
                                .getInterviewer()
                                .getId()
                                .toString();
                String interviewerName =
                        availableInterviewersPST.get(interviewIndex).getInterviewer().getName();
                String experienceLevel =
                        availableInterviewersPST
                                .get(interviewIndex)
                                .getInterviewer()
                                .getExperienceLevel();
                // update the booked time slot to interviewer db and check if booking is successful
                // covert and pass startTime in UTC
                LocalDateTime selectedStartTimeUTC =
                        timeZoneController.toDatabaseTime(selectedTimeSlot.getStartTime());
                LocalDateTime selectedEndTimeUTC =
                        timeZoneController.toDatabaseTime(selectedTimeSlot.getEndTime());
                boolean bookingSucess =
                        bookingController.canBook(interviewerId, selectedStartTimeUTC);
                log.info("bookingSucess: {}", bookingSucess);
                if (!bookingSucess) {
                    event.reply("Failed to book the selected time slot, please try again.")
                            .setEphemeral(true)
                            .queue();
                    return;
                } else {
                    // update the booked interview to interviews db
                    // covert and pass Time in UTC
                    String interviewId =
                            interviewController.addOrUpdateInterview(
                                    interviewerId, selectedStartTimeUTC, selectedEndTimeUTC);
                    log.info("add or update interviewer", interviewerId);
                    // update the booked interview to user db
                    usersController.addOrUpdateUser(userId, interviewId);
                    log.info("add or update user", userId);

                    String messageContent =
                            String.format(
                                    "You have successfully booked mock interview with %s (%s level) from %s to %s.",
                                    interviewerName,
                                    experienceLevel,
                                    selectedStartTime,
                                    selectedEndTime);
                    event.reply(messageContent).setEphemeral(true).queue();
                }
            } catch (NumberFormatException e) {
                event.reply("Failed to parse selection, please try again")
                        .setEphemeral(true)
                        .queue();
            } catch (IndexOutOfBoundsException e) {
                event.reply("Invalid selection, please try again.").setEphemeral(true).queue();
            }
        } else {
            event.reply("Something goes wrong, please try again.").setEphemeral(true).queue();
        }
    }
}
