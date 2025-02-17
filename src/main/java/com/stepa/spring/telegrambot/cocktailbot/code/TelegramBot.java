package com.stepa.spring.telegrambot.cocktailbot.code;

import com.stepa.spring.telegrambot.cocktailbot.command.CommandContainer;
import com.stepa.spring.telegrambot.cocktailbot.service.SendBotMessageServiceImpl;
import com.stepa.spring.telegrambot.cocktailbot.service.TelegramUserService;
import com.stepa.spring.telegrambot.cocktailbot.service.DBCocktailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.stepa.spring.telegrambot.cocktailbot.command.CommandName.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private boolean checkOfIngr = false;
    private boolean checkOfName = false;
    private final CommandContainer commandContainer;
    private static String COMMAND_PREFIX = "/";

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText().trim();
            String user = update.getMessage().getFrom().getUserName();
            if (message.startsWith(COMMAND_PREFIX)) {
                checkOfName = false;
                checkOfIngr = false;
                String commandIdentifier = message.split(" ")[0].toLowerCase();
                System.out.println("Обрабатываем команду " + commandIdentifier);
                if(commandIdentifier.equals(INGR.getCommandName())){
                    checkOfIngr=true;
                    commandContainer.retrieveCommand(INGR.getCommandName(),user).execute(update);
                }
                else if (commandIdentifier.equals(NAME.getCommandName())) {
                    checkOfName = true;
                    commandContainer.retrieveCommand(NAME.getCommandName(),user).execute(update);
                } else {
                    commandContainer.retrieveCommand(commandIdentifier,user).execute(update); //обработка команды "/"
                }
            }

            else if(checkOfIngr){//поиск по ингредиенту
                commandContainer.retrieveCommand(SEARCHBYINGR.getCommandName(),user).execute(update);
            }
            else if(checkOfName){//поиску по названию
                commandContainer.retrieveCommand(SEARCHBYNAME.getCommandName(),user).execute(update);
            }
            else {
                commandContainer.retrieveCommand(NO.getCommandName(),user).execute(update);
            }
        }
    }

    public TelegramBot(TelegramUserService telegramUserService, DBCocktailsService dbCocktailsService,
                       @Value("#{'${bot.admins}'.split(',')}") List<String> admins) {

        this.commandContainer = new CommandContainer(new SendBotMessageServiceImpl(this),
                                                    telegramUserService, dbCocktailsService, admins);
    }
}
