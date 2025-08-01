package com.example.research2.SpringBoot.repositories;

import com.example.research2.SpringBoot.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepo extends JpaRepository<Post,Long> {

    List<Post> findAllByOrderByCreatedAtDesc();
}
