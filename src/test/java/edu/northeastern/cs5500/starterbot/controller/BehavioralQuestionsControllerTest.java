package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.BehaviorQuestions;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class BehavioralQuestionsControllerTest {

    private BehavioralQuestionsController behavioralQuestionsController;
    private InMemoryRepository<BehaviorQuestions> behaviorQuestionsRepository;

    @BeforeEach
    public void setup() {
        behaviorQuestionsRepository = new InMemoryRepository<>();
        behavioralQuestionsController =
                new BehavioralQuestionsController(behaviorQuestionsRepository);
    }

    @Test
    public void testGetAllBehavioralQuestions() {
        BehaviorQuestions firstQuestion = new BehaviorQuestions();
        firstQuestion.setId(new ObjectId());
        firstQuestion.setQuestionName("Tell me about yourself");
        BehaviorQuestions secondQuestion = new BehaviorQuestions();
        secondQuestion.setId(new ObjectId());
        secondQuestion.setQuestionName("What are your strengths?");

        log.info("size: {}", behaviorQuestionsRepository.getAll().size());
        behaviorQuestionsRepository.add(firstQuestion);
        behaviorQuestionsRepository.add(secondQuestion);
        List<BehaviorQuestions> allBehaviorQuestions =
                new ArrayList<>(behavioralQuestionsController.getAllBehaviorQuestions());

        assertThat(behaviorQuestionsRepository.getAll().size()).isEqualTo(3);
        assertThat(allBehaviorQuestions)
                .containsExactlyElementsIn(new ArrayList<>(behaviorQuestionsRepository.getAll()))
                .inOrder();
    }
}
