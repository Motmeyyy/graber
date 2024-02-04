package com.example.graber.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Posts")
@Data
public class Post {
    @Id
    private String id;
    private String title;
    private String content;
    private List<String> tags;

    public Post(String title, String content, List<String> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

}
