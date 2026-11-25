package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.MarketRepository;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Репозиторий рынка в памяти.
 * <p>
 * Составные операции экономики требуют ссылки на репозитории пользователей
 * и карточек, чтобы атомарно изменять лоты, балансы и коллекцию.
 */
public class InMemoryMarketRepository implements MarketRepository {

    /** Хранилище лотов по идентификатору. */
    private final Map<Long, MarketListing> storage = new HashMap<>();

    /** Следующий идентификатор лота. */
    private long nextId = 1;

    /** Репозиторий пользователей для составных операций. */
    private final InMemoryUserRepository userRepository;

    /** Репозиторий карточек для составных операций. */
    private final InMemoryUserCardRepository userCardRepository;

    /**
     * @param userRepository     репозиторий пользователей
     * @param userCardRepository репозиторий карточек
     */
    public InMemoryMarketRepository(InMemoryUserRepository userRepository,
                                    InMemoryUserCardRepository userCardRepository) {
        this.userRepository = userRepository;
        this.userCardRepository = userCardRepository;
    }

    /**
     * Сохраняет новый лот (вспомогательный метод для тестов).
     *
     * @param listing лот для сохранения
     * @return сохранённый лот с присвоенным идентификатором
     */
    public @NotNull MarketListing saveListing(@NotNull MarketListing listing) {
        synchronized (userRepository) {
            long id = listing.id() == 0 ? nextId++ : listing.id();
            MarketListing saved = new MarketListing(id, listing.sellerId(), listing.heroId(),
                    listing.price(), listing.createdAt());
            storage.put(id, saved);
            return saved;
        }
    }

    @Override
    public @NotNull MarketListing createListing(long sellerId, long heroId, int price) throws AppException {
        synchronized (userRepository) {
            Map<Long, User> userSnapshot = userRepository.snapshot();
            Map<Long, Map<Long, Integer>> cardSnapshot = userCardRepository.snapshot();
            Map<Long, MarketListing> marketSnapshot = snapshot();
            long nextIdSnapshot = nextId;
            try {
                if (!userCardRepository.removeCard(sellerId, heroId, 1)) {
                    throw new NotEnoughCardsException(heroId, 1,
                            userCardRepository.countByUserIdAndHeroId(sellerId, heroId));
                }
                return saveListing(new MarketListing(0, sellerId, heroId, price, LocalDateTime.now()));
            } catch (AppException | RuntimeException exception) {
                restore(userRepository, userCardRepository, userSnapshot, cardSnapshot, marketSnapshot,
                        nextIdSnapshot);
                throw exception;
            }
        }
    }

    @Override
    public @NotNull MarketListing buy(long buyerId, long listingId, int sellerAmount) throws AppException {
        synchronized (userRepository) {
            Map<Long, User> userSnapshot = userRepository.snapshot();
            Map<Long, Map<Long, Integer>> cardSnapshot = userCardRepository.snapshot();
            Map<Long, MarketListing> marketSnapshot = snapshot();
            long nextIdSnapshot = nextId;
            try {
                MarketListing deleted = storage.remove(listingId);
                if (deleted == null) {
                    throw new ListingUnavailableException(listingId);
                }
                OptionalInt balance = userRepository.spendBalance(buyerId, deleted.price());
                if (balance.isEmpty()) {
                    OptionalInt available = userRepository.findBalance(buyerId);
                    if (available.isEmpty()) {
                        throw new UserNotFoundException(buyerId);
                    }
                    throw new InsufficientFundsException(deleted.price(), available.getAsInt());
                }
                if (!userRepository.addBalance(deleted.sellerId(), sellerAmount)) {
                    throw new UserNotFoundException(deleted.sellerId());
                }
                userCardRepository.addCard(buyerId, deleted.heroId());
                return deleted;
            } catch (AppException | RuntimeException exception) {
                restore(userRepository, userCardRepository, userSnapshot, cardSnapshot, marketSnapshot,
                        nextIdSnapshot);
                throw exception;
            }
        }
    }

    @Override
    public @NotNull MarketListing cancelListing(long sellerId, long listingId) throws AppException {
        synchronized (userRepository) {
            Map<Long, User> userSnapshot = userRepository.snapshot();
            Map<Long, Map<Long, Integer>> cardSnapshot = userCardRepository.snapshot();
            Map<Long, MarketListing> marketSnapshot = snapshot();
            long nextIdSnapshot = nextId;
            try {
                MarketListing listing = storage.get(listingId);
                if (listing == null || listing.sellerId() != sellerId) {
                    throw new ListingUnavailableException(listingId);
                }
                storage.remove(listingId);
                userCardRepository.addCard(sellerId, listing.heroId());
                return listing;
            } catch (AppException | RuntimeException exception) {
                restore(userRepository, userCardRepository, userSnapshot, cardSnapshot, marketSnapshot,
                        nextIdSnapshot);
                throw exception;
            }
        }
    }

    /**
     * Находит лот по идентификатору.
     *
     * @param listingId идентификатор лота
     * @return лот, если найден
     */
    @Override
    public @NotNull Optional<MarketListing> findById(long listingId) {
        synchronized (userRepository) {
            return Optional.ofNullable(storage.get(listingId));
        }
    }

    @Override
    public @NotNull List<MarketListing> findAllActive() {
        synchronized (userRepository) {
            return sorted(storage.values());
        }
    }

    @Override
    public @NotNull List<MarketListing> findBySeller(long sellerId) {
        synchronized (userRepository) {
            return sorted(storage.values().stream()
                    .filter(listing -> listing.sellerId() == sellerId)
                    .toList());
        }
    }

    /**
     * Сортирует лоты по идентификатору.
     *
     * @param listings лоты
     * @return отсортированный список
     */
    private List<MarketListing> sorted(java.util.Collection<MarketListing> listings) {
        List<MarketListing> result = new ArrayList<>(listings);
        result.sort(Comparator.comparingLong(MarketListing::id));
        return result;
    }

    /**
     * Возвращает снимок хранилища лотов для отката составных операций.
     *
     * @return копия хранилища
     */
    Map<Long, MarketListing> snapshot() {
        synchronized (userRepository) {
            return new HashMap<>(storage);
        }
    }

    /**
     * Восстанавливает все затронутые хранилища из снимков.
     *
     * @param users          репозиторий пользователей
     * @param cards          репозиторий карточек
     * @param userSnapshot   снимок пользователей
     * @param cardSnapshot   снимок карточек
     * @param marketSnapshot снимок лотов
     * @param nextIdSnapshot снимок следующего идентификатора
     */
    private void restore(InMemoryUserRepository users, InMemoryUserCardRepository cards,
                         Map<Long, User> userSnapshot, Map<Long, Map<Long, Integer>> cardSnapshot,
                         Map<Long, MarketListing> marketSnapshot, long nextIdSnapshot) {
        synchronized (userRepository) {
            users.restore(userSnapshot);
            cards.restore(cardSnapshot);
            storage.clear();
            storage.putAll(marketSnapshot);
            nextId = nextIdSnapshot;
        }
    }

    /**
     * @return количество сохранённых лотов
     */
    public int size() {
        synchronized (userRepository) {
            return storage.size();
        }
    }
}
