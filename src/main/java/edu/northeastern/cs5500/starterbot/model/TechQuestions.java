package edu.northeastern.cs5500.starterbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechQuestions implements Model {

    private ObjectId id; // MongoDB ID field
    private String questionName; // Name of the question
    private String url; // URL to the question
    public static final String TECH_QUESTIONS = "TechQuestions";
}
