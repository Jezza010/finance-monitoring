package com.finmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private Integer id;
    private Integer userId;
    private String sessionToken;
    private Instant createdAt;
} 