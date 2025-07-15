package org.example.tgProcessing;

import org.example.telegramBots.TelegramBotLogs;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.example.telegramBots.TelegramBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Создаёт одноразовую ссылку и автоматически одобряет заявку.
 * Жизненный цикл:
 * 1) createLink(chatId) -> возвращает строку-ссылку.
 * 2) Когда кто-то кликнул, Bot-фасад вызывает handleJoinRequest(update).
 */
public class InviteLinkManager {
    public static void main(String[] args) {
    }
    private final TelegramBot bot;

    public InviteLinkManager(TelegramBot bot) {
        this.bot = bot;
    }

    /**
     * Создаёт одноразовую ссылку в группу.
     * @param groupId ID группы (должен быть админом)
     * @return строка вида https://t.me/+AbCdEfGhIjKlMnOp
     */
    public String createLink(Long groupId) throws TelegramApiException {
        CreateChatInviteLink create = CreateChatInviteLink.builder()
                .chatId(groupId)
                .memberLimit(1)          // только ОДНО присоединение
                .expireDate((int) Instant.now().plus(1, ChronoUnit.DAYS).getEpochSecond())
                .createsJoinRequest(true) // обязательно, иначе approve не вызовется
                .build();
        ChatInviteLink link = bot.execute(create);
        return link.getInviteLink();
    }

    /**
     * Вызывайте этот метод из вашего UpdateController-а
     * когда пришёл ChatMemberUpdated с invite_link != null.
     */
    public void handleJoinRequest(ChatMemberUpdated update) throws TelegramApiException {
        // проверяем, что это именно заявка на вступление
        if (update.getInviteLink() == null || update.getNewChatMember() == null) return;

        Long userId = update.getNewChatMember().getUser().getId();
        Long groupId = update.getChat().getId();

        ApproveChatJoinRequest approve = ApproveChatJoinRequest.builder()
                .chatId(groupId)
                .userId(userId)
                .build();
        bot.execute(approve);
    }
}