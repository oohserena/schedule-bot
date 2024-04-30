package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.InterviewController;
import edu.northeastern.cs5500.starterbot.controller.TimeZoneController;
import edu.northeastern.cs5500.starterbot.controller.UsersController;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.model.Users;
import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

@Slf4j
public class NotesCommand implements SlashCommandHandler, ButtonHandler, ModalHandler {

    static final String NAME = "notes";

    @Inject InterviewController interviewController;
    @Inject UsersController usersController;
    @Inject TimeZoneController timeZoneController;

    @Inject
    public NotesCommand() {
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
        return Commands.slash(getName(), "Create Notes for an Interview");
    }

    /**
     * This method is called when the user interacts with the /notes command. It will display the
     * user's scheduled interviews and allow them to edit the notes for each interview.
     *
     * @param event The SlashCommandInteractionEvent
     * @return void
     */
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /notes");
        String userId = event.getUser().getId();

        // Find the user by their discord id
        Users user = usersController.findUserByDiscordId(userId);
        if (user == null) {
            event.deferReply(true).setContent("You have no interviews scheduled.").queue();
            return;
        }

        // Find the user's interviews
        List<Interviews> userInterviews =
                interviewController.findInterviewsByIds(user.getInterviewIds());
        if (userInterviews.isEmpty()) {
            event.deferReply(true).setContent("You have no interviews scheduled.").queue();
            return;
        }

        // Acknowledge the command and defer the reply
        event.deferReply().queue();
        // Create a new thread to send the interview details to the user
        new Thread(
                        () -> {
                            // Send the interview details to the user
                            for (Interviews interview : userInterviews) {
                                MessageCreateBuilder messageCreateBuilder =
                                        new MessageCreateBuilder();
                                String interviewId = interview.getId().toString();
                                String formattedInterview = formatInterviewForDisplay(interview);

                                // Create an embed for the interview details
                                MessageEmbed interviewEmbed =
                                        new EmbedBuilder()
                                                .setDescription(formattedInterview)
                                                .build();
                                // Create a button to edit the notes
                                Button editButton =
                                        Button.primary(
                                                getName() + ":edit-notes:" + interviewId,
                                                "Edit Notes");

                                // Add the embed and button to the message
                                messageCreateBuilder
                                        .addEmbeds(interviewEmbed)
                                        .addActionRow(editButton);
                                // Send the message to the user
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

    /**
     * Allow the user to edit the notes for an interview.
     *
     * @param event The ButtonInteractionEvent
     * @return void
     */
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {

        // Get the component id from the event
        String componentId = event.getComponentId();
        // Split the component id to get the handler name, action, and interview id
        String[] parts = componentId.split(":");
        if (parts.length < 3) {
            event.reply("Invalid button.").setEphemeral(true).queue();
            return;
        }

        String handlerName = parts[0];
        String action = parts[1];
        String interviewId = parts[2];
        // Check if the handler name is correct
        Interviews interview = interviewController.getInterviewById(interviewId);
        if (interview == null) {
            event.reply("Interview not found.").setEphemeral(true).queue();
            return;
        }
        // Check if the action is correct
        String noteTitle =
                interview.getTitle() == null
                        ? "Enter the title of your noted here"
                        : interview.getTitle();
        String noteDescription =
                interview.getNotes() == null
                        ? "Enter the description of your note here"
                        : interview.getNotes();

        if (action.equals("edit-notes")) {
            // Create a text input for the note title
            TextInput titleInput =
                    TextInput.create("note-title", "Note Title", TextInputStyle.SHORT)
                            .setPlaceholder(noteTitle)
                            .setRequired(true)
                            .build();
            // Create a text input for the note description
            TextInput descriptionInput =
                    TextInput.create(
                                    "note-description",
                                    "Note Description",
                                    TextInputStyle.PARAGRAPH)
                            .setPlaceholder(noteDescription)
                            .setRequired(true)
                            .build();
            // Create a modal for the user to edit the notes
            Modal modal =
                    Modal.create(getName() + ":edit-notes:" + interviewId, "Edit Notes")
                            .addActionRows(ActionRow.of(titleInput), ActionRow.of(descriptionInput))
                            .build();
            // Send the modal to the user
            event.replyModal(modal).queue();
        } else {
            event.reply("Invalid button.").setEphemeral(true).queue();
        }
    }

    /**
     * Format the interview for display in the user's timezone.
     *
     * @param interview The interview to format
     * @return String
     */
    private String formatInterviewForDisplay(Interviews interview) {
        // convert interview start and end time to PST for user display
        LocalDateTime startTime = timeZoneController.toUserLocalTime(interview.getStart());
        LocalDateTime endTime = timeZoneController.toUserLocalTime(interview.getEnd());
        return String.format(
                "%s (Time: %s to %s)",
                interviewController.getInterviewerNameById(interview.getInterviewerId()),
                startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    /**
     * This method is called when the user interacts with the modal to edit the notes. It will save
     * the notes to the database and reply to the user acknowledging the note was saved.
     *
     * @param event The ModalInteractionEvent
     * @return void
     */
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        // Get the modal id from the event
        String modelId = event.getModalId();
        String[] names = modelId.split(":", 3);
        // String handlerName = names[0];
        // String action = names[1];
        String interviewId = names[2];

        // Get the note title and description from the modal
        String noteTitle =
                event.getValue("note-title").getAsString(); // Get the note title from the modal
        String noteDescription =
                event.getValue("note-description")
                        .getAsString(); // Get the note description from the modal

        // Save the note to the database
        saveNoteForInterview(interviewId, noteTitle, noteDescription);

        // Create an embed to acknowledge the note was saved
        MessageEmbed noteEmbed =
                new EmbedBuilder()
                        .setTitle("Note saved")
                        .addField(noteTitle, noteDescription, false)
                        .setFooter("Interviews BOT | /notes to edit your notes", null)
                        .setColor(new Color(0x4E5D94))
                        .build();

        // Reply to the user acknowledging the note was saved
        event.replyEmbeds(noteEmbed).setEphemeral(true).queue();
    }

    private void saveNoteForInterview(String interviewId, String title, String description) {
        interviewController.updateInterviewNotes(interviewId, title, description);
    }
}
