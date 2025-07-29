package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.models.Post;
import com.example.research2.SpringBoot.services.PlayerService;
import com.example.research2.SpringBoot.services.PostService;
import com.example.research2.SpringBoot.util.GamesUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.time.LocalDateTime;
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
            model.addAttribute("name", principal.getName());
        }
        return "mainPage";
    }

    @GetMapping("/create-post")
    public String showCreatePostForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        model.addAttribute("newPost", new Post());
        model.addAttribute("games", GamesUtils.getAllGames());
        model.addAttribute("name", principal.getName());
        return "create-post";
    }

    @PostMapping("/post")
    public String createPost(@ModelAttribute("newPost") @Valid Post newPost, BindingResult result, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            model.addAttribute("games", GamesUtils.getAllGames());
            model.addAttribute("error", "Пожалуйста, заполните все обязательные поля.");
            model.addAttribute("name", principal.getName());
            return "create-post";
        }
        try {
            Player player = playerService.findPlayerByEmail(principal.getName());
            newPost.setPlayer(player);
            newPost.setCreatedAt(LocalDateTime.now());
            postService.savePost(newPost);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("games", GamesUtils.getAllGames());
            model.addAttribute("error", "Ошибка при создании поста: " + e.getMessage());
            model.addAttribute("name", principal.getName());
            return "create-post";
        }
    }

    @GetMapping("/post/{id}")
    public String showPost(@PathVariable Long id, Model model, Principal principal) {
        try {
            Post post = postService.findPostById(id);
            model.addAttribute("post", post);
            model.addAttribute("name", principal != null ? principal.getName() : null);
            model.addAttribute("currentUserId", principal != null ? playerService.findPlayerByEmail(principal.getName()).getId() : null);
            return "post";
        } catch (Exception e) {
            model.addAttribute("error", "Пост не найден: " + e.getMessage());
            return "mainPage";
        }
    }

    @GetMapping("/edit-post/{id}")
    public String showEditPostForm(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            Post post = postService.findPostById(id);
            Player player = playerService.findPlayerByEmail(principal.getName());
            if (!post.getPlayer().getId().equals(player.getId())) {
                model.addAttribute("post", post);
                model.addAttribute("error", "У вас нет прав для редактирования этого поста");
                model.addAttribute("name", principal.getName());
                model.addAttribute("currentUserId", player.getId());
                return "post";
            }
            model.addAttribute("post", post);
            model.addAttribute("games", GamesUtils.getAllGames());
            model.addAttribute("name", principal.getName());
            return "edit-post";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при загрузке формы редактирования: " + e.getMessage());
            return "mainPage";
        }
    }

    @PostMapping("/edit-post/{id}")
    public String editPost(@PathVariable Long id, @ModelAttribute("post") @Valid Post updatedPost, BindingResult result, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            Post existingPost = postService.findPostById(id);
            Player player = playerService.findPlayerByEmail(principal.getName());
            if (!existingPost.getPlayer().getId().equals(player.getId())) {
                model.addAttribute("post", existingPost);
                model.addAttribute("error", "У вас нет прав для редактирования этого поста");
                model.addAttribute("name", principal.getName());
                model.addAttribute("currentUserId", player.getId());
                return "post";
            }
            if (result.hasErrors()) {
                model.addAttribute("games", GamesUtils.getAllGames());
                model.addAttribute("error", "Пожалуйста, заполните все обязательные поля.");
                model.addAttribute("name", principal.getName());
                return "edit-post";
            }
            existingPost.setTitle(updatedPost.getTitle());
            existingPost.setGame(updatedPost.getGame());
            existingPost.setDescription(updatedPost.getDescription());
            existingPost.setQuantityOfHours(updatedPost.getQuantityOfHours());
            postService.savePost(existingPost);
            return "redirect:/post/" + id;
        } catch (Exception e) {
            model.addAttribute("games", GamesUtils.getAllGames());
            model.addAttribute("error", "Ошибка при сохранении поста: " + e.getMessage());
            model.addAttribute("name", principal.getName());
            return "edit-post";
        }
    }

    @PostMapping("/delete-post/{id}")
    public String deletePost(@PathVariable Long id, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            Post post = postService.findPostById(id);
            Player player = playerService.findPlayerByEmail(principal.getName());
            if (!post.getPlayer().getId().equals(player.getId())) {
                model.addAttribute("post", post);
                model.addAttribute("error", "У вас нет прав для удаления этого поста");
                model.addAttribute("name", principal.getName());
                model.addAttribute("currentUserId", player.getId());
                return "post";
            }
            postService.deletePost(id, player.getId());
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при удалении поста: " + e.getMessage());
            return "mainPage";
        }
    }
}