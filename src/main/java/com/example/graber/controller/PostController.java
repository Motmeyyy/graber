package com.example.graber.controller;

import com.example.graber.model.Post;
import com.example.graber.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/habr/latest")
    public Flux<Post> getAndSaveLatestHabrArticles() {
        return postService.getAllHabrArticlesFromLastPages(10);
    }
}

