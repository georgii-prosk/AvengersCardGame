package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.Command;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.config.DatabaseConfig;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.repository.impl.UserRepositoryImpl;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Основной класс Telegram-бота для карточной игры "Avengers".
 * <p>
 * Реализует интерфейс {@link LongPollingSingleThreadUpdateConsumer} и отвечает за
 * обработку входящих обновлений от Telegram API методом Long Polling.
 * Класс инициализирует необходимые зависимости (клиент Telegram, репозиторий пользователей,
 * сервисы) и делегирует выполнение команд зарегистрированным обработчикам через
 * {@link CommandRegistry}.
 * </p>
 *
 * <p>Жизненный цикл обработки обновления:</p>
 * <ol>
 *     <li>Проверка наличия текстового сообщения в обновлении.</li>
 *     <li>Извлечение идентификатора чата, пользователя и текста команды.</li>
 *     <li>Поиск соответствующей команды через {@link CommandRegistry#resolve(String)}.</li>
 *     <li>Выполнение команды с передачей необходимых параметров.</li>
 * </ol>
 *
 * @author Avengers Team
 * @version 1.0
 * @see LongPollingSingleThreadUpdateConsumer
 * @see CommandRegistry
 * @see Command
 */
public class AvengersBot implements LongPollingSingleThreadUpdateConsumer {

    /**
     * Логгер для записи событий и ошибок работы бота.
     */
    private final Logger log = LoggerFactory.getLogger(AvengersBot.class);

    /**
     * Клиент Telegram для отправки сообщений и выполнения запросов к API.
     */
    private final TelegramClient telegramClient;

    /**
     * Реестр команд, содержащий зарегистрированные обработчики и логику их разрешения.
     */
    private final CommandRegistry commandRegistry;

    /**
     * Создаёт новый экземпляр бота с указанным токеном.
     * <p>
     * В конструкторе выполняется инициализация:
     * <ul>
     *     <li>клиента Telegram на основе {@link OkHttpTelegramClient};</li>
     *     <li>конфигурации базы данных {@link DatabaseConfig};</li>
     *     <li>репозитория пользователей {@link UserRepositoryImpl};</li>
     *     <li>сервиса пользователей {@link UserService};</li>
     *     <li>сервиса валидации {@link ValidationService};</li>
     *     <li>реестра команд {@link CommandRegistry}.</li>
     * </ul>
     * </p>
     *
     * @param botToken токен Telegram-бота, полученный от BotFather.
     *                 Не должен быть {@code null} или пустым.
     */
    public AvengersBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);

        DatabaseConfig dbConfig = new DatabaseConfig();
        UserRepository userRepository = new UserRepositoryImpl(dbConfig);
        UserService userService = new UserService(userRepository);
        ValidationService validationService = new ValidationService();

        this.commandRegistry = new CommandRegistry(userService, validationService);
    }

    /**
     * Обрабатывает входящее обновление от Telegram.
     * <p>
     * Метод вызывается при получении нового апдейта. Если обновление не содержит
     * текстового сообщения, оно игнорируется. В противном случае извлекаются
     * идентификатор чата, идентификатор и имя пользователя, а также текст сообщения.
     * На основе текста определяется команда, которая затем выполняется.
     * </p>
     *
     * <p>
     * При возникновении ошибки {@link TelegramApiException} во время выполнения команды
     * сообщение об ошибке записывается в лог, но исключение не пробрасывается дальше,
     * чтобы не прерывать работу бота.
     * </p>
     *
     * @param update объект {@link Update}, содержащий информацию о входящем сообщении
     *               или другом событии от Telegram. Не может быть {@code null}.
     */
    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String text = update.getMessage().getText();

        Command command = commandRegistry.resolve(text);
        try {
            command.execute(telegramClient, chatId, userId, userName, text);
        } catch (TelegramApiException e) {
            log.error("Error while executing the command", e);
        }
    }
}