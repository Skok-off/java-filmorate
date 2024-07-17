package ru.yandex.practicum.filmorate.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Constants {
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final Integer MAX_DESCRIPTION_LENGTH = 200;
    public static final Date MIN_RELEASE_DATE;

    static {
        try {
            MIN_RELEASE_DATE = SIMPLE_DATE_FORMAT.parse("1895-12-28");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
