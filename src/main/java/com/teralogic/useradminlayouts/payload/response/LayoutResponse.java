package com.teralogic.useradminlayouts.payload.response;

import jakarta.validation.Valid;

import java.util.List;

public class LayoutResponse {
    private String username;
    private List<String> layouts;

    public LayoutResponse(@Valid String username, List<String> layouts) {
        this.username = username;
        this.layouts = layouts;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getLayouts() {
        return layouts;
    }

    public void setLayouts(List<String> layouts) {
        this.layouts = layouts;
    }
}
