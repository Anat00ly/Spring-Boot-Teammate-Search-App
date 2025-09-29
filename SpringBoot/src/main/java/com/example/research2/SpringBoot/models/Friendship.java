    package com.example.research2.SpringBoot.models;

    import jakarta.persistence.*;

    import java.time.LocalDateTime;
    import java.util.Objects;

    @Entity
    @Table(name = "friendship",
            uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"}))
    public class Friendship {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "receiver_id", nullable = false)
        private Player receiver;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sender_id", nullable = false)
        private Player sender;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private FriendRequestStatus friendRequestStatus;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        public Friendship() {}

        public Friendship(Player receiver, Player sender, FriendRequestStatus friendRequestStatus) {
            this.receiver = receiver;
            this.sender = sender;
            this.friendRequestStatus = friendRequestStatus;
        }

        // — геттеры/сеттеры
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Player getReceiver() { return receiver; }
        public void setReceiver(Player receiver) { this.receiver = receiver; }

        public Player getSender() { return sender; }
        public void setSender(Player sender) { this.sender = sender; }

        public FriendRequestStatus getFriendRequestStatus() { return friendRequestStatus; }
        public void setFriendRequestStatus(FriendRequestStatus friendRequestStatus) {
            this.friendRequestStatus = friendRequestStatus;
        }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Friendship that = (Friendship) o;
            return Objects.equals(id, that.id)
                    && Objects.equals(receiver, that.receiver)
                    && Objects.equals(sender, that.sender)
                    && friendRequestStatus == that.friendRequestStatus
                    && Objects.equals(createdAt, that.createdAt)
                    && Objects.equals(updatedAt, that.updatedAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, receiver, sender, friendRequestStatus, createdAt, updatedAt);
        }
    }
