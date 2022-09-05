package main.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class UserDTO {
    private Integer id;
    private String name;
}
