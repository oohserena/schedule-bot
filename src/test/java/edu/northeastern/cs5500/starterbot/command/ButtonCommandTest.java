package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

public class ButtonCommandTest {
    @Test
    void testNameMatchesData() {
        ButtonCommand buttonCommand = new ButtonCommand();
        String name = buttonCommand.getName();
        CommandData commandData = buttonCommand.getCommandData();

        assertThat(name).isEqualTo(commandData.getName());
    }
}
