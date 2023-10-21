package com.telegram.SpringTelegram.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdeaRepository extends CrudRepository<Idea, Long> {
}
