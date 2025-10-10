package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.models.Post;
import com.example.research2.SpringBoot.services.NotificationService;
import com.example.research2.SpringBoot.services.PlayerService;
import com.example.research2.SpringBoot.services.PostService;
import com.example.research2.SpringBoot.util.GamesUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class MainPageController {

    private final PostService postService;
    private final PlayerService playerService;
    private final NotificationService notificationService;

    public MainPageController(PostService postService, PlayerService playerService, NotificationService notificationService) {
        this.postService = postService;
        this.playerService = playerService;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String showMainPage(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String game,
            @RequestParam(required = false) Integer hoursFrom,
            @RequestParam(required = false) Integer hoursTo,
            Model model,
            Principal principal) {

        if(principal != null){
            Player currentPlayer = playerService.findPlayerByEmail(principal.getName());
            model.addAttribute("currentPlayer",currentPlayer);
        }

        model.addAttribute("games",GamesUtils.getAllGames());

        if(title == null && game == null && hoursFrom == null && hoursTo == null){
            List<Post> posts = postService.getAllPosts();
            model.addAttribute("posts", posts);
        } else {
            List<Post> posts = postService.searchPost(
                    title,game,hoursFrom,hoursTo
            );
            model.addAttribute("posts", posts);
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
            return "create-post";
        }
    }

    @GetMapping("/post/{id}")
    public String showPost(@PathVariable Long id, Model model, Principal principal) {
        try {
            Post post = postService.findPostById(id);
            model.addAttribute("post", post);
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
                model.addAttribute("currentUserId", player.getId());
                return "post";
            }
            model.addAttribute("post", post);
            model.addAttribute("games", GamesUtils.getAllGames());
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
                model.addAttribute("currentUserId", player.getId());
                return "post";
            }
            if (result.hasErrors()) {
                model.addAttribute("games", GamesUtils.getAllGames());
                model.addAttribute("error", "Пожалуйста, заполните все обязательные поля.");
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

    @PostMapping("/post/{id}/respond")
    public String respondToPost(@PathVariable("id") Long postId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Необходимо войти, чтобы откликнуться на пост.");
            return "redirect:/login";
        }

        try {
            Player sender = playerService.findPlayerByEmail(principal.getName());
            if (sender == null) {
                redirectAttributes.addFlashAttribute("error", "Пользователь не найден.");
                return "redirect:/";
            }

            Post post = postService.findById(postId);
            if (post == null) {
                redirectAttributes.addFlashAttribute("error", "Пост не найден.");
                return "redirect:/";
            }

            Player receiver = post.getPlayer();
            if (receiver == null) {
                redirectAttributes.addFlashAttribute("error", "Владелец поста не найден.");
                return "redirect:/";
            }

            if (receiver.getId().equals(sender.getId())) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете откликнуться на свой пост.");
                return "redirect:/post/" + postId;
            }

            notificationService.sendRespondPostNotification(receiver, sender, postId);
            redirectAttributes.addFlashAttribute("success", "Вы успешно откликнулись на пост!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отклике: " + e.getMessage());
        }

        return "redirect:/";
    }
}