package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.BehaviorQuestions;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetryService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BehavioralQuestionsController {
    GenericRepository<BehaviorQuestions> behaviorQuestionsRepository;
    @Inject OpenTelemetry openTelemetry;

    @Inject
    public BehavioralQuestionsController(
            GenericRepository<BehaviorQuestions> behaviorQuestionsRepository) {

        this.behaviorQuestionsRepository = behaviorQuestionsRepository;

        // Add default question if none exists
        if (behaviorQuestionsRepository.count() == 0) {
            BehaviorQuestions behaviorQuestions = new BehaviorQuestions();
            behaviorQuestions.setQuestionName("Tell me about yourself");
            behaviorQuestionsRepository.add(behaviorQuestions);
        }

        openTelemetry = new OpenTelemetryService();
    }

    /**
     * Get all the behavior questions
     *
     * @return behavior questins list
     */
    public List<BehaviorQuestions> getAllBehaviorQuestions() {
        var span = openTelemetry.span("getBehaviorQuestions");
        // return (List<BehaviorQuestions>) behaviorQuestionsRepository.getAll();
        return new ArrayList<>(behaviorQuestionsRepository.getAll());
    }
}
