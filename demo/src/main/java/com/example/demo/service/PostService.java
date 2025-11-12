package com.example.demo.service;

import com.example.demo.dto.PostDto;
import com.example.demo.dto.PostRequestDto;
import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    
    private final PostRepository postRepository;
    
    // 전체 게시글 조회 (최신순)
    public List<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 게시글 상세 조회 (조회수 증가)
    @Transactional
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id));
        
        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        
        return convertToDto(post);
    }
    
    // 게시글 생성
    @Transactional
    public PostDto createPost(PostRequestDto requestDto) {
        Post post = new Post();
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setAuthor(requestDto.getAuthor());
        post.setViewCount(0);
        
        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }
    
    // 게시글 수정
    @Transactional
    public PostDto updatePost(Long id, PostRequestDto requestDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id));
        
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setAuthor(requestDto.getAuthor());
        
        return convertToDto(post);
    }
    
    // 게시글 삭제
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id);
        }
        postRepository.deleteById(id);
    }
    
    // 제목으로 검색
    public List<PostDto> searchByTitle(String keyword) {
        return postRepository.findByTitleContaining(keyword)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Entity -> DTO 변환
    private PostDto convertToDto(Post post) {
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViewCount()
        );
    }
}
