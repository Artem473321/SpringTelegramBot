package com.telegram.SpringTelegram.service;

import com.telegram.SpringTelegram.config.BotConfig;
import com.telegram.SpringTelegram.model.User;
import com.telegram.SpringTelegram.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    private UserRepository userRepository;
    static final String HELP_TEXT = "This bot created for your ideas";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listCommands = new ArrayList<>();
        listCommands.add(new BotCommand("/start", "Начать работу."));
        listCommands.add(new BotCommand("/mydate", "Посмотреть всю работу."));
        listCommands.add(new BotCommand("/deletedate", "Удалить работу."));
        listCommands.add(new BotCommand("/help", "Информация о командах."));
        listCommands.add(new BotCommand("/settings", "Настройки."));
        try {
            this.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
        }catch (TelegramApiException e){
            log.error("Error: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()){
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (text){
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Unknow command!");
                    break;
            }
        }

    }

    public void registerUser(Message message){
        if (userRepository.findById(message.getChatId()).isEmpty()){
            Long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("User saved");
        }
        else {
            log.info("User is already saved!");
        }
    }
    private void startCommandReceived(long chatId, String name){
        String answer = "Hi, " + name + ", nice to meet you! " + chatId;
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try{
            execute(message);
        }catch (TelegramApiException e){
            log.error("Error: " + e.getMessage());
        }

    }
}
