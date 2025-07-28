package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.models.Post;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import com.example.research2.SpringBoot.repositories.PostRepo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    private final PostRepo postRepo;
    private final PlayerRepo playerRepo;

    public PostService(PostRepo postRepo, PlayerRepo playerRepo) {
        this.postRepo = postRepo;
        this.playerRepo = playerRepo;
    }

    public List<Post> getAllPosts(){
        return postRepo.findAllByOrderByCreatedAtDesc();
    }

    public Post createPost(Long playerId, Post post) {
        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + playerId));

        Post newPost = new Post();
        newPost.setTitle(post.getTitle());
        newPost.setPlayer(player);
        newPost.setGame(post.getGame());
        newPost.setDescription(post.getDescription());
        newPost.setQuantityOfHours(post.getQuantityOfHours());
        newPost.setCreatedAt(LocalDateTime.now());

        return postRepo.save(newPost);
    }

    public Post findPostById(Long id) {
        return postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Can't find the post"));
    }
}
