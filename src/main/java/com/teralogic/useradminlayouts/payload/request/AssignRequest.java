package com.teralogic.useradminlayouts.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class AssignRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;


    private Set<Integer> layout;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Integer> getLayout() {
        return layout;
    }

    public void setLayout(Set<Integer> layout) {
        this.layout = layout;
    }
}
