package edu.northeastern.cs5500.starterbot.command;

import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ModalHandler {
    @Nonnull
    public String getName();

    @Nonnull
    public CommandData getCommandData();

    public void onModalInteraction(@Nonnull ModalInteractionEvent event);
}
