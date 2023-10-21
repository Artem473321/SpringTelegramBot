package com.telegram.SpringTelegram.service;

import com.telegram.SpringTelegram.Status;
import com.telegram.SpringTelegram.db.IdeaDAO;
import com.telegram.SpringTelegram.model.Idea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Timestamp;

@Component
public class ButtonReaction {


    public StringBuilder buttonIsClicked(Update update){
        StringBuilder resultText = new StringBuilder();
        String buttonText = update.getCallbackQuery().getData();

        switch (buttonText){
            case "Создать идею.":
                resultText.append("Напиши свою идею:");
                TelegramBot.status = Status.CREATING_IDEA.name();
                break;
            case "Удалить идею.":
                resultText.append("Какой идею удалить по номеру:\n");
                TelegramBot.status = Status.DELETE_IDEA.name();
                break;
            case "Посмотреть идеи.":
                resultText.append("Вот ваши идеи:\n");
                TelegramBot.status = Status.SHOWING_IDEA.name();
                break;
        }

        return resultText;
    }

    public Idea saveIdea(String text, String name){
        Idea idea = new Idea();

        idea.setIdea(text);
        idea.setWhoCreated(name);
        idea.setWasCreated(new Timestamp(System.currentTimeMillis()));

        return idea;

    }
}
