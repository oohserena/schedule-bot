package edu.northeastern.cs5500.starterbot.repository;

import static org.junit.jupiter.api.Assertions.*;

import edu.northeastern.cs5500.starterbot.model.TechQuestions;
import edu.northeastern.cs5500.starterbot.service.MongoDBService;
import java.util.Collection;
import org.bson.types.ObjectId;
import org.junit.Test;

public class MongoDBConnectionTest {

    MongoDBRepository<TechQuestions> repository;

    public MongoDBConnectionTest() {
        MongoDBService mongoDBService = new MongoDBService();

        repository = new MongoDBRepository<>(TechQuestions.class, mongoDBService);
    }

    @Test
    public void testConnection() {

        assertNotNull(repository, "Repository is null");
        Collection<TechQuestions> questions = repository.getAll();
        assertNotNull(questions, "Failed to fetch data from the database");

        assertFalse(questions.isEmpty(), "No data was found in the database");

        questions.forEach(
                question -> {
                    assertNotNull(question.getId(), "Question ID is null");
                    assertNotNull(question.getQuestionName(), "Question Name is null");
                    assertNotNull(question.getUrl(), "Question URL is null");
                    // print question all info
                    System.out.println(question.toString());
                });

        ObjectId someObjectId = new ObjectId("65f3500cd704ef32baabc64f");
        TechQuestions question = repository.get(someObjectId);
        assertNotNull(question, "The question with the given ID was not found");
    }
}
