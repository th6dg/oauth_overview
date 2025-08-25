package com.wiredpackage.oauth.shared.constants;

public enum ServiceRolePath {
    MANAGER("mgr"),
    STAFF("staff");

    private final String path;

    ServiceRolePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
