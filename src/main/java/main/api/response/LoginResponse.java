package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import main.dto.UserDTO;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private boolean result;
    private UserDTO user;
}
