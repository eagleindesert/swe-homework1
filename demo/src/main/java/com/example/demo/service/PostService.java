package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    
    private final PostRepository postRepository;
    
    // 모든 게시글 조회 (최신순)
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
    
    // 게시글 상세 조회
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id));
    }
    
    // 게시글 상세 조회 및 조회수 증가
    @Transactional
    public Post getPostByIdAndIncrementViews(Long id) {
        Post post = getPostById(id);
        post.incrementViews();
        return post;
    }
    
    // 게시글 저장
    @Transactional
    public Post createPost(PostDto postDto) {
        Post post = new Post(postDto.getTitle(), postDto.getAuthor(), postDto.getContent());
        return postRepository.save(post);
    }
    
    // 게시글 수정
    @Transactional
    public Post updatePost(Long id, PostDto postDto) {
        Post post = getPostById(id);
        post.setTitle(postDto.getTitle());
        post.setAuthor(postDto.getAuthor());
        post.setContent(postDto.getContent());
        return postRepository.save(post);
    }
    
    // 게시글 삭제
    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.delete(post);
    }
    
    // 제목으로 검색
    public List<Post> searchByTitle(String keyword) {
        return postRepository.findByTitleContaining(keyword);
    }
    
    // 작성자로 검색
    public List<Post> searchByAuthor(String keyword) {
        return postRepository.findByAuthorContaining(keyword);
    }
}
