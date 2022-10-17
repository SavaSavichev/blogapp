package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import main.model.enums.ModerationStatus;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "posts")
public class Post implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer postId;

    @Column(name = "is_active")
    int isActive;

    @Column(name = "moderation_status")
    @Enumerated(EnumType.STRING)
    ModerationStatus moderationStatus;

    @Column(name = "moderator_id")
    Integer moderatorId;

    @Column(name = "user_id")
    Integer userId;

    Timestamp timestamp;
    String title;

    @Column(columnDefinition = "text")
    String text;

    @Column(name = "view_count")
    private Integer viewCount;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Collection<Comment> postComments;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Collection<Vote> postVotes;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "value = 1")
    private Collection<Vote> postLikes;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "value = -1")
    private Collection<Vote> postDislikes;

    public String getAnnounce() {
        String postText = getText();
        if( postText == null){
            return "";
        }
        String announce = postText.replaceAll("<(.*?)>","" )
                .replaceAll("[\\p{P}\\p{S}]", "");
        announce = announce.substring(0, Math.min(150, announce.length())) + "...";
        return announce;
    }
}