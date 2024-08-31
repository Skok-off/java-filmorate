package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Film {

    private Long id;
    private String name;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    private Integer duration;
    @NonNull
    private Mpa mpa;
    private List<Genre> genres;
    private List<Director> directors;

}
