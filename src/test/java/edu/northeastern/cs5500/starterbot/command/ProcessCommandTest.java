package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

@Slf4j
public class ProcessCommandTest {

    @Test
    void testNameMatchesData() {
        ProcessCommand processCommand = new ProcessCommand();
        String name = processCommand.getName();
        CommandData commandData = processCommand.getCommandData();
        assertThat(name).isEqualTo(commandData.getName());
    }
}
