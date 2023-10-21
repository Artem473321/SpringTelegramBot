package com.telegram.SpringTelegram.service;

import com.telegram.SpringTelegram.Status;
import com.telegram.SpringTelegram.config.BotConfig;
import com.telegram.SpringTelegram.db.IdeaDAO;
import com.telegram.SpringTelegram.db.UserDAO;
import com.telegram.SpringTelegram.model.Idea;
import com.telegram.SpringTelegram.model.User;
import lombok.extern.log4j.Log4j;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    static final int[] listOfAdmin = {487855759, 483668639};

    static String status;

    @Autowired
    private IdeaDAO ideaDAO;
    @Autowired
    private UserDAO userDAO;

    private ButtonReaction buttonReaction = new ButtonReaction();

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
        if (status != null) {
            int count = 1;
            Message message = update.getMessage();
            if (status.equals(Status.CREATING_IDEA.name())){
                Idea idea = buttonReaction.saveIdea(message.getText(), message.getChat().getUserName());
                ideaDAO.save(idea);
                sendMessage(message.getChatId(),"You create idea.");
            } else if (status.equals(Status.DELETE_IDEA.name())) {
                String hasDelete = "Idea is not deleted.";
                for (Idea idea: ideaDAO.findAll()) {
                    log.info(count);
                    log.info(message.getText());
                    if (String.valueOf(count).equals(message.getText())){
                        ideaDAO.delete(idea);
                        hasDelete = "Idea is deleted.";
                    }
                    count += 1;
                }
                sendMessage(message.getChatId(),hasDelete);
            }
            status = null;
            sendMessage(message.getChatId(), "Do you want something else?");
        }else if (update.hasMessage() && update.getMessage().hasText()){
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
        } else if (update.hasCallbackQuery()) {
            sendMessage(update.getCallbackQuery().getMessage().getChatId(), buttonReaction.buttonIsClicked(update).toString());
        }
    }

    private InlineKeyboardMarkup creatingButtons() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Создать идею.");
        inlineKeyboardButton1.setCallbackData("Создать идею.");

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Удалить идею.");
        inlineKeyboardButton2.setCallbackData("Удалить идею.");

        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText("Посмотреть идеи.");
        inlineKeyboardButton3.setCallbackData("Посмотреть идеи.");

        rowInline1.add(inlineKeyboardButton1);
        rowInline1.add(inlineKeyboardButton2);
        rowInline1.add(inlineKeyboardButton3);
        rowsInline.add(rowInline1);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public void registerUser(Message message){
        if (userDAO.findById(message.getChatId()).isEmpty()){
            Long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userDAO.save(user);
            log.info("User saved");
        }
        else {
            log.info("User is already saved!");
        }
    }
    private void startCommandReceived(long chatId, String name){
        String answer = "Hi, " + name + ", nice to meet you! ";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        StringBuilder resultText = new StringBuilder();
        message.setChatId(String.valueOf(chatId));
        if (status == null){
            message.setReplyMarkup(creatingButtons());
        }
        if (Objects.equals(status, Status.SHOWING_IDEA.name())){
            int count = 1;
            for (Idea idea: ideaDAO.findAll()) {
                resultText.append(count).append(". ").append(idea.getIdea()).append("\n");
                count += 1;
            }
            status = null;
        }

        message.setText(textToSend + "\n" + resultText);
        try{
            execute(message);
        }catch (TelegramApiException e){
            log.error("Error: " + e.getMessage());
        }

    }
}
