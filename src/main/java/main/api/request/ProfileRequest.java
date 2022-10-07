package main.api.request;

import lombok.*;

@Data
public class ProfileRequest {
    private String name;
    private String email;
    private String password;
    private String removePhoto;
}