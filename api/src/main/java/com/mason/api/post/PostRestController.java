package com.mason.api.post;

import java.util.List;

import com.mason.api.post.dto.CaptionPreviewRequest;
import com.mason.api.post.dto.CaptionPreviewResponse;
import com.mason.api.post.dto.PostResponse;
import com.mason.api.post.dto.TagSuggestionRequest;
import com.mason.api.post.dto.TagSuggestionResponse;
import com.mason.api.post.dto.UpdatePostRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostRestController {

    private final PostService postService;
    private final PostCaptionFormatter postCaptionFormatter;
    private final TagSuggestionService tagSuggestionService;

    public PostRestController(
        PostService postService,
        PostCaptionFormatter postCaptionFormatter,
        TagSuggestionService tagSuggestionService
    ) {
        this.postService = postService;
        this.postCaptionFormatter = postCaptionFormatter;
        this.tagSuggestionService = tagSuggestionService;
    }

    @GetMapping
    public List<PostResponse> findAll() {
        return postService.findAll().stream()
            .map(PostResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public PostResponse findById(@PathVariable Long id) {
        return PostResponse.from(postService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody UpdatePostRequest request) {
        postService.updateContent(
            id,
            request.title(),
            request.caption(),
            request.makerName(),
            request.makerInstagramId(),
            request.makerXId(),
            request.status()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preview-caption")
    public CaptionPreviewResponse previewCaption(@Valid @RequestBody CaptionPreviewRequest request) {
        String text = postCaptionFormatter.format(
            request.platform(),
            request.title(),
            request.caption(),
            request.makerName(),
            request.makerInstagramId(),
            request.makerXId()
        );
        return new CaptionPreviewResponse(text);
    }

    @PostMapping("/suggest-tags")
    public TagSuggestionResponse suggestTags(@RequestBody TagSuggestionRequest request) {
        return new TagSuggestionResponse(tagSuggestionService.suggest(request.title(), request.caption()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}