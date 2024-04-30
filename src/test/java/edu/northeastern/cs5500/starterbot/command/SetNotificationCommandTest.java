package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

@Slf4j
class SetNotificationCommandTest {

    @Test
    void testNameMatchesData() {
        SetNotificationCommand setNotificationCommand = new SetNotificationCommand();
        String name = setNotificationCommand.getName();
        CommandData commandData = setNotificationCommand.getCommandData();
        assertThat(name).isEqualTo(commandData.getName());
    }
}
