package server.mainproject.post.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import server.mainproject.audit.Auditable;
import server.mainproject.member.entity.Member;

import javax.persistence.*;

@Entity @Getter @Setter
@NoArgsConstructor
public class Recommend extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long recommendId;

    @ManyToOne
    @JoinColumn(name = "POST_ID")
    private DevPost post;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;
}
