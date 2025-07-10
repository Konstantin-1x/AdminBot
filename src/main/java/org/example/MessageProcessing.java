package org.example;

import org.example.dao.PeopleDAO;
import org.example.dao.TariffDAO;
import org.example.session.TariffCreationSession;
import org.example.table.People;
import org.example.table.Tariff;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageProcessing {
    Map<Long, String> waitingForInput = new HashMap<>();
    Map<Long, TariffCreationSession> newTariffs = new HashMap<>();

    void handleUpdate(Update update) throws TelegramApiException {
        CreateTelegramBot createTelegramBot = new CreateTelegramBot();
        if(update.hasMessage() && update.getMessage().hasText()){
            String msg = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String state = waitingForInput.get(chatId);
            TariffCreationSession session = newTariffs.get(chatId);
            if(state!= null && !msg.equals("back")){
                if(state.startsWith("awaiting_nameTariff_for")){
                    int tariffId = Integer.parseInt(state.split(":")[1]);
                    String newName = update.getMessage().getText();

                    new TariffDAO().updateNameById(tariffId, newName);
                    waitingForInput.remove(chatId);

                    createTelegramBot.sendMessage(chatId, null,"✅ Название тарифа обновлено на: " + newName);
                    Tariff tariff = new TariffDAO().findById(tariffId);
                    createTelegramBot.sentOneTariff(chatId,tariff);
                    return;
                }
                if(state.startsWith("awaiting_descriptionTariff_for")){
                    int tariffId = Integer.parseInt(state.split(":")[1]);
                    String newName = update.getMessage().getText();

                    new TariffDAO().updateDescriptionById(tariffId, newName);
                    waitingForInput.remove(chatId);

                    createTelegramBot.sendMessage(chatId, null,"✅ Описание тарифа обновлено на: " + newName);
                    Tariff tariff = new TariffDAO().findById(tariffId);
                    createTelegramBot.sentOneTariff(chatId,tariff);
                    return;
                }
                if(state.startsWith("awaiting_priceTariff_for")){
                    int tariffId = Integer.parseInt(state.split(":")[1]);
                    String newName = update.getMessage().getText().replace(",", ".");;

                    try {
                        BigDecimal newPrice = new BigDecimal(newName);
                        new TariffDAO().updatePriceById(tariffId, newPrice);

                        waitingForInput.remove(chatId);
                        createTelegramBot.sendMessage(chatId, null, "✅ Цена тарифа обновлена на: " + newPrice + " ₽");
                        Tariff tariff = new TariffDAO().findById(tariffId);
                        createTelegramBot.sentOneTariff(chatId,tariff);

                    } catch (NumberFormatException e) {
                        createTelegramBot.sendMessage(chatId, null, "⚠️ Пожалуйста, введите корректную цену (например: 199.99).");
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

                        waitingForInput.remove(chatId);
                        createTelegramBot.sendMessage(chatId, null, "✅ Продолжительность тарифа обновлена на: " + newTerm + " суток");
                        createTelegramBot.sentOneTariff(chatId,tariff);

                    } catch (NumberFormatException e) {
                        createTelegramBot.sendMessage(chatId, null, "⚠️ Пожалуйста, введите корректную продолжительность (например: 14).");
                    }
                    return;
                }
                if(state.startsWith("awaiting_discountTariff_for")){
                    int tariffId = Integer.parseInt(state.split(":")[1]);
                    String newName = update.getMessage().getText().replace(",",".");

                    try {
                        BigDecimal newDiscount = new BigDecimal(newName);
                        new TariffDAO().updateDiscountById(tariffId, newDiscount);
                        waitingForInput.remove(chatId);
                        createTelegramBot.sendMessage(chatId, null, "✅ Скидка тарифа обновлена на: " + newDiscount + "%");
                        Tariff tariff = new TariffDAO().findById(tariffId);
                        createTelegramBot.sentOneTariff(chatId,tariff);

                    } catch (NumberFormatException e) {
                        createTelegramBot.sendMessage(chatId, null, "⚠️ Пожалуйста, введите корректную скидку (например: 15.2).");
                    }
                    return;
                }
                if(state.startsWith("addAdmin_")){
                    PeopleDAO peopleDAO = new PeopleDAO();
                    String find = msg.replace("@","");
                    People people = peopleDAO.findByUsername(find);
                    if(people!=null){
                        if(people.getTgId() == chatId || Objects.equals(people.getUsername(), "mqweco") || Objects.equals(people.getUsername(), "RESTx")){
                            createTelegramBot.sendMessage(chatId,null,"Ты дебил?");
                        }else {
                            if(!people.isAdmin()){
                                peopleDAO.updateAdminByTgId(people.getTgId(),true);
                                createTelegramBot.sendMessage(chatId,null,"Админ добавлен");
                                createTelegramBot.sendMessage(people.getTgId(),null,"Вы новый администратор");
                            }else {
                                peopleDAO.updateAdminByTgId(people.getTgId(),false);
                                createTelegramBot.sendMessage(chatId,null,"Админ удален");
                                createTelegramBot.sendMessage(people.getTgId(),null,"Вы разжалованы");
                            }
                        }

                    }else {
                        createTelegramBot.sendMessage(chatId,null,"Человека с таким именем в БД не найдено");
                    }
                    waitingForInput.remove(chatId);
                    return;
                }
            }

            if(session!=null){
                Tariff tariff = session.getTariff();
                switch (session.getStep()){
                    case NAME -> {
                        tariff.setName(msg);
                        session.setStep(TariffCreationSession.Step.DESCRIPTION);
                        createTelegramBot.sendMessage(chatId,null,"Введите описание");
                    }
                    case DESCRIPTION -> {
                        tariff.setDescription(msg);
                        session.setStep(TariffCreationSession.Step.PRICE);
                        createTelegramBot.sendMessage(chatId, null, "💰 Введите цену (например, 499.99):");
                    }
                    case PRICE -> {
                        try {
                            BigDecimal price = new BigDecimal(msg.replace(",", "."));
                            tariff.setPrice(price);
                            session.setStep(TariffCreationSession.Step.TERM);
                            createTelegramBot.sendMessage(chatId, null, "📆 Введите срок действия (в сутках):");
                        } catch (NumberFormatException e) {
                            createTelegramBot.sendMessage(chatId, null, "⚠️ Введите корректную цену, например: 399.99");
                        }
                    }
                    case TERM -> {
                        try {
                            int term = Integer.parseInt(msg);
                            tariff.setTerm(term);
                            session.setStep(TariffCreationSession.Step.DISCOUNT);
                            createTelegramBot.sendMessage(chatId, null, "🎁 Введите скидку (в рублях, можно 0):");
                        } catch (NumberFormatException e) {
                            createTelegramBot.sendMessage(chatId, null, "⚠️ Введите целое число (например: 7)");
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
                            createTelegramBot.sendMessage(chatId, null, "✅ Тариф создан!\n\n" +
                                    "📦 Название: " + tariff.getName() + "\n" +
                                    "💬 Описание: " + tariff.getDescription() + "\n" +
                                    "💰 Цена: " + tariff.getPrice() + " ₽\n" +
                                    "📆 Срок: " + tariff.getTerm() + " суток\n" +
                                    "🎁 Скидка: " + tariff.getDiscount() + " ₽\n" +
                                    "👁 Отображается: ❌");
                            newTariffs.remove(chatId);
                        } catch (NumberFormatException e) {
                            createTelegramBot.sendMessage(chatId, null, "⚠️ Введите корректную скидку (например: 50.00)");
                        }
                    }
                }
                return;
            }

            switch (msg) {
                case "/start" -> createTelegramBot.sendStart(chatId,update);
                case "Подписки" -> createTelegramBot.sendSubscription(chatId);
                case "Меню" -> createTelegramBot.sendMenu(chatId);
                case "Тарифы" -> createTelegramBot.sendTariff(chatId);
                case "Отключить рассылку","Включить рассылку" -> {
                    PeopleDAO peopleDAO = new PeopleDAO();
                    People people = peopleDAO.findById(chatId);
                    boolean checkMarketing = people.isMarketing();
                    peopleDAO.updateMarketingByTgId(chatId,!checkMarketing);
                    if(checkMarketing){
                        createTelegramBot.sendMessage(chatId,0,"Рассылка отключена");
                    }else {
                        createTelegramBot.sendMessage(chatId,0,"Рассылка включена");
                    }
                    createTelegramBot.sendMenu(chatId);
                }

                case "Админ меню" -> createTelegramBot.sendAdminMenu(chatId);
                case "Изменить тарифы" -> {
                    Admin admin = new Admin();
                    if(admin.isAdmin(chatId)){
                        createTelegramBot.sendTariff(chatId);
                    }else {
                        createTelegramBot.sendMenu(chatId);
                    }
                }
                case "Админ","Пользователь" ->{
                    PeopleDAO peopleDAO = new PeopleDAO();
                    People people = peopleDAO.findById(chatId);
                    if(people.isAdmin()){
                        peopleDAO.updateUserByTgId(chatId,!people.isUser_flag());
                        createTelegramBot.sendAdminMenu(chatId);
                    }else {
                        createTelegramBot.sendMenu(chatId);
                    }
                }

                case "Добавить админа" ->{
                    PeopleDAO peopleDAO = new PeopleDAO();
                    People people = peopleDAO.findById(chatId);
                    if(people.isAdmin()){
                        waitingForInput.put(chatId, "addAdmin_");
                        createTelegramBot.sendMessage(chatId,null,"Отправьте тег (Например @qwerty123)");
                    }
                   else {
                        createTelegramBot.sendMenu(chatId);
                    }
                }

                default -> {
                    createTelegramBot.sendMenu(chatId);
                }

            }
        }
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            if(data.startsWith("tariff_")){
                TariffDAO tariffDAO = new TariffDAO();
                int tariffId = Integer.parseInt(data.substring("tariff_".length()));
                Tariff selected = tariffDAO.findById(tariffId);
                createTelegramBot.sentOneTariff(chatId,selected);
            } else if (data.startsWith("update_tariffs_name_")) {
                int tariffId = Integer.parseInt(data.substring("update_tariffs_name_".length()));

                waitingForInput.put(chatId,"awaiting_nameTariff_for:"+tariffId);
                createTelegramBot.sendMessage(chatId,null,"Введите новое имя для тарифа");
            } else if (data.startsWith("update_tariffs_description_")){
                int tariffId = Integer.parseInt(data.substring("update_tariffs_description_".length()));

                waitingForInput.put(chatId,"awaiting_descriptionTariff_for:"+tariffId);
                createTelegramBot.sendMessage(chatId,null,"Введите новое описание для тарифа");
            } else if (data.startsWith("update_tariffs_price_")) {
                int tariffId = Integer.parseInt(data.substring("update_tariffs_price_".length()));

                waitingForInput.put(chatId,"awaiting_priceTariff_for:"+tariffId);
                createTelegramBot.sendMessage(chatId,null,"Введите новую цену для тарифа");
            }else if (data.startsWith("update_tariffs_term_")) {
                int tariffId = Integer.parseInt(data.substring("update_tariffs_term_".length()));

                waitingForInput.put(chatId,"awaiting_termTariff_for:"+tariffId);
                createTelegramBot.sendMessage(chatId,null,"Введите продолжительность тарифа");
            }else if (data.startsWith("update_tariffs_discount_")) {
                int tariffId = Integer.parseInt(data.substring("update_tariffs_discount_".length()));

                waitingForInput.put(chatId,"awaiting_discountTariff_for:"+tariffId);
                createTelegramBot.sendMessage(chatId,null,"Введите скидку тарифа");
            }else if (data.startsWith("changeVisible_")) {
                int tariffId = Integer.parseInt(data.substring("changeVisible_".length()));
                TariffDAO tariffDAO = new TariffDAO();
                Tariff tariff = tariffDAO.findById(tariffId);
                boolean visible = tariff.isVisible();
                tariffDAO.updateVisibleById(tariffId,!visible);
                if(visible){
                    createTelegramBot.sendMessage(chatId,null,"Тариф теперь невидим");
                }else {
                    createTelegramBot.sendMessage(chatId,null,"Тариф теперь видим");
                }

            }

            switch (data) {
                case "Подписки" -> createTelegramBot.sendSubscription(chatId);
                case "tariffs" -> createTelegramBot.sendTariff(chatId);
                case "addTariff_" -> createTariff(chatId);
                default -> {
                }
            }
        }
    }

    public void createTariff(long chatId){
        CreateTelegramBot createTelegramBot = new CreateTelegramBot();
        TariffCreationSession tariffCreationSession = new TariffCreationSession();
        Tariff tariff = new Tariff();
        tariff.setVisible(false);
        tariffCreationSession.setStep(TariffCreationSession.Step.NAME);
        tariffCreationSession.setTariff(tariff);
        newTariffs.put(chatId,tariffCreationSession);

        createTelegramBot.sendMessage(chatId,null,"Введите название тарифа");
    }
}