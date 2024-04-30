package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

public class DropdownCommandTest {
    @Test
    void testNameMatchesData() {
        DropdownCommand dropdownCommand = new DropdownCommand();
        String name = dropdownCommand.getName();
        CommandData commandData = dropdownCommand.getCommandData();

        assertThat(name).isEqualTo(commandData.getName());
    }
}
