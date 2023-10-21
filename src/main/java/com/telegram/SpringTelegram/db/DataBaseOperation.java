package com.telegram.SpringTelegram.db;

import com.telegram.SpringTelegram.model.Idea;
import com.telegram.SpringTelegram.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface DataBaseOperation<E> {
    void save(E entity);

    void deleteById(Long id);

    void delete(E entity);

    Iterable<E> findAll();

    Optional<E> findById(Long id);
}
