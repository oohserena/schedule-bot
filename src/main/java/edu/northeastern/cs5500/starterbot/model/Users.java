package edu.northeastern.cs5500.starterbot.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users implements Model {
    private ObjectId id;
    private String name;
    private List<String> interviewIds;
    private String discordId;
}
