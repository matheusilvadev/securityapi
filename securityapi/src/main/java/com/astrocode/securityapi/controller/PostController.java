package com.astrocode.securityapi.controller;

import com.astrocode.securityapi.dtos.CreatePostDTO;
import com.astrocode.securityapi.entities.Post;
import com.astrocode.securityapi.entities.Role;
import com.astrocode.securityapi.repository.PostRepository;
import com.astrocode.securityapi.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostController(PostRepository postRepository,
                          UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts")
    public ResponseEntity<Void> createPost(@RequestBody CreatePostDTO postDTO,
                                           JwtAuthenticationToken token){
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var post = new Post();
        post.setUser(user.get());
        post.setContent(postDTO.content());

        postRepository.save(post);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long postId,
                                           JwtAuthenticationToken token){
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || post.getUser().getUserId().equals(UUID.fromString(token.getName()))){
            postRepository.deleteById(postId);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        return ResponseEntity.ok().build();
    }

}