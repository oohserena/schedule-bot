package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

public class NotesCommandTest {
    @Test
    void testNameMatchesData() {
        NotesCommand notesCommand = new NotesCommand();
        String name = notesCommand.getName();
        CommandData commandData = notesCommand.getCommandData();

        assertThat(name).isEqualTo(commandData.getName());
    }
}
