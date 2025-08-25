package com.wiredpackage.oauth.shared.constants;

public class OAuthSocketEvents {
    private OAuthSocketEvents() {
    }

    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
    public static final String JOIN_WAITING_APPROVAL_ROOM = "join_waiting_approval_room";
    public static final String LEAVE_WAITING_APPROVAL_ROOM = "leave_waiting_approval_room";
    public static final String HEALTH_CHECK = "health_check";
}
