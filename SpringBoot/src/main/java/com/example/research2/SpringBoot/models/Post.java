package com.example.research2.SpringBoot.models;

import jakarta.persistence.*;
import lombok.extern.apachecommons.CommonsLog;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String title;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private String game;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int quantityOfHours;

    private LocalDateTime createdAt;

    public Post(){}
    public Post(String game, String description, int quantityOfHours, LocalDateTime createdAt) {
        this.game = game;
        this.description = description;
        this.quantityOfHours = quantityOfHours;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantityOfHours() {
        return quantityOfHours;
    }

    public void setQuantityOfHours(int quantityOfHours) {
        this.quantityOfHours = quantityOfHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
