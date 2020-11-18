package com.example.logging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class User {
    private String name;
    private String ssn;
    private String email;
    private String description;
}
