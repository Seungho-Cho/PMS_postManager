package com.mason.api.post;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PostViewController {

    @GetMapping("/admin/posts")
    public String posts() {
        return "admin/posts";
    }

    @GetMapping("/admin/posts/{id}")
    public String postDetail(@PathVariable Long id, Model model) {
        model.addAttribute("postId", id);
        return "admin/post-detail";
    }
}