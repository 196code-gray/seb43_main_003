package server.mainproject.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.mainproject.comment.entity.Comment;
import server.mainproject.exception.BusinessLogicException;
import server.mainproject.exception.ExceptionCode;
import server.mainproject.member.entity.Member;
import server.mainproject.member.service.MemberService;
import server.mainproject.post.dto.DevPostDto;
import server.mainproject.post.dto.DevPostPatchDto;
import server.mainproject.post.entity.DevPost;
import server.mainproject.post.entity.Recommend;
import server.mainproject.post.repository.RecommendRepository;
import server.mainproject.post.repository.DevPostRepository;
import server.mainproject.tag.Post_Tag;
import server.mainproject.tag.Post_TagRepository;
import server.mainproject.tag.Tag;
import server.mainproject.tag.TagRepository;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DevPostService {
    private final DevPostRepository repository;
    private final RecommendRepository recommendRepository;
    private final MemberService memberService;
    private final Post_TagRepository ptr;
    private final TagRepository tagRepository;
    public DevPost savedPost(DevPostDto.Post post) {

        Member member = memberService.verifiedMember(post.getMemberId());

        DevPost newPost = post.toEntity();

        newPost.setMember(member);
        newPost.setName(member.getUserName());

        DevPost savePost = repository.save(newPost);

        if (post.getTags() != null) {
            for (String tagName : post.getTags()) {
                Tag tag = tagRepository.findByName(tagName);
                Post_Tag pt = new Post_Tag();
                if (tag == null) {
                    Tag t = new Tag();
                    t.setName(tagName);
                    pt.setTag(t);
                }
                if (pt.getTag() == null) {
                    pt.setTag(tag);
                }
                pt.setPost(savePost);
                ptr.save(pt);
            }
        }

        return savePost;
    }

    // 추천 누름 기능
    public Recommend saveRecommend(long postId, long memberId) {

        Member member = memberService.verifiedMember(memberId);

        DevPost post = existsPost(postId);

        post.getRecommends()
                .stream()
                .filter(id -> id.getMember().getMemberId() == memberId)
                .filter(two -> two.getPost().getPostId() == postId)
                .findAny()
                .ifPresent(e -> {
                    throw new BusinessLogicException(ExceptionCode.ALREADY_LIKES);
                });

        int like = post.getRecommend();
        post.setRecommend(like + 1);

        Recommend recommend = new Recommend();
        recommend.setPost(post);
        recommend.setMember(member);

        return recommendRepository.save(recommend);
    }
    public DevPost updatePost (DevPostPatchDto patch, long postId) {
        Member member = memberService.verifiedMember(patch.getMemberId());

        DevPost find = existsPost(postId);

        verifiedPostMember(find, member.getMemberId());

        Optional.ofNullable(patch.getTitle()).ifPresent(title -> find.setTitle(title));
        Optional.ofNullable(patch.getContent()).ifPresent(content -> find.setContent(content));
        Optional.ofNullable(patch.getSourceURL()).ifPresent(link -> find.setSourceURL(link));
        Optional.ofNullable(patch.getSourceMedia()).ifPresent(source -> find.setSourceMedia(source));
        Optional.ofNullable(patch.getThumbnailImage()).ifPresent(image -> find.setThumbnailImage(image));
        Optional.ofNullable(patch.getSorta()).ifPresent(sorta -> find.setSorta(sorta));

        if (patch.getStar() != 0) {
            find.setStar(patch.getStar());
        }
        find.setName(member.getUserName());

        Post_Tag postTag = new Post_Tag();

        if (patch.getTags() != null) {
            List<Post_Tag> existingTags = find.getPostTags();
            List<String> newTags = patch.getTags();

            existingTags.removeIf(tag -> !newTags.contains(tag.getTag().getName()));

            for (String tagName : newTags) {
                boolean tagExists = existingTags.stream()
                        .anyMatch(tag -> tag.getTag().getName().equals(tagName));

                if (!tagExists) {
                    Tag tag = tagRepository.findByName(tagName);
                    if (tag == null) {
                        tag = new Tag();
                        tag.setName(tagName);
                        postTag.setTag(tag);
                    }
                    postTag.setPost(find);
                    postTag = ptr.save(postTag);
                    existingTags.add(postTag);
                }
            }
        }

        return repository.save(find);
    }
    @Transactional(readOnly = true)
    public DevPost findPost (long postId) {
        DevPost post = existsPost(postId);

        double answersReview = post.getComments()
                .stream()
                .filter(id -> id.getDevPost().getPostId() == postId)
                .map(review -> review.getStar())
                .mapToDouble(avr -> avr)
                .average()
                .orElse(0.0);

        DecimalFormat df = new DecimalFormat("#.##");
        String formattedReview = df.format(answersReview);
        double roundedReview = Double.parseDouble(formattedReview);

        post.setStarAvg(roundedReview);

        return post;
    }
    @Transactional(readOnly = true)
    public List<DevPost> findAllPost () {

       List<DevPost> posts = repository.findAll(Sort.by("postId").descending());

        DecimalFormat df = new DecimalFormat("#.##");

        postAnswerReviewAvg(posts, df);

        return posts;
    }
    @Transactional(readOnly = true)
    public List<DevPost> realtimePost () {

       List<DevPost> posts = repository.findAll(Sort.by("recommend").descending())
               .stream()
               .limit(3)
               .collect(Collectors.toList());

        DecimalFormat df = new DecimalFormat("#.##");

        postAnswerReviewAvg(posts, df);

        return posts;
    }

    @Transactional(readOnly = true)
    public List<DevPost> findPost (List<DevPost> post) {

        List<DevPost> posts = repository.findAll();

        DecimalFormat df = new DecimalFormat("#.##");

        postAnswerReviewAvg(posts, df);

        posts.sort(Comparator.comparingDouble(DevPost::getStarAvg).reversed());

        return posts;
    }

    public void deletePost (long postId, long memberId) {

        DevPost post = existsPost(postId);

        verifiedPostMember(post, memberId);

        repository.delete(post);
    }

    public void unRecommendPost(long postId, long memberId) {

        memberService.verifiedMember(memberId);
        DevPost post = existsPost(postId);

        Optional<Recommend> optional = recommendRepository.findAll()
                .stream()
                        .filter(id -> id.getMember().getMemberId() == memberId)
                                .filter(i -> i.getPost().getPostId() == postId)
                                        .findFirst();
        Recommend recommend = optional.orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_WRITE));

        int num = post.getRecommend();
        post.setRecommend(num -1);
        repository.save(post);

        recommendRepository.delete(recommend);
    }


    public DevPost existsPost (long postId) {
        Optional<DevPost> optional = repository.findById(postId);
        DevPost findId = optional.orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_FOUND));

        return findId;
    }

    public void verifiedPostMember(DevPost post, long memberId) {
        if (post.getMember().getMemberId() != memberId) {
            throw new BusinessLogicException(ExceptionCode.POST_NOT_WRITE);
        }
    }
    private static void postAnswerReviewAvg(List<DevPost> posts, DecimalFormat df) {
        posts.forEach(post -> {
            double reviews = post.getComments()
                    .stream()
                    .filter(answer -> answer.getDevPost().getPostId().equals(post.getPostId()))
                    .mapToDouble(Comment::getStar)
                    .average()
                    .orElse(0.0);

            String format = df.format(reviews);
            double roundedReview = Double.parseDouble(format);

            post.setStarAvg(roundedReview);
        });
    }

    public void postCommentReviewAvg(List<Comment> comments, DecimalFormat df) {
        DevPost post = repository.findById(comments.get(comments.size()-1).getPostId()).get();
        double reviews = comments
                .stream()
                .filter(answer -> answer.getDevPost().getPostId().equals(post.getPostId()))
                .mapToDouble(Comment::getStar)
                .average()
                .orElse(0.0);

        String format = df.format(reviews);
        double roundedReview = Double.parseDouble(format);

        post.setStarAvg(roundedReview);
        repository.save(post);

    }

}
