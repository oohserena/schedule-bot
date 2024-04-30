package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

@Slf4j
class CancelNotificationCommandTest {

    @Test
    void testNameMatchesData() {
        CancelNotificationCommand cancelNotificationCommand = new CancelNotificationCommand();
        String name = cancelNotificationCommand.getName();
        CommandData commandData = cancelNotificationCommand.getCommandData();
        assertThat(name).isEqualTo(commandData.getName());
    }
}
