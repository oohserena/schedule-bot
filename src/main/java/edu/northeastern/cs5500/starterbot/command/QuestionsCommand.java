package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.BehavioralQuestionsController;
import edu.northeastern.cs5500.starterbot.controller.TechQuestionsController;
import edu.northeastern.cs5500.starterbot.model.BehaviorQuestions;
import edu.northeastern.cs5500.starterbot.model.TechQuestions;
import java.util.List;
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
public class QuestionsCommand implements SlashCommandHandler, ButtonHandler {
    static final String NAME = "questions";
    // TechQuestionsController techQuestionsController;
    // BehavioralQuestionsController behavioralQuestionsController;
    @Inject TechQuestionsController techQuestionsController;
    @Inject BehavioralQuestionsController behavioralQuestionsController;

    @Inject
    // public QuestionsCommand() {
    //     MongoDBService mongoDBService = new MongoDBService();
    //     techQuestionsController =
    //             new TechQuestionsController(
    //                     new MongoDBRepository<>(TechQuestions.class, mongoDBService));

    //     behavioralQuestionsController =
    //             new BehavioralQuestionsController(
    //                     new MongoDBRepository<>(BehaviorQuestions.class, mongoDBService));
    // }
    public QuestionsCommand() {}

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    @Nonnull
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Demonstrate a button interaction");
    }

    /**
     * This method is called when the user types /questions in the discord chat. It sends a message
     * to the user asking them to choose between Tech and Behavioral questions.
     *
     * @param event SlashCommandInteractionEvent
     * @return void
     */
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /questions");

        // Create a message asking the user to choose between Tech and Behavioral questions
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder =
                messageCreateBuilder.addActionRow(
                        Button.primary(getName() + ":tech", "Tech"),
                        Button.primary(getName() + ":behavioral", "Behavioral"));
        messageCreateBuilder =
                messageCreateBuilder.setContent(
                        "Please choose a category of questions: Tech or Behavioral.");
        event.reply(messageCreateBuilder.build()).queue();
    }

    /**
     * This method sends a message to the user with the questions based on the button clicked.
     *
     * @param event ButtonInteractionEvent
     * @return void
     */
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        // event.reply(event.getButton().getLabel()).queue();

        // Check which button was clicked and send the appropriate questions
        String label = event.getButton().getLabel();
        if (label.equals("Tech")) {
            log.info("enter Tech condition");
            // Get all tech questions from the database
            List<TechQuestions> techQuestions = techQuestionsController.getAllTechQuestions();
            // Format the questions to be sent to the user
            String techQuestionsString = formatTechQuestionsForDiscord(techQuestions);
            // Send the questions to the user
            event.reply(techQuestionsString)
                    .queue(
                            success -> log.info("Message sent successfully"),
                            error -> log.error("error met:" + error.toString()));
        } else if (label.equals("Behavioral")) {
            log.info("enter Behavioral condition");
            // Get all behavioral questions from the database
            List<BehaviorQuestions> behavioralQuestions =
                    behavioralQuestionsController.getAllBehaviorQuestions();
            // Format the questions to be sent to the user
            String behavioralQuestionsString =
                    formatBehavioralQuestionsForDiscord(behavioralQuestions);
            event.reply(behavioralQuestionsString)
                    .queue(
                            success -> log.info("Message sent successfully"),
                            error -> log.error("error met:" + error.toString()));
        }
    }

    private static String formatTechQuestionsForDiscord(List<TechQuestions> techQuestions) {
        StringBuilder sb = new StringBuilder();
        for (TechQuestions question : techQuestions) {
            sb.append("**Question:** ").append(question.getQuestionName()).append("\n");
            sb.append("**URL:** ").append(question.getUrl()).append("\n\n");
        }
        return sb.toString();
    }

    private static String formatBehavioralQuestionsForDiscord(
            List<BehaviorQuestions> behavioralQuestions) {
        StringBuilder sb = new StringBuilder();
        for (BehaviorQuestions question : behavioralQuestions) {
            sb.append("**Question:** ").append(question.getQuestionName()).append("\n");
        }
        return sb.toString();
    }
}
