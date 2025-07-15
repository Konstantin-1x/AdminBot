package org.example.tgProcessing;

import org.example.dao.PeopleDAO;
import org.example.dao.TariffDAO;
import org.example.session.SessionStore;
import org.example.session.TariffCreationSession;
import org.example.table.People;
import org.example.table.Tariff;
import org.example.telegramBots.TelegramBot;
import org.example.tgProcessing.InviteLinkManager;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.math.BigDecimal;
import java.util.Objects;

public class MessageProcessing {

    public void sentPhotoUpdate(Update update){
        Sent createTelegramBot = new Sent();
        long chatId = update.getMessage().getChatId();
        PeopleDAO peopleDAO = new PeopleDAO();
        Integer threadID = update.getMessage().getMessageThreadId();
        if(threadID!=null){
            People people = peopleDAO.findByGroupIdAndIdMessage(chatId,threadID);
            createTelegramBot.sendPhoto(people.getTgId(),null,chatId,update.getMessage().getMessageId());
        }else {
            People people = peopleDAO.findById(chatId);
            createTelegramBot.sendPhoto(people.getGroupID(),people.getId_message(),chatId,update.getMessage().getMessageId());
        }
    }

    public void handleUpdate(Update update) throws TelegramApiException {
        Sent createTelegramBot = new Sent();
        LogicUI logicUI = new LogicUI();

        String msg = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        PeopleDAO peopleDAO = new PeopleDAO();
        Integer threadID = update.getMessage().getMessageThreadId();
        if(threadID!=null){
            People people = peopleDAO.findByGroupIdAndIdMessage(chatId,threadID);
            createTelegramBot.sendMessageFromBot(people.getTgId(),msg);
            return;
        }

        People people = peopleDAO.findById(chatId);

        if(people!=null){
            createTelegramBot.sendMessageUser(people.getGroupID(),people.getId_message(),"Пользователь: " + msg);
        }else {
            logicUI.sendStart(chatId,update);
            return;
        }

        String state = SessionStore.getState(chatId);
//        String state = waitingForInput.get(chatId);

        TariffCreationSession session = SessionStore.getTariffSession(chatId);
//        TariffCreationSession session = newTariffs.get(chatId);
        if(state!= null && !msg.equals("back")){
            if(state.startsWith("awaiting_nameTariff_for")){
                int tariffId = Integer.parseInt(state.split(":")[1]);
                String newName = update.getMessage().getText();

                new TariffDAO().updateNameById(tariffId, newName);
                SessionStore.removeState(chatId);
//                waitingForInput.remove(chatId);

                createTelegramBot.sendMessage(people, "✅ Название тарифа обновлено на: " + newName);
                Tariff tariff = new TariffDAO().findById(tariffId);
                logicUI.sentOneTariff(people,tariff);
                return;
            }
            if(state.startsWith("awaiting_descriptionTariff_for")){
                int tariffId = Integer.parseInt(state.split(":")[1]);
                String newName = update.getMessage().getText();

                new TariffDAO().updateDescriptionById(tariffId, newName);
                SessionStore.removeState(chatId);
//                waitingForInput.remove(chatId);

                createTelegramBot.sendMessage(people, "✅ Описание тарифа обновлено на: " + newName);
                Tariff tariff = new TariffDAO().findById(tariffId);
                logicUI.sentOneTariff(people,tariff);
                return;
            }
            if(state.startsWith("awaiting_priceTariff_for")){
                int tariffId = Integer.parseInt(state.split(":")[1]);
                String newName = update.getMessage().getText().replace(",", ".");;

                try {
                    BigDecimal newPrice = new BigDecimal(newName);
                    new TariffDAO().updatePriceById(tariffId, newPrice);
                    SessionStore.removeState(chatId);
//                    waitingForInput.remove(chatId);

                    createTelegramBot.sendMessage(people,  "✅ Цена тарифа обновлена на: " + newPrice + " ₽");
                    Tariff tariff = new TariffDAO().findById(tariffId);
                    logicUI.sentOneTariff(people,tariff);

                } catch (NumberFormatException e) {
                    createTelegramBot.sendMessage(people,  "⚠️ Пожалуйста, введите корректную цену (например: 199.99).");
                }
                return;
            }
            if(state.startsWith("awaiting_termTariff_for")){
                int tariffId = Integer.parseInt(state.split(":")[1]);
                String newName = update.getMessage().getText();

                try {
                    int newTerm = Integer.parseInt(newName);
                    new TariffDAO().updateTermById(tariffId, newTerm);
                    Tariff tariff = new TariffDAO().findById(tariffId);
                    SessionStore.removeState(chatId);
//                    waitingForInput.remove(chatId);

                    createTelegramBot.sendMessage(people,  "✅ Продолжительность тарифа обновлена на: " + newTerm + " суток");
                    logicUI.sentOneTariff(people,tariff);

                } catch (NumberFormatException e) {
                    createTelegramBot.sendMessage(people,  "⚠️ Пожалуйста, введите корректную продолжительность (например: 14).");
                }
                return;
            }
            if(state.startsWith("awaiting_discountTariff_for")){
                int tariffId = Integer.parseInt(state.split(":")[1]);
                String newName = update.getMessage().getText().replace(",",".");

                try {
                    BigDecimal newDiscount = new BigDecimal(newName);
                    new TariffDAO().updateDiscountById(tariffId, newDiscount);
                    SessionStore.removeState(chatId);
//                    waitingForInput.remove(chatId);

                    createTelegramBot.sendMessage(people,  "✅ Скидка тарифа обновлена на: " + newDiscount + "%");
                    Tariff tariff = new TariffDAO().findById(tariffId);
                    logicUI.sentOneTariff(people,tariff);

                } catch (NumberFormatException e) {
                    createTelegramBot.sendMessage(people,  "⚠️ Пожалуйста, введите корректную скидку (например: 15.2).");
                }
                return;
            }
            if(state.startsWith("addAdmin_")){
                String find = msg.replace("@","");
                People peopleByUsername = peopleDAO.findByUsername(find);
                if(peopleByUsername!=null){
                    if(peopleByUsername.getTgId() == chatId || Objects.equals(peopleByUsername.getUsername(), "mqweco") || Objects.equals(peopleByUsername.getUsername(), "RESTx")){
                        createTelegramBot.sendMessage(people,"Ты дебил?");
                    }else {
                        if(!peopleByUsername.isAdmin()){
                            peopleDAO.updateAdminByTgId(peopleByUsername.getTgId(),true);
                            createTelegramBot.sendMessage(people,"Админ добавлен");
                            createTelegramBot.sendMessage(peopleByUsername,"Вы новый администратор");
                        }else {
                            peopleDAO.updateAdminByTgId(peopleByUsername.getTgId(),false);
                            createTelegramBot.sendMessage(people,"Админ удален");
                            createTelegramBot.sendMessage(peopleByUsername,"Вы разжалованы");
                        }
                    }

                }else {
                    createTelegramBot.sendMessage(people,"Человека с таким именем в БД не найдено");
                }

                SessionStore.removeState(chatId);
//                waitingForInput.remove(chatId);
                return;
            }
        }

        if(session!=null){
            Tariff tariff = session.getTariff();
            switch (session.getStep()){
                case NAME -> {
                    tariff.setName(msg);
                    session.setStep(TariffCreationSession.Step.DESCRIPTION);
                    createTelegramBot.sendMessage(people,"Введите описание");
                }
                case DESCRIPTION -> {
                    tariff.setDescription(msg);
                    session.setStep(TariffCreationSession.Step.PRICE);
                    createTelegramBot.sendMessage(people, "💰 Введите цену (например, 499.99):");
                }
                case PRICE -> {
                    try {
                        BigDecimal price = new BigDecimal(msg.replace(",", "."));
                        tariff.setPrice(price);
                        session.setStep(TariffCreationSession.Step.TERM);
                        createTelegramBot.sendMessage(people,  "📆 Введите срок действия (в сутках):");
                    } catch (NumberFormatException e) {
                        createTelegramBot.sendMessage(people, "⚠️ Введите корректную цену, например: 399.99");
                    }
                }
                case TERM -> {
                    try {
                        int term = Integer.parseInt(msg);
                        tariff.setTerm(term);
                        session.setStep(TariffCreationSession.Step.DISCOUNT);
                        createTelegramBot.sendMessage(people, "🎁 Введите скидку (в рублях, можно 0):");
                    } catch (NumberFormatException e) {
                        createTelegramBot.sendMessage(people,  "⚠️ Введите целое число (например: 7)");
                    }
                }
                case DISCOUNT -> {
                    try {
                        TariffDAO tariffDao = new TariffDAO();
                        BigDecimal discount = new BigDecimal(msg.replace(",", "."));
                        tariff.setDiscount(discount);
                        session.setStep(TariffCreationSession.Step.CONFIRM);
                        // сохраняем тариф
                        tariff.setId(tariffDao.getNextId());
                        new TariffDAO().save(tariff);
                        createTelegramBot.sendMessage(people, "✅ Тариф создан!\n\n" +
                                "📦 Название: " + tariff.getName() + "\n" +
                                "💬 Описание: " + tariff.getDescription() + "\n" +
                                "💰 Цена: " + tariff.getPrice() + " ₽\n" +
                                "📆 Срок: " + tariff.getTerm() + " суток\n" +
                                "🎁 Скидка: " + tariff.getDiscount() + " ₽\n" +
                                "👁 Отображается: ❌");
                        SessionStore.removeTariffSession(chatId);
//                        newTariffs.remove(chatId);
                    } catch (NumberFormatException e) {
                        createTelegramBot.sendMessage(people, "⚠️ Введите корректную скидку (например: 50.00)");
                    }
                }
            }
            return;
        }

        switch (msg) {
            case "/start" -> logicUI.sendStart(chatId,update);
            case "Подписки" -> logicUI.sendSubscription(people);
            case "Меню" -> logicUI.sendMenu(people);
            case "Тарифы" -> logicUI.sendTariff(people);
            case "Отключить рассылку","Включить рассылку" -> {
                boolean checkMarketing = people.isMarketing();
                peopleDAO.updateMarketingByTgId(chatId,!checkMarketing);
                if(checkMarketing){
                    createTelegramBot.sendMessage(people,"Рассылка отключена");
                }else {
                    createTelegramBot.sendMessage(people,"Рассылка включена");
                }
                logicUI.sendMenu(people);
            }

            case "Админ меню" -> logicUI.sendAdminMenu(people);
            case "Изменить тарифы" -> {
                if(people.isAdmin()){
                    logicUI.sendTariff(people);
                }else {
                    logicUI.sendMenu(people);
                }
            }
            case "Админ","Пользователь" ->{
                if(people.isAdmin()){
                    peopleDAO.updateUserByTgId(chatId,!people.isUser_flag());
                    people.setUser_flag(!people.isUser_flag());
                    logicUI.sendAdminMenu(people);
                }else {
                    logicUI.sendMenu(people);
                }
            }

            case "Добавить админа" ->{
                if(people.isAdmin()){
                    SessionStore.setState(chatId,"addAdmin_");
//                    waitingForInput.put(chatId, "addAdmin_");
                    createTelegramBot.sendMessage(people,"Отправьте тег (Например @qwerty123)");
                }
               else {
                    logicUI.sendMenu(people);
                }
            }
            case "Добавить группу" -> {
                if(people.isAdmin()){
                    SessionStore.setState(chatId,"addAdminGroup_");

                    createTelegramBot.sendMessage(people,"Добавьте бота в группу, дайте ему права админа и отправьте команду <b>/addGroup</b>");
                }
            }
            default -> {
                logicUI.sendMenu(people);
            }
        }
    }


    public void callBackQuery(Update update){
        Sent createTelegramBot = new Sent();
        LogicUI logicUI = new LogicUI();
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        PeopleDAO peopleDAO = new PeopleDAO();
        People people = peopleDAO.findById(chatId);

        if(data.startsWith("tariff_")){
            TariffDAO tariffDAO = new TariffDAO();
            int tariffId = Integer.parseInt(data.substring("tariff_".length()));
            Tariff selected = tariffDAO.findById(tariffId);
            logicUI.sentOneTariff(people,selected);
        } else if (data.startsWith("update_tariffs_name_")) {
            int tariffId = Integer.parseInt(data.substring("update_tariffs_name_".length()));

            SessionStore.setState(chatId,"awaiting_nameTariff_for:"+tariffId);
//            waitingForInput.put(chatId,"awaiting_nameTariff_for:"+tariffId);
            createTelegramBot.sendMessage(people,"Введите новое имя для тарифа");
        } else if (data.startsWith("update_tariffs_description_")){
            int tariffId = Integer.parseInt(data.substring("update_tariffs_description_".length()));

            SessionStore.setState(chatId,"awaiting_descriptionTariff_for:"+tariffId);
//            waitingForInput.put(chatId,"awaiting_descriptionTariff_for:"+tariffId);
            createTelegramBot.sendMessage(people,"Введите новое описание для тарифа");
        } else if (data.startsWith("update_tariffs_price_")) {
            int tariffId = Integer.parseInt(data.substring("update_tariffs_price_".length()));

            SessionStore.setState(chatId,"awaiting_priceTariff_for:"+tariffId);
//            waitingForInput.put(chatId,"awaiting_priceTariff_for:"+tariffId);
            createTelegramBot.sendMessage(people,"Введите новую цену для тарифа");
        }else if (data.startsWith("update_tariffs_term_")) {
            int tariffId = Integer.parseInt(data.substring("update_tariffs_term_".length()));

            SessionStore.setState(chatId,"awaiting_termTariff_for:"+tariffId);
//            waitingForInput.put(chatId,"awaiting_termTariff_for:"+tariffId);
            createTelegramBot.sendMessage(people,"Введите продолжительность тарифа");
        }else if (data.startsWith("update_tariffs_discount_")) {
            int tariffId = Integer.parseInt(data.substring("update_tariffs_discount_".length()));

            SessionStore.setState(chatId,"awaiting_discountTariff_for:"+tariffId);
//            waitingForInput.put(chatId,"awaiting_discountTariff_for:"+tariffId);
            createTelegramBot.sendMessage(people,"Введите скидку тарифа");
        }else if (data.startsWith("changeVisible_")) {
            int tariffId = Integer.parseInt(data.substring("changeVisible_".length()));
            TariffDAO tariffDAO = new TariffDAO();
            Tariff tariff = tariffDAO.findById(tariffId);
            boolean visible = tariff.isVisible();
            tariffDAO.updateVisibleById(tariffId,!visible);
            if(visible){
                createTelegramBot.sendMessage(people,"Тариф теперь невидим");
            }else {
                createTelegramBot.sendMessage(people,"Тариф теперь видим");
            }
        } else if (data.startsWith("buy_tariffs_")) {
            int tariffId = Integer.parseInt(data.substring("buy_tariffs_".length()));
            Tariff tariff = new TariffDAO().findById(tariffId);
            InviteLinkManager inviteLinkManager = new InviteLinkManager(TelegramBot.INSTANCE);
            // ID группы, куда надо добавить пользователя
            // (можно хранить в таблице Tariff или отдельной таблице)
            String groupId = "-1002815389123";
            try {
                String link = inviteLinkManager.createLink(Long.valueOf(groupId));
                createTelegramBot.sendMessage(people,
                        "💳 Оплатите тариф «" + tariff.getName() + "» и перейдите по ссылке:\n\n" +
                                "<a href=\"" + link + "\">👉 Вступить в закрытый канал</a>");
            } catch (TelegramApiException e) {
                createTelegramBot.sendMessage(people,
                        "⚠️ Не удалось создать ссылку. Попробуйте позже.");
            }
        }

        switch (data) {
            case "Подписки" -> logicUI.sendSubscription(people);
            case "tariffs" -> logicUI.sendTariff(people);
            case "addTariff_" -> createTariff(chatId);
            default -> {
            }
        }
    }

    public void createTariff(long chatId){
        PeopleDAO peopleDAO = new PeopleDAO();
        People people = peopleDAO.findById(chatId);

        Sent createTelegramBot = new Sent();
        TariffCreationSession tariffCreationSession = new TariffCreationSession();
        Tariff tariff = new Tariff();
        tariff.setVisible(false);
        tariffCreationSession.setStep(TariffCreationSession.Step.NAME);
        tariffCreationSession.setTariff(tariff);
        SessionStore.setTariffSession(chatId,tariffCreationSession);
//        newTariffs.put(chatId,tariffCreationSession);

        createTelegramBot.sendMessage(people,"Введите название тарифа");
    }
}