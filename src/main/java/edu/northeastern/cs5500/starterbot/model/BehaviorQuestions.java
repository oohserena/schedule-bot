package edu.northeastern.cs5500.starterbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorQuestions implements Model {

    private ObjectId id;
    private String questionName;
}
