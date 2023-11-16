package server.mainproject.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.mainproject.member.entity.Member;
import server.mainproject.member.service.MemberService;
import server.mainproject.post.dto.DevPostDto;
import server.mainproject.post.entity.DevPost;
import server.mainproject.post.repository.DevPostRepository;
import server.mainproject.tag.Post_Tag;
import server.mainproject.tag.Post_TagRepository;
import server.mainproject.tag.Tag;
import server.mainproject.tag.TagRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
@ExtendWith(MockitoExtension.class)
class DevPostServiceTest {
    @InjectMocks
    DevPostService target;
    @Mock
    DevPostRepository devPostRepository;
    @Mock
    Post_TagRepository postTagRepository;
    @Mock
    TagRepository tagRepository;
    @Mock
    MemberService memberService;

    static Member member = Member.builder()
                .memberId(1L).email("abc@gmail.com").password("12345678").build();

    static DevPost dev = DevPost.builder()
                .postId(1L)
                .title("제목")
                .content("내용")
                .star(1)
                .thumbnailImage("image")
                .sorta("sorta")
                .sourceMedia("media")
                .sourceURL("url")
                .name("이름")
                .member(member)
                .build();


    @Nested
    @DisplayName("게시글 저장")
    class SavePostTest {
        @Test
        @DisplayName("정상 저장")
        public void success() throws Exception {
            // given
            DevPostDto.Post post = DevPostDto.Post.builder()
                    .content("내용")
                    .title("제목")
                    .sourceURL("url")
                    .star(1)
                    .sourceMedia("media")
                    .sorta("sorta")
                    .thumbnailImage("image")
                    .memberId(1L)
                    .build();

            given(memberService.verifiedMember(any(Long.TYPE))).willReturn(member);
            given(devPostRepository.save(any(DevPost.class))).willReturn(dev);

            // when & then
            assertDoesNotThrow(() -> target.savedPost(post));
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(dev.getPostId()).isEqualTo(1L);
                softAssertions.assertThat(dev.getTitle()).isEqualTo(post.getTitle());
                softAssertions.assertThat(dev.getContent()).isEqualTo(post.getContent());
            });
        }
    }
}
