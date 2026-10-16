package com.avengers.cardgame.fakes;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.service.Message;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Фейковый бот для тестов: запоминает отправленные сообщения.
 */
public class FakeBot implements Bot {

    /** Отправленные сообщения с идентификатором чата. */
    private final List<Sent> sent = new ArrayList<>();

    @Override
    public void sendMessage(@NotNull Message msg, long chatId) {
        sent.add(new Sent(msg, chatId));
    }

    /**
     * @return список отправленных сообщений
     */
    public List<Sent> sent() {
        return sent;
    }

    /**
     * @return текст последнего отправленного сообщения
     * @throws AssertionError если ничего не отправлено
     */
    public String lastText() {
        if (sent.isEmpty()) {
            throw new AssertionError("Бот не отправил ни одного сообщения");
        }
        return sent.getLast().message().text();
    }

    /**
     * @return количество отправленных сообщений
     */
    public int count() {
        return sent.size();
    }

    /**
     * Отправленное сообщение с идентификатором чата.
     *
     * @param message сообщение
     * @param chatId  идентификатор чата
     */
    public record Sent(Message message, long chatId) {
    }
}