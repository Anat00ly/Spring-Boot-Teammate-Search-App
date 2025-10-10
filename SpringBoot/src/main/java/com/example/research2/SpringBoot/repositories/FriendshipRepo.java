package com.example.research2.SpringBoot.repositories;

import com.example.research2.SpringBoot.models.FriendRequestStatus;
import com.example.research2.SpringBoot.models.Friendship;
import com.example.research2.SpringBoot.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendshipRepo extends JpaRepository<Friendship, Long> {

        // Проверка на существование дружбы
        boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
        Optional<Friendship> findBySenderIdAndReceiverId(Long senderId, Long receiverId);


        boolean existsBySenderIdAndReceiverIdAndFriendRequestStatus(Long senderId,
                                                                    Long receiverId,
                                                                    FriendRequestStatus friendRequestStatus);



        boolean existsBySenderAndReceiverAndFriendRequestStatus(Player sender, Player receiver, FriendRequestStatus friendRequestStatus);
}
