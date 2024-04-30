package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

public class QuestionsCommandTest {
    @Test
    void testNameMatchesData() {
        QuestionsCommand questionsCommand = new QuestionsCommand();
        String name = questionsCommand.getName();
        CommandData commandData = questionsCommand.getCommandData();

        assertThat(name).isEqualTo(commandData.getName());
    }
}
