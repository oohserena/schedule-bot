package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.Users;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.FakeOpenTelemetryService;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jetty.util.log.Log;

@Singleton
public class UsersController {

    GenericRepository<Users> userRepository;
    @Inject OpenTelemetry openTelemetry;

    @Inject
    UsersController(GenericRepository<Users> userRepository) {
        this.userRepository = userRepository;
        openTelemetry = new FakeOpenTelemetryService();
    }

    /**
     * Find user by discord ID
     *
     * @param discordId, discord ID of the user
     * @return user
     */
    public Users findUserByDiscordId(String discordId) {
        return userRepository.getAll().stream()
                .filter(user -> discordId.equals(user.getDiscordId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add or update interview info in user collection to db
     *
     * @param discordId, discord ID of the user
     * @param interviewId, interview ID
     */
    public void addOrUpdateUser(String discordId, String interviewId) {
        Users user = findUserByDiscordId(discordId);
        if (user == null) {
            user = new Users();
            user.setDiscordId(discordId);
            user.setName(
                    "default user name"); // set default name, can use /preferredname command to set
            // name
            user.setInterviewIds(new ArrayList<>());
        }

        if (!user.getInterviewIds().contains(interviewId)) {
            user.getInterviewIds().add(interviewId);
            try {
                if (findUserByDiscordId(discordId) == null) {
                    userRepository.add(user);
                    Log.getRootLogger().info("User added with interview ID.");
                } else {
                    userRepository.update(user);
                    Log.getRootLogger().info("User updated with new interview ID.");
                }
            } catch (Exception e) {
                Log.getRootLogger().warn("Failed to add or update user with interview ID", e);
                throw new RuntimeException("Failed to add or update user with interview ID", e);
            }
        } else {
            Log.getRootLogger().info("User already has this interview ID.");
        }
    }
}
