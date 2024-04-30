package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.TechQuestions;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class TechQuestionsControllerTest {

    private TechQuestionsController techQuestionsController;
    private InMemoryRepository<TechQuestions> techQuestionsRepository;

    @BeforeEach
    public void setup() {
        techQuestionsRepository = new InMemoryRepository<>();
        techQuestionsController = new TechQuestionsController(techQuestionsRepository);
    }

    @Test
    public void testGetAllTechQuestions() {
        TechQuestions firstQuestion = new TechQuestions();
        firstQuestion.setId(new ObjectId());
        firstQuestion.setQuestionName("Best Time to Buy and Sell Stock");
        firstQuestion.setUrl(
                "https://leetcode.com/problems/best-time-to-buy-and-sell-stock/description/\n");
        TechQuestions secondQuestion = new TechQuestions();
        secondQuestion.setId(new ObjectId());
        secondQuestion.setQuestionName("Two Sum");
        secondQuestion.setUrl("https://leetcode.com/problems/two-sum/description/\n");

        log.info("size: {}", techQuestionsRepository.getAll().size());

        techQuestionsRepository.add(firstQuestion);
        techQuestionsRepository.add(secondQuestion);
        List<TechQuestions> allTechQuestions =
                new ArrayList<>(techQuestionsController.getAllTechQuestions());

        assertThat(techQuestionsRepository.getAll().size()).isEqualTo(3);
        assertThat(allTechQuestions)
                .containsExactlyElementsIn(new ArrayList<>(techQuestionsRepository.getAll()));
    }
}
