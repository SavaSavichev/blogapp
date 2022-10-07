package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LikeDislikeRequest {
    @JsonProperty("post_id")
    private Integer postId;
}
