package com.example.research2.SpringBoot.models;

public enum NotificationType {
    FRIEND_REQUEST("Заявка в друзья", "fa-user-plus"),
    FRIEND_ACCEPTED("Заявка принята", "fa-check-circle"),
    FRIEND_DECLINED("Заявка отклонена", "fa-times-circle"),
    FRIEND_REMOVED("Удален из друзей", "fa-user-minus"),
    SYSTEM("Системное уведомление", "fa-bell"),
    POST_RESPOND("Отклик на пост", "fa-reply");

    private final String displayName;
    private final String iconClass;


    NotificationType(String displayName, String iconClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }
}
