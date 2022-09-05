package main.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class CommentsDTO {
    private Integer id;
    private Long timestamp;
    private String text;
    private UserComDTO user;
}
