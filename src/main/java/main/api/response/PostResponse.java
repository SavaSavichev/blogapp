package main.api.response;

import lombok.Data;
import main.dto.PostDTO;

import java.util.List;

@Data
public class PostResponse {
    private Integer count;
    private List<PostDTO> posts;
}
