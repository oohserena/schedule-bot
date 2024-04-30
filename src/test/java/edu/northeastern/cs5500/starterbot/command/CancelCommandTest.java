package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

public class CancelCommandTest {
    @Test
    void testNameMatchesData() {
        CancelCommand cancelCommand = new CancelCommand();
        String name = cancelCommand.getName();
        CommandData commandData = cancelCommand.getCommandData();

        assertThat(name).isEqualTo(commandData.getName());
    }
}
