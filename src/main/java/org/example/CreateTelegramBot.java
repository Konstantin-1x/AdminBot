package org.example;


import jakarta.persistence.criteria.CriteriaBuilder;
import org.example.dao.PeopleDAO;
import org.example.dao.TariffDAO;
import org.example.table.People;
import org.example.table.Tariff;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.forum.CreateForumTopic;
import org.telegram.telegrambots.meta.api.objects.forum.ForumTopic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateTelegramBot extends TelegramLongPollingBot {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    MessageProcessing messageProcessing = new MessageProcessing();
    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for(Update update : updates){
            executor.submit(()-> {
                try {
                    messageProcessing.handleUpdate(update);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public String getBotUsername() {
        return "Main_WB_bot";
    }
    @Override
    public String getBotToken() {
        return "7806843527:AAEB_XekSPvss0H1cHi1Ub6jO83uiUMaqmA";
    }

    public void sendMessage(long chatId, Integer messageThreadId, String messageText) {
        boolean sent = false;

        while (!sent) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(messageText);
            sendMessage.setParseMode("HTML");

            if (messageThreadId != null) {
                sendMessage.setMessageThreadId(messageThreadId);
            }
            try {
                execute(sendMessage);
                sent = true;
            } catch (TelegramApiException e) {
                System.err.println("❌ Failed to send: " + e.getMessage());

                String message = e.getMessage();

                int retryAfterSeconds = extractRetryAfterSeconds(message);

                if (retryAfterSeconds > 0) {
                    try {
                        Thread.sleep(retryAfterSeconds * 1000L);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                        break;
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        break;
                    }
                }
            }
        }
    }

    private int extractRetryAfterSeconds(String message) {
        if (message != null && message.contains("Too Many Requests")) {
            String[] parts = message.split("retry after");
            if (parts.length > 1) {
                try {
                    return Integer.parseInt(parts[1].trim().split(" ")[0]);
                } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }

    public void createTopic(long chatId, Update update) {
        CreateForumTopic topic = CreateForumTopic.builder()
                .chatId("-1002815389123")
                .name("Чат с " + update.getMessage().getFrom().getUserName())
                .iconColor(0xFFD67E)
                .build();

        try {
            execute(topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStart(long chatId,Update update) {
        PeopleDAO peopleDAO = new PeopleDAO();

        People people = peopleDAO.findById(chatId);

        if(people==null){
            people = new People();
            people.setTgId(chatId);
            people.setMarketing(true);
            people.setActive(true);
            people.setSubscriptionTime("0");
            people.setAdmin(false);
            people.setUsername(update.getMessage().getFrom().getUserName());
            people.setFirstName(update.getMessage().getFrom().getFirstName());
            people.setUser_flag(true);
            peopleDAO.save(people);

            createTopic(chatId,update);
        }

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Моя подписка");
        row1.add("Тарифы");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Меню");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(row1,row2));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);


        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(people.getFirstName() + ", Вас приветствует WB бот подписок");
        message.setParseMode("HTML");
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSubscription(long chatId) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Моя подписка");
        row1.add("Тарифы");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Меню");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(row1,row2));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);


        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите действие Подписки");
        message.setParseMode("HTML");
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMenu(long chatId) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Количество");
        row1.add("Подписки");

        KeyboardRow row2 = new KeyboardRow();
        PeopleDAO peopleDAO = new PeopleDAO();
        People people = peopleDAO.findById(chatId);
        if(people.isMarketing() && (!people.isAdmin() || people.isUser_flag())){
            row2.add("Отключить рассылку");
        }else if(!people.isAdmin() || people.isUser_flag()){
            row2.add("Включить рассылку");
        }
        if(people.isAdmin()){
            row2.add("Админ меню");
        }

        row2.add("Что-то");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(row1,row2));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);


        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите действие Меню");
        message.setParseMode("HTML");
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendAdminMenu(long chatId){
        PeopleDAO peopleDAO = new PeopleDAO();
        People people = peopleDAO.findById(chatId);
        if(people.isAdmin()){
            KeyboardRow row1 = new KeyboardRow();
            row1.add("Добавить админа");

            KeyboardRow row2 = new KeyboardRow();
            row2.add("Создать рассылку");
            if(people.isUser_flag()){
                row2.add("Пользователь");
                row1.add("Тарифы");
            }else {
                row2.add("Админ");
                row1.add("Изменить тарифы");
            }
            row1.add("Меню");
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setKeyboard(List.of(row1,row2));
            keyboardMarkup.setResizeKeyboard(true);
            keyboardMarkup.setOneTimeKeyboard(false);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Админ Меню");
            message.setParseMode("HTML");
            message.setReplyMarkup(keyboardMarkup);
            try {
                execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else sendMenu(chatId);
    }
    public void sendTariff(long chatId) throws TelegramApiException {
        TariffDAO tariffDAO = new TariffDAO();

        PeopleDAO peopleDAO = new PeopleDAO();
        People people = peopleDAO.findById(chatId);
        List<Tariff> tariffs;
        if(people.isAdmin() && people.isUser_flag()){
            tariffs = tariffDAO.findAllVisible();
        } else{
            tariffs = tariffDAO.findAll();
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Tariff tariff : tariffs) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(tariff.getName() + " - " + (tariff.getPrice().subtract(tariff.getPrice().multiply(tariff.getDiscount().multiply(BigDecimal.valueOf(0.01))))) + " ₽");
            button.setCallbackData("tariff_" + tariff.getId());

            rows.add(List.of(button));
        }
        if(people.isAdmin() && !people.isUser_flag()){
            InlineKeyboardButton addTariff = new InlineKeyboardButton("Добавить тариф");
            addTariff.setCallbackData("addTariff_");
            rows.add(List.of(addTariff));
        }


        InlineKeyboardButton back = new InlineKeyboardButton("⬅️ Назад");
        back.setCallbackData("Подписки");

        rows.add(List.of(back));

        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📦 Выберите тариф:");
        message.setReplyMarkup(markup);

        execute(message);
    }
    public void sentOneTariff(long chatId, Tariff selected){
        PeopleDAO peopleDAO = new PeopleDAO();
        People people = peopleDAO.findById(chatId);
        String sentText = "<b> Тариф: " + selected.getName() + "</b>\n" +
                "<i>Описание:</i> " + selected.getDescription() + "\n" +
                "<b>Стоимость</b>: <code>" + (selected.getPrice().subtract(selected.getPrice().multiply(selected.getDiscount().multiply(BigDecimal.valueOf(0.01))))) + "₽</code>\n" +
                "<b>Срок:</b> " + selected.getTerm() + " суток";
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        InlineKeyboardButton back = new InlineKeyboardButton("⬅️ Назад");
        back.setCallbackData("tariffs");

        if(!people.isUser_flag() && people.isAdmin()){

            sentText += "\n<b>Скидка:</b> " + selected.getDiscount() + "%";

            InlineKeyboardButton name = new InlineKeyboardButton("Наименование");
            name.setCallbackData("update_tariffs_name_" + selected.getId());

            InlineKeyboardButton description = new InlineKeyboardButton("Описание");
            description.setCallbackData("update_tariffs_description_" + selected.getId());

            InlineKeyboardButton price = new InlineKeyboardButton("Стоимость");
            price.setCallbackData("update_tariffs_price_" + selected.getId());

            InlineKeyboardButton term = new InlineKeyboardButton("Продолжительность");
            term.setCallbackData("update_tariffs_term_" + selected.getId());

            InlineKeyboardButton discount = new InlineKeyboardButton("Скидка");
            discount.setCallbackData("update_tariffs_discount_" + selected.getId());

            InlineKeyboardButton visible;
            if(selected.isVisible()){
                visible = new InlineKeyboardButton("Видим ✅");
            }else{
                visible = new InlineKeyboardButton("Невидим ️⚠️");
            }
            visible.setCallbackData("changeVisible_" + selected.getId());

            markup.setKeyboard(List.of(
                    List.of(name),
                    List.of(description),
                    List.of(price),
                    List.of(term),
                    List.of(discount),
                    List.of(visible),
                    List.of(back)
            ));
        } else {
            InlineKeyboardButton buy = new InlineKeyboardButton("✅ Купить");
            buy.setCallbackData("buy_tariffs_" + selected.getId());

            markup.setKeyboard(List.of(
                    List.of(buy),
                    List.of(back)
            ));
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sentText);
        message.setParseMode("HTML");
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void deleteMessage(Long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);

        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
