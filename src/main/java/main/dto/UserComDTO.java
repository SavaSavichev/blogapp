package main.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class UserComDTO {
    private Integer id;
    private String name;
    private String photo;
}
