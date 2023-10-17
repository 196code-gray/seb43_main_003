package server.mainproject.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import server.mainproject.post.dto.DevPostDto;
import server.mainproject.post.dto.DevPostPatchDto;
import server.mainproject.post.entity.DevPost;
import server.mainproject.post.mapper.DevPostMapper;
import server.mainproject.post.repository.DevPostRepository;
import server.mainproject.post.service.DevPostService;
import server.mainproject.response.SingleResponse;
import server.mainproject.utils.URICreator;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// todo : 회원이 탈퇴되도 글은 유지.
@RestController
@RequestMapping("/posts")
@Validated
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DevPostController {
    private final DevPostService service;
    private final DevPostMapper mapper;
    private final DevPostRepository repository;
    @PostMapping
    public ResponseEntity postPost(@RequestBody @Valid DevPostDto.Post post) {

        DevPost create = service.savedPost(post);
        URI uri = URICreator.createUri("/post", create.getPostId());

        return new ResponseEntity(new SingleResponse<>
                (mapper.EntityToResponse(create)), HttpStatus.OK);

    }

    @PatchMapping("/{post-id}/edit")
    public ResponseEntity patchPost(@PathVariable("post-id") @Positive long postId,
                                    @RequestBody @Valid DevPostPatchDto patch) {

        return new ResponseEntity(new SingleResponse<>
                (mapper.EntityToResponse(service.updatePost(patch, postId))), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getAllPosts() {

        List<DevPost> posts = service.findAllPost();

        return new ResponseEntity<>(new SingleResponse<>(mapper.ListResponse(posts)), HttpStatus.OK);
    }

    @GetMapping("/{post-id}")
    public ResponseEntity getPost (@PathVariable("post-id") @Positive long postId) {
        DevPost find = service.findPost(postId);
        return new ResponseEntity(new SingleResponse<>(mapper.EntityToResponse(find)), HttpStatus.OK);
    }
    @GetMapping("/realtime-ranking")
    public ResponseEntity rankingPosts () {

        List<DevPost> posts = service.realtimePost();

        return new ResponseEntity<>(new SingleResponse<>(mapper.mainPageResponse(posts)), HttpStatus.OK);
    }

    @GetMapping("/popular-ranking")  // 추천수 높은 순
    public ResponseEntity getPopular() {
        List<DevPost> response = new ArrayList<>();

        List<DevPost> textPosts = repository.findBySorta("text");
        textPosts.sort(Comparator.comparingInt(DevPost::getRecommend).reversed());
        DevPost textPost = textPosts.get(0);
        response.add(textPost);

        List<DevPost> videoPosts = repository.findBySorta("video");
        videoPosts.sort(Comparator.comparingInt(DevPost::getRecommend).reversed());
        DevPost videoPost = videoPosts.get(0);
        response.add(videoPost);

        List<DevPost> trendPosts = repository.findBySorta("trend");
        trendPosts.sort(Comparator.comparingInt(DevPost::getRecommend).reversed());
        DevPost trendPost = trendPosts.get(0);
        response.add(trendPost);

        return new ResponseEntity<>(new SingleResponse<>(mapper.mainPageResponse(response)), HttpStatus.OK);
    }

    @DeleteMapping("/{post-id}/{member-id}")
    public ResponseEntity deletePost (@PathVariable("post-id") @Positive long postId,
                                      @PathVariable("member-id") @Positive long memberId) {
        service.deletePost(postId,memberId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
    @PostMapping("/{post-id}/recommends/{member-id}")
    public ResponseEntity recommendsPost(@PathVariable("post-id") @Positive long postId,
                                         @PathVariable("member-id") @Positive long memberId) {

        service.saveRecommend(postId, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{post-id}/recommends/{member-id}")
    public ResponseEntity unRecommendsPost(@PathVariable("post-id") @Positive long postId,
                                           @PathVariable("member-id") @Positive long memberId) {

        service.unRecommendPost(postId,memberId);
        return ResponseEntity.ok().build();
    }
}
