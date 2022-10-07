package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {
    @JsonProperty("e_mail")
    @Email(message = "Введен некорректный e-mail")
    private String email;
    @Size(min = 6, max = 12, message = "Пароль имеет недопустимую длину")
    private String password;
    @Size(max = 25, message = "Длина имени превышает 25 знаков")
    private String name;
    private String captcha;
    @JsonProperty("captcha_secret")
    private String captchaSecret;
}
