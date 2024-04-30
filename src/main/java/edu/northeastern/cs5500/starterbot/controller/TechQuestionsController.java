package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.TechQuestions;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.FakeOpenTelemetryService;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TechQuestionsController {
    GenericRepository<TechQuestions> techQuestionsRepository;
    @Inject OpenTelemetry openTelemetry;

    @Inject
    public TechQuestionsController(GenericRepository<TechQuestions> techQuestionsRepository) {
        this.techQuestionsRepository = techQuestionsRepository;

        // Add default question if none exists
        if (techQuestionsRepository.count() == 0) {
            TechQuestions techQuestions = new TechQuestions();
            techQuestions.setQuestionName("Two Sum");
            techQuestions.setUrl("https://leetcode.com/problems/two-sum/description/");
            techQuestionsRepository.add(techQuestions);
        }

        // openTelemetry = new OpenTelemetryService();
        openTelemetry = new FakeOpenTelemetryService();
    }

    /** Get all technical questions */
    public List<TechQuestions> getAllTechQuestions() {
        var span = openTelemetry.span("getAllTechQuestions");
        // return (List<TechQuestions>) techQuestionsRepository.getAll();
        return new ArrayList<>(techQuestionsRepository.getAll());
    }
}
