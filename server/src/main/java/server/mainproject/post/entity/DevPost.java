package server.mainproject.post.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import server.mainproject.comment.entity.Comment;
import server.mainproject.audit.Auditable;
import server.mainproject.member.entity.Member;
import server.mainproject.tag.Post_Tag;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity @Getter @Setter
@NoArgsConstructor
public class DevPost extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private String name;

    @Column
    private String sourceURL;

    @Column
    private int recommend;

    @Column
    private String sourceMedia;

    @Column(nullable = false)
    private int star;

    @Column(columnDefinition = "TEXT")
    private String thumbnailImage;

    @Column
    private Double starAvg;

    @Column
    private String sorta;

    @Transient
    private int score;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Recommend> recommends = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @OneToMany(mappedBy = "devPost", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Post_Tag> postTags = new ArrayList<>();

    public void updateTitle (String title) {
        this.title = title;
    }
    public void updateContent (String content) {
        this.content = content;
    }
    public void updateName (String name) {
        this.name = name;
    }
    public void updateSourceURL (String sourceURL) {
        this.sourceURL = sourceURL;
    }
    public void updateThumbnailImage(String image) {
        this.thumbnailImage = image;
    }
    public void updateSorta(String sorta) {
        this.sorta = sorta;
    }
    public void updateSourceMedia(String media) {
        this.sourceMedia = media;
    }


    @Builder
    public DevPost (String title, String content, String name, String sourceURL,
                    int star, String sourceMedia, String sorta, String thumbnailImage) {
        this.title = title;
        this.content = content;
        this.sourceURL = sourceURL;
        this.star = star;
        this.sourceMedia = sourceMedia;
        this.sorta = sorta;
        this.thumbnailImage = thumbnailImage;
    }

}
