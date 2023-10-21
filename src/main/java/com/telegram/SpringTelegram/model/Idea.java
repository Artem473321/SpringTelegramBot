package com.telegram.SpringTelegram.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;

@Entity(name = "Ideas")
@Data
public class Idea {
    @Id
    @GeneratedValue
    private Long id;

    private String idea;

    private String whoCreated;

    private Timestamp wasCreated;
}
