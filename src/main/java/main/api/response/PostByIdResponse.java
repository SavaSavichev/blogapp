package main.api.response;

import lombok.Data;
import main.dto.CommentsDTO;
import main.dto.UserDTO;

import java.util.List;

@Data
public class PostByIdResponse {
    private Integer id;
    private long timestamp;
    private boolean active;
    private UserDTO user;
    private String title;
    private String text;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer viewCount;
    private List<CommentsDTO> comments;
    private List<String> tags;
}
