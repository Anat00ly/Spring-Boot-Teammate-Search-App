package com.example.research2.SpringBoot.repositories;

import com.example.research2.SpringBoot.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepo extends JpaRepository<Player, Long> {
    List<Player> findByName(String name);
    Optional<Player> findByEmail(String Email);
    boolean existsByEmail(String email);
    List<Player> findAllByEmailNot(String email);
    Optional<Player> findById(Long id);
    List<Player> findAll();

}
