package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public final class UserMapper {

    public static User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
            .id(resultSet.getLong("id"))
            .name(resultSet.getString("name"))
            .login(resultSet.getString("login"))
            .email(resultSet.getString("email"))
            .birthday(resultSet.getDate("birthday").toLocalDate())
            .build();
    }

}
