package main.api.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class PostRequest {
    private long timestamp;
    private int active;
    @NotBlank(message = "Заголовок должен быть предоставлен")
    @Size(min = 3, message = "Заголовок не может быть короче 3-х символов")
    private String title;
    private List<String> tags;
    @NotBlank(message = "Текст должен быть предоставлен")
    @Size(min = 50, message = "Текст не может быть короче 50-ти символов")
    private String text;
}
