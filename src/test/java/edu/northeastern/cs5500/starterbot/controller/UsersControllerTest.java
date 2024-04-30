package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.Users;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UsersControllerTest {

    private UsersController usersController;
    private InMemoryRepository<Users> userRepository;

    @BeforeEach
    public void setup() {
        userRepository = new InMemoryRepository<>();
        usersController = new UsersController(userRepository);
    }

    @Test
    public void testFindUserByDiscordId() {
        Users user = new Users();
        user.setDiscordId("123");
        userRepository.add(user);
        Users result = usersController.findUserByDiscordId("123");
        assertThat(result.getDiscordId()).isEqualTo("123");
    }

    @Test
    public void testAddNewUser() {
        String discordId = "newUser";
        String interviewId = "interview123";
        Users user = usersController.findUserByDiscordId(discordId);
        assertThat(user).isNull();
        usersController.addOrUpdateUser(discordId, interviewId);
        Users newUser = usersController.findUserByDiscordId(discordId);
        assertThat(newUser.getDiscordId()).isEqualTo(discordId);
        assertThat(newUser.getInterviewIds()).contains(interviewId);

        // Test updating an existing user
        String newInterviewId = "newInterviewId";
        usersController.addOrUpdateUser(discordId, newInterviewId);
        Users updatedUser = usersController.findUserByDiscordId(discordId);
        assertThat(updatedUser.getInterviewIds()).contains(newInterviewId);

        // Test user already has an existing interviewId
        usersController.addOrUpdateUser(discordId, newInterviewId);
        Users updatedUser2 = usersController.findUserByDiscordId(discordId);
        assertThat(updatedUser2.getInterviewIds().size()).isEqualTo(2);
    }
}
