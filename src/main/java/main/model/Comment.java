package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "post_comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer commentId;
    private Integer parent_id;
    @Column(insertable = false, updatable = false)
    private Integer post_id;
    @Column(name = "user_id")
    private Integer userId;
    private Timestamp time;
    private String text;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="post_id")
    public Post post;
}