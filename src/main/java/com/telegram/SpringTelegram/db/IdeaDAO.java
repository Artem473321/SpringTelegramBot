package com.telegram.SpringTelegram.db;

import com.telegram.SpringTelegram.model.Idea;
import com.telegram.SpringTelegram.model.IdeaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Component
@Service
public class IdeaDAO implements DataBaseOperation<Idea>{

    @Autowired
    IdeaRepository ideaRepository;

    public IdeaDAO(){}

    @Override
    public void save(Idea entity) {
        ideaRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        ideaRepository.deleteById(id);
    }

    @Override
    public void delete(Idea entity) {
        ideaRepository.delete(entity);
    }

    @Override
    public Iterable<Idea> findAll() {
        return ideaRepository.findAll();
    }

    @Override
    public Optional<Idea> findById(Long id) {
        return ideaRepository.findById(id);
    }
}
