package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    public Mpa findMpa(Long id) {
        Mpa mpa = mpaDbStorage.findMpa(id);
        if (Objects.isNull(mpa)) {
            throw new NotFoundException("Рейтинг с id = " + id + " не найден.");
        }
        return mpa;
    }

    public Collection<Mpa> findAll() {
        return mpaDbStorage.findAll();
    }

}
