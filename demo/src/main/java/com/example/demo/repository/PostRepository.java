package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 제목으로 검색
    List<Post> findByTitleContaining(String keyword);
    
    // 작성자로 검색
    List<Post> findByAuthor(String author);
    
    // 최신순 정렬
    List<Post> findAllByOrderByCreatedAtDesc();
}
