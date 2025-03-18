package api.giybat.uz.controller;

import api.giybat.uz.dto.post.PostCreateDTO;
import api.giybat.uz.dto.post.PostDTO;
import api.giybat.uz.dto.post.PostFilterDTO;
import api.giybat.uz.dto.post.SimilarPostListDTO;
import api.giybat.uz.service.PostService;
import api.giybat.uz.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Tag(name = "PostController", description = "Api set for working with Post")
public class PostController {

    private final PostService postService;

    @PostMapping("")
    @Operation(summary = "Create Post", description = "Api used for post creation")
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostCreateDTO dto) {
        return ResponseEntity.ok(postService.create(dto));
    }

    @GetMapping("/profile")
    @Operation(summary = "Profile Post List", description = "Get all profile post list")
    public ResponseEntity<Page<PostDTO>> profilePostList(@RequestParam(value = "page", defaultValue = "1") int page,
                                                         @RequestParam(value = "size", defaultValue = "12") int size) {
        return ResponseEntity.ok(postService.getProfilePostList(PageUtil.page(page),size));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Get Post By Id", description = "Api returns post By Id")
    public ResponseEntity<PostDTO> byId(@PathVariable("id") String id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Post", description = "Api used for post update")
    public ResponseEntity<PostDTO> update(@PathVariable("id") String id,
                                          @Valid @RequestBody PostCreateDTO dto) {
        return ResponseEntity.ok(postService.update(id,dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Post By Id", description = "Api used for deleting post")
    public ResponseEntity<Boolean> delete(@PathVariable("id") String id) {
        return ResponseEntity.ok(postService.delete(id));
    }

    @PostMapping("/public/filter")
    @Operation(summary = "Post public filter", description = "Api used for post filtering")
    public ResponseEntity<Page<PostDTO>> filter(@Valid @RequestBody PostFilterDTO dto,
                                                @RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.filter(dto, page -1 ,size));
    }

    @PostMapping("/public/similar")
    @Operation(summary = "Get similar post list", description = "Api used for getting similar post list")
    public ResponseEntity<List<PostDTO>> similarPostList(@RequestBody SimilarPostListDTO dto) {
        return ResponseEntity.ok(postService.getSimilarPostList(dto));
    }
}
