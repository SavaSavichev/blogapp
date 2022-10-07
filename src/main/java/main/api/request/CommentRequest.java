package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class CommentRequest {
    @JsonProperty("parent_id")
    private Integer parentId;
    @JsonProperty("post_id")
    private Integer postId;
    @Size(min = 5, max = 250, message = "Текст комментария не задан или слишком короткий")
    private String text;
}
