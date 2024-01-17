package ru.yandex.practicum.filmorate.service.mpa;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MpaServiceImpl implements MpaService{
    private final MpaDao mpaDao;

    @Override
    public Mpa getMpaById(int id) {
        List<Mpa> mpa = mpaDao.getMpaById(id);
        if (mpa.size() == 0) {
            throw new MpaNotFoundException("Рейтинг с id " + id + " не найден.");
        }
        log.info("Mpa с id {} возвращён.", id);
        return mpa.get(0);
    }

    @Override
    public List<Mpa> getAllMpa() {
        List<Mpa> mpas = mpaDao.getAllMpa();
        log.info("Текущее количество mpa: {}. Список возвращён.", mpas.size());
        return mpas;
    }
}
