package org.example.tgProcessing;

import org.example.telegramBots.TelegramBot;
import org.example.dao.PeopleDAO;
import org.example.dao.TariffDAO;
import org.example.table.People;
import org.example.table.Tariff;
import org.example.telegramBots.TelegramBotLogs;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class LogicUI {

    public void sendStart(long chatId,Update update) {
        Sent sent = new Sent();
        TelegramBot TelegramBot = new TelegramBot();
        TelegramBotLogs telegramBotLogs = new TelegramBotLogs();
        PeopleDAO peopleDAO = new PeopleDAO();

        People people = peopleDAO.findById(chatId);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Моя подписка");
        row1.add("Тарифы");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Меню");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(row1,row2));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        SendMessage sendMessage = new SendMessage();

        sendMessage.setReplyMarkup(keyboardMarkup);
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

            sent.sendMessageStart(people, people.getFirstName() + ", Вас приветствует WB бот подписок", sendMessage);
            List<Long> messageIdAndGroup = telegramBotLogs.createTopic(update);

            people.setGroupID(messageIdAndGroup.get(0));
            people.setId_message(Math.toIntExact(messageIdAndGroup.get(1)));

            peopleDAO.save(people);
        }else{
            sent.sendMessage(people,people.getFirstName() + ", с возвращением!", sendMessage);
        }
    }

    public void sendSubscription(People people) {
        Sent sent = new Sent();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Моя подписка");
        row1.add("Тарифы");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Меню");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(row1,row2));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(keyboardMarkup);

        sent.sendMessage(people,"Выберите действие Подписки",sendMessage);
    }

    public void sendMenu(People people) {
        Sent sent = new Sent();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Количество");
        row1.add("Подписки");

        KeyboardRow row2 = new KeyboardRow();
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

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(keyboardMarkup);

        sent.sendMessage(people,"Выберите действие Меню",sendMessage);
    }
    public void sendAdminMenu(People people){
        Sent sent = new Sent();
        if(people.isAdmin()){
            KeyboardRow row1 = new KeyboardRow();
            row1.add("Добавить админа");

            KeyboardRow row2 = new KeyboardRow();
            row2.add("Добавить группу");
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

            SendMessage sendMessage = new SendMessage();
            sendMessage.setReplyMarkup(keyboardMarkup);

            sent.sendMessage(people,"Админ Меню", sendMessage);
        }else sendMenu(people);
    }
    public void sendTariff(People people) {
        Sent sent = new Sent();
        TariffDAO tariffDAO = new TariffDAO();

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

            BigDecimal discount = tariff.getDiscount().multiply(BigDecimal.valueOf(0.01));
            BigDecimal discountedPrice = tariff.getPrice().subtract(tariff.getPrice().multiply(discount));
            BigDecimal formattedPrice = discountedPrice.setScale(1, RoundingMode.HALF_UP);

            button.setText(tariff.getName() + " - " + formattedPrice + " ₽");

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
        message.setReplyMarkup(markup);
        sent.sendMessage(people,"📦 Выберите тариф:", message);
    }
    public void sentOneTariff(People people, Tariff selected){
        Sent sent = new Sent();
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
        message.setReplyMarkup(markup);
        sent.sendMessage(people,sentText,message);
    }
}
