package server.mainproject.post.dto;

import lombok.Getter;
import lombok.Setter;
import server.mainproject.member.entity.Member;

import java.util.List;

@Getter @Setter
public class DevPostUpdateDto {
//    private Long postId;
    private Long memberId;
    private String title;
    private String content;
    private int star;
    private String sourceURL;
    private String sourceMedia;
    private String thumbnailImage;
    private List<String> tags;
    private String sorta;

    public Member getMember () {
        Member member = new Member();
        member.setMemberId(memberId);

        return member;
    }

}
