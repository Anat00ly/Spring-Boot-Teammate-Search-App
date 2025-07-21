package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.models.Post;
import com.example.research2.SpringBoot.services.PlayerService;
import com.example.research2.SpringBoot.services.PostService;
import com.example.research2.SpringBoot.util.GamesUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
    public String showMainPage(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts",posts);
        return "mainPage";
    }

    @GetMapping("create-post")
    public String showCreatePostForm(Model model){
        model.addAttribute("newPost",new Post());
        model.addAttribute("game", GamesUtils.getAllGames());
        return "create-post";
    }

    @PostMapping("/post")
    public String createPost(Post newPost, Principal principal){
        Player player = playerService.findPlayerByEmail(principal.getName());
        postService.createPost(player.getId(),newPost);
        return "redirect:/";
    }

}
