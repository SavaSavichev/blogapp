package main.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class PostDTO {
    private Integer id;
    private Long timestamp;
    private UserDTO user;
    private String title;
    private String announce;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private Integer viewCount;
}
