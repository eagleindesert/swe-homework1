package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;
import com.example.demo.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    
    // 게시글 목록
    @GetMapping
    public String listPosts(@RequestParam(value = "searchType", required = false) String searchType,
                           @RequestParam(value = "keyword", required = false) String keyword,
                           Model model) {
        List<Post> posts;
        
        if (keyword != null && !keyword.isEmpty()) {
            if (null == searchType) {
                posts = postService.getAllPosts();
            } else switch (searchType) {
                case "title":
                    posts = postService.searchByTitle(keyword);
                    break;
                case "author":
                    posts = postService.searchByAuthor(keyword);
                    break;
                default:
                    posts = postService.getAllPosts();
                    break;
            }
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
        } else {
            posts = postService.getAllPosts();
        }
        
        model.addAttribute("posts", posts);
        return "posts/list";
    }
    
    // 게시글 상세
    @GetMapping("/{id}")
    public String viewPost(@PathVariable("id") Long id, Model model) {
        Post post = postService.getPostByIdAndIncrementViews(id);
        model.addAttribute("post", post);
        return "posts/view";
    }
    
    // 게시글 작성 폼
    @GetMapping("/new")
    public String newPostForm(Model model) {
        model.addAttribute("postDto", new PostDto());
        return "posts/form";
    }
    
    // 게시글 저장
    @PostMapping
    public String createPost(@Valid @ModelAttribute("postDto") PostDto postDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        
        Post post = postService.createPost(postDto);
        redirectAttributes.addFlashAttribute("message", "게시글이 작성되었습니다.");
        return "redirect:/posts/" + post.getId();
    }
    
    // 게시글 수정 폼
    @GetMapping("/{id}/edit")
    public String editPostForm(@PathVariable("id") Long id, Model model) {
        Post post = postService.getPostById(id);
        
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setAuthor(post.getAuthor());
        postDto.setContent(post.getContent());
        
        model.addAttribute("postDto", postDto);
        model.addAttribute("isEdit", true);
        return "posts/form";
    }
    
    // 게시글 수정
    @PostMapping("/{id}")
    public String updatePost(@PathVariable("id") Long id,
                            @Valid @ModelAttribute("postDto") PostDto postDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "posts/form";
        }
        
        postService.updatePost(id, postDto);
        redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        return "redirect:/posts/" + id;
    }
    
    // 게시글 삭제
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        postService.deletePost(id);
        redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        return "redirect:/posts";
    }
}
