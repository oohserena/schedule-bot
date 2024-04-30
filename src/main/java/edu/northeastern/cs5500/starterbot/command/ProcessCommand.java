package edu.northeastern.cs5500.starterbot.command;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
public class ProcessCommand implements SlashCommandHandler {

    static final String NAME = "process";

    @Inject
    public ProcessCommand() {
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
        return Commands.slash(getName(), "Show mock interview process template");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /process");

        String processTemplate =
                "Mock Interview Process:\n"
                        + "1. Introduction (5-10 minutes)\n"
                        + "2. Behavioral Questions (15-20 minutes)\n"
                        + "3. Technical Questions (20-30 minutes)\n"
                        + "4. Chat (5-10 minutes)\n"
                        + "5. Feedback (10-15 minutes)";
        event.reply(processTemplate).queue();
    }
}
