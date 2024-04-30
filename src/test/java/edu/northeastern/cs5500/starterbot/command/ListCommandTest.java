package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

public class ListCommandTest {
    @Test
    void testNameMatchesData() {
        ListCommand listCommand = new ListCommand();
        String name = listCommand.getName();
        CommandData commandData = listCommand.getCommandData();

        assertThat(name).isEqualTo(commandData.getName());
    }
}
