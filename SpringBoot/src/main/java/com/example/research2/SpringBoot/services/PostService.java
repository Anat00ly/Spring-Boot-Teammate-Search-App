package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.models.Post;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import com.example.research2.SpringBoot.repositories.PostRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepo postRepo;
    private final PlayerRepo playerRepo;

    public PostService(PostRepo postRepo, PlayerRepo playerRepo) {
        this.postRepo = postRepo;
        this.playerRepo = playerRepo;
    }

    public List<Post> getAllPosts() {
        return postRepo.findAllByOrderByCreatedAtDesc();
    }

    public Post findPostById(Long id) {
        return postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Пост не найден: " + id));
    }

    public Post savePost(Post post) {
        return postRepo.save(post);
    }

    public Post createPost(Long playerId, Post post) {
        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + playerId));
        post.setPlayer(player);
        post.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt() : LocalDateTime.now());
        return postRepo.save(post);
    }

    public void updatePost(Long id, Post updatedPost, Long userId) {
        Post currentPost = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Пост не найден: " + id));
        if (!currentPost.getPlayer().getId().equals(userId)) {
            throw new RuntimeException("У вас нет прав для редактирования этого поста");
        }
        currentPost.setTitle(updatedPost.getTitle());
        currentPost.setGame(updatedPost.getGame());
        currentPost.setDescription(updatedPost.getDescription());
        currentPost.setQuantityOfHours(updatedPost.getQuantityOfHours());
        postRepo.save(currentPost);
    }

    public void deletePost(Long id, Long userId) {
        Post currentPost = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Пост не найден: " + id));
        if (!currentPost.getPlayer().getId().equals(userId)) {
            throw new RuntimeException("У вас нет прав для удаления этого поста");
        }
        postRepo.delete(currentPost);
    }

    public List<Post> searchPost(String title, String game, Integer hoursFrom, Integer hoursTo){
        List<Post> allPosts = postRepo.findAll();
        return allPosts.stream()
                .filter(post -> title == null || title.isEmpty() ||
                        post.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(post -> game == null || game.isEmpty() ||
                        post.getGame().toLowerCase().contains(game.toLowerCase()))
                .filter(post -> hoursFrom == null || post.getQuantityOfHours() >= hoursFrom)
                .filter(post -> hoursTo == null || post.getQuantityOfHours() <= hoursTo)
                .collect(Collectors.toList());
    }

    public Post findById(Long id) {
        Optional<Post> optionalPost = postRepo.findById(id);
        return optionalPost.orElse(null);
    }
}