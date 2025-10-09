package com.example.research2.SpringBoot.repositories;

import com.example.research2.SpringBoot.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepo extends JpaRepository<Post,Long> {

    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findAll();
    Optional<Post> findById(Long postId);
}
