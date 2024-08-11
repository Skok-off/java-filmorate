package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;
    @NotNull
    @NotBlank
    @Email
    private String email;
    private String login;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
}
