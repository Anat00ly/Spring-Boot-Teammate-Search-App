package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.models.Post;
import com.example.research2.SpringBoot.services.PlayerService;
import com.example.research2.SpringBoot.services.PostService;
import com.example.research2.SpringBoot.util.GamesUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class MainPageController {

    private final PostService postService;
    private final PlayerService playerService;

    public MainPageController(PostService postService, PlayerService playerService) {
        this.postService = postService;
        this.playerService = playerService;
    }

    @GetMapping("/")
    public String showMainPage(Model model, Principal principal) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        if (principal != null) {
            model.addAttribute("name", principal.getName()); // Добавляем email текущего пользователя
        }
        return "mainPage";
    }

    @GetMapping("/create-post")
    public String showCreatePostForm(Model model) {
        model.addAttribute("newPost", new Post());
        model.addAttribute("game", GamesUtils.getAllGames());
        return "create-post";
    }

    @PostMapping("/post")
    public String createPost(Post newPost, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());
        postService.createPost(player.getId(), newPost);
        return "redirect:/";
    }

    @GetMapping("/post/{id}")
    public String showPost(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.findPostById(id);
        model.addAttribute("post", post);
        if (principal != null) {
            model.addAttribute("name", principal.getName()); // Добавляем email для навбара
        }
        return "post";
    }
}