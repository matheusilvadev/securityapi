package com.astrocode.securityapi.dtos;

public record FeedItemDTO(long postId,
                          String content,
                          String username) {
}
