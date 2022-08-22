package main.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CommentsDTO {
    private Integer id;
    private Long timestamp;
    private String text;
    private UserComDTO user;
}
