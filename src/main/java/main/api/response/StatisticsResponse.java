package main.api.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatisticsResponse {
    private Integer postsCount;
    private Integer likesCount;
    private Integer dislikesCount;
    private Integer viewsCount;
    private Long firstPublication;
}
