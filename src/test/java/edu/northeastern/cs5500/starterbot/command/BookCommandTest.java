package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

public class BookCommandTest {
    @Test
    void testNameMatchesData() {
        BookCommand bookingCommand = new BookCommand();
        String name = bookingCommand.getName();
        CommandData commandData = bookingCommand.getCommandData();

        assertThat(name).isEqualTo(commandData.getName());
    }
}
