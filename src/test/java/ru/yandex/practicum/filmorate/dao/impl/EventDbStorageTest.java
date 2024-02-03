package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventDbStorageTest {
    private UserDbStorage userDbStorage;
    private FilmDbStorage filmDbStorage;
    private EventDbStorage eventDbStorage;
    private ReviewDbStorage reviewDbStorage;
    private MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private User user1;
    private User user2;
    private User user3;
    private Film film1;
    private Review review1;


    @BeforeEach
    void setUp() {
        userDbStorage = new UserDbStorage(jdbcTemplate);
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
        eventDbStorage = new EventDbStorage(jdbcTemplate);
        reviewDbStorage = new ReviewDbStorage(jdbcTemplate);
        mpaDbStorage = new MpaDbStorage(jdbcTemplate);

        user1 = userDbStorage.addUser(new User("test1@mail.ru", "test1 login",
                "test1 name", LocalDate.of(2000, 11, 11)));
        user2 = userDbStorage.addUser(new User("test2@mail.ru", "test2 login",
                "test2 name", LocalDate.of(2000, 11, 12)));
        user3 = userDbStorage.addUser(new User("test3@mail.ru", "test3 login",
                "test3 name", LocalDate.of(2000, 12, 12)));

        film1 = filmDbStorage.addFilm(new Film("test1 film", "test1 desc",
                LocalDate.of(2004, 11, 11), 125, mpaDbStorage.getMpaById(1).get(0)));
    }


    @Test
    @DisplayName("Получение пустой ленты пользователя 3")
    void shouldGetEmptyFeedOfUser3() {
        List<Event> emptyEvents = eventDbStorage.getUserEvents(user3.getId());

        assertThat(emptyEvents)
                .usingRecursiveComparison()
                .isEqualTo(List.of());
    }

    @Test
    @DisplayName("Получение ленты несуществующего пользователя")
    void shouldNotGetEmptyFeedOfUser999() {
        assertThatThrownBy(() -> eventDbStorage.getUserEvents(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Получение ленты пользователя 1")
    void shouldGetFeedOfUser1() {
        List<Event> emptyEvents = eventDbStorage.getUserEvents(user1.getId());
        assertThat(emptyEvents)
                .usingRecursiveComparison()
                .isEqualTo(List.of());

        eventDbStorage.addEvent(new Event(user1.getId(), "FRIEND", "ADD", user2.getId()));
        eventDbStorage.addEvent(new Event(user1.getId(), "LIKE", "ADD", film1.getId()));

        List<Event> user1Events = eventDbStorage.getUserEvents(user1.getId());

        assertThat(user1Events)
                .isNotNull()
                .isNotEmpty()
                .usingRecursiveComparison();

        assertThat(user1Events.get(0).getEventType())
                .isEqualTo("FRIEND");

        assertThat(user1Events.get(1).getEventType())
                .isEqualTo("LIKE");
    }

    @Test
    @DisplayName("Получение ленты пользователя 2")
    void shouldGetFeedOfUser2() {
        List<Event> emptyEvents = eventDbStorage.getUserEvents(user2.getId());
        assertThat(emptyEvents)
                .usingRecursiveComparison()
                .isEqualTo(List.of());

        review1 = reviewDbStorage.createReview(new Review(1, "Bad review", false, user2.getId(), film1.getId(), 1));
        eventDbStorage.addEvent(new Event(review1.getUserId(), "REVIEW", "ADD", review1.getReviewId()));

        Review updatedReview1 = reviewDbStorage.updateReview(new Review(1, "Good review", true, user2.getId(), film1.getId(), 1));
        eventDbStorage.addEvent(new Event(updatedReview1.getUserId(), "REVIEW", "UPDATE", updatedReview1.getReviewId()));

        List<Event> user2Events = eventDbStorage.getUserEvents(user2.getId());

        assertThat(user2Events)
                .isNotNull()
                .usingRecursiveComparison();

        assertThat(user2Events.get(0).getEventType())
                .isNotNull()
                .isNotBlank()
                .isEqualTo("REVIEW");

        assertThat(user2Events.get(0).getOperation())
                .isNotNull()
                .isNotBlank()
                .isEqualTo("ADD");

        assertThat(user2Events.get(1).getEventType())
                .isNotNull()
                .isNotBlank()
                .isEqualTo("REVIEW");

        assertThat(user2Events.get(1).getOperation())
                .isNotNull()
                .isNotBlank()
                .isEqualTo("UPDATE");
    }
}
