package ru.yandex.practicum.filmorate.errors;

public enum ErrorCode {
    //Общие
    ID_IS_NULL("Должен быть указан идентификатор"),
    //Film
    NULL_OR_BLANK_NAME("Название фильма не может быть пустым."),
    LONG_DESCRIPTION("Описание фильма слишком длинное."),
    OLD_RELEASE_DATE("Дата релиза слишком старая."),
    DURATION_NOT_POSITIVE("Длительность фильма должна быть положительной."),
    //User
    DUPLICATE_EMAIL("Email уже используется."),
    NULL_OR_BLANK_EMAIL("Email не указан."),
    NULL_OR_BLANK_LOGIN("Логин не может быть пустым или содержать пробелы."),
    INCORRECT_BIRTHDAY("Некорректная дата рождения.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
