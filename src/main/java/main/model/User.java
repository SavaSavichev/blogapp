package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import main.model.enums.Role;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.sql.Timestamp;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JoinTable(name = "posts", joinColumns = @JoinColumn(name = "post_id"))
    private Integer userId;

    @Column(name = "is_moderator")
    private Integer isModerator;

    @Column(name = "reg_time")
    @DateTimeFormat(pattern = "YYYY-MM-dd HH:mm:ss")
    private Timestamp regTime;

    private String name;

    @Column(name = "e_mail", unique = true)
    private String email;

    private String password;

    private String code;

    @Nullable
    private String photo;

    public Role getRole(){
        return isModerator == 1 ? Role.MODERATOR : Role.USER;
    }
}