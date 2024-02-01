package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {

    private final JdbcTemplate jdbcTemplate;

    private FilmDbStorage filmDbStorage;

    private UserDbStorage userDbStorage;

    private ReviewDbStorage reviewDbStorage;

    @BeforeEach
    public void setup() {
        reviewDbStorage = new ReviewDbStorage(jdbcTemplate);
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
        userDbStorage = new UserDbStorage(jdbcTemplate);

        createMockUser("test0@test.com", "user0", "user0");
        createMockUser("test1@test.com", "user1", "user1");
        createMockFilm();
    }

    public void createMockUser(String email, String login, String name) {
        userDbStorage.addUser(
                new User(email, login, name, LocalDate.of(2000, Month.JULY, 20))
        );
    }

    public void createMockFilm() {
        filmDbStorage.addFilm(
                new Film(
                        "film",
                        "film",
                        LocalDate.of(2000, Month.JULY, 20),
                        150,
                        new Mpa(1, "PG")
                )
        );
    }

    @Test
    public void get_review_by_id_normal_case() {
        Review review = new Review(1, "content", true, 1, 1, 0);

        reviewDbStorage.createReview(review);
        Review savedReview = reviewDbStorage.getReviewById(1).get();

        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review);
    }

    @Test
    public void get_all_reviews_by_film_id_normal_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);
        List<Review> reviews = List.of(review1, review2);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        List<Review> savedReviews = reviewDbStorage.getAllReviewsByFilmId(1, 10);

        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(reviews);
    }

    @Test
    public void get_all_reviews_by_film_id_two_reviews_in_db_count_1_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);
        List<Review> reviews = List.of(review1);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        List<Review> savedReviews = reviewDbStorage.getAllReviewsByFilmId(1, 1);

        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(reviews);
    }

    @Test
    public void get_all_reviews_by_film_id_no_reviews_in_db_case() {
        List<Review> savedReviews = reviewDbStorage.getAllReviewsByFilmId(1, 10);
        List<Review> emptyList = List.of();

        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(emptyList);
    }

    @Test
    public void get_all_reviews_by_film_id_film_is_not_found_case() {
        List<Review> savedReviews = reviewDbStorage.getAllReviewsByFilmId(100, 10);
        List<Review> emptyList = List.of();

        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(emptyList);
    }

    @Test
    public void get_all_reviews_by_film_id_after_one_like_to_second_review_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        reviewDbStorage.likeReview(2, 1);
        List<Review> savedReviews = reviewDbStorage.getAllReviewsByFilmId(1, 10);

        review2.setUseful(1);

        List<Review> reviews = List.of(review2, review1);

        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(reviews);
    }

    @Test
    public void get_all_reviews_normal_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);
        List<Review> reviews = List.of(review1, review2);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        List<Review> savedReviews = reviewDbStorage.getAllReviews(10);

        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(reviews);
    }

    @Test
    public void get_all_reviews_two_reviews_in_db_count_1_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);
        List<Review> reviews = List.of(review1);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        List<Review> savedReviews = reviewDbStorage.getAllReviews( 1);

        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(reviews);
    }

    @Test
    public void get_all_reviews_no_reviews_in_db_case() {
        List<Review> savedReviews = reviewDbStorage.getAllReviews(10);
        List<Review> emptyList = List.of();

        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(emptyList);
    }

    @Test
    public void create_review_normal_case() {
        Review review = new Review(1, "content", true, 1, 1, 0);

        reviewDbStorage.createReview(review);
        Review savedReview = reviewDbStorage.getReviewById(1).get();

        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review);
    }

    @Test
    public void update_review_normal_case() {
        Review review = new Review(1, "content", true, 1, 1, 0);

        reviewDbStorage.createReview(review);

        review.setContent("updated");
        review.setIsPositive(false);

        reviewDbStorage.updateReview(review);

        Review savedReview = reviewDbStorage.getReviewById(1).get();

        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review);
    }

    @Test
    public void update_review_trying_to_change_useful_case() {
        Review review = new Review(1, "content", true, 1, 1, 0);

        reviewDbStorage.createReview(review);

        review.setContent("updated");
        review.setIsPositive(false);
        review.setUseful(10);

        reviewDbStorage.updateReview(review);

        Review savedReview = reviewDbStorage.getReviewById(1).get();

        assertThat(savedReview.getUseful())
                .isNotNull()
                .isNotEqualTo(review.getUseful());

    }

    @Test
    public void delete_review_normal_case() {
        Review review = new Review(1, "content", true, 1, 1, 0);

        reviewDbStorage.createReview(review);
        Review savedReview = reviewDbStorage.getReviewById(1).get();

        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review);

        reviewDbStorage.deleteReview(1);

        Optional<Review> deletedReviewOptional = reviewDbStorage.getReviewById(1);

        assertThat(deletedReviewOptional.isEmpty())
                .isNotNull()
                .isEqualTo(true);
    }

    @Test
    public void like_review_normal_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);

        Review savedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(savedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        reviewDbStorage.likeReview(2, 1);

        review2.setUseful(1);

        Review likedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(likedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);
    }

    @Test
    public void like_review_second_time_from_same_user_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        reviewDbStorage.likeReview(2, 1);

        review2.setUseful(1);

        Review likedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(likedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        assertThatThrownBy(() -> reviewDbStorage.likeReview(2, 1))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void dislike_review_normal_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);

        Review savedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(savedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        reviewDbStorage.dislikeReview(2, 1);

        review2.setUseful(-1);

        Review dislikedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(dislikedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);
    }

    @Test
    public void dislike_review_second_time_from_same_user_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        reviewDbStorage.dislikeReview(2, 1);

        review2.setUseful(-1);

        Review dislikedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(dislikedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        assertThatThrownBy(() -> reviewDbStorage.dislikeReview(2, 1))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void remove_like_from_review_normal_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        reviewDbStorage.likeReview(2, 1);

        review2.setUseful(1);

        Review likedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(likedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        reviewDbStorage.removeLikeFromReview(2, 1);
        Review review2WithoutLike = reviewDbStorage.getReviewById(2).get();

        review2.setUseful(0);

        assertThat(review2WithoutLike)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);
    }

    @Test
    public void remove_like_from_review_no_like_was_added_before_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);

        Review savedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(savedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        reviewDbStorage.removeLikeFromReview(2, 1);
        Review savedReview2AfterRemovingLike = reviewDbStorage.getReviewById(2).get();

        assertThat(savedReview2AfterRemovingLike)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);
    }

    @Test
    public void remove_like_should_remove_dislike_as_well_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        reviewDbStorage.dislikeReview(2, 1);

        review2.setUseful(-1);

        Review dislikedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(dislikedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        reviewDbStorage.removeLikeFromReview(2, 1);
        Review review2WithoutDislike = reviewDbStorage.getReviewById(2).get();

        review2.setUseful(0);

        assertThat(review2WithoutDislike)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);
    }

    @Test
    public void remove_dislike_from_review_normal_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);
        reviewDbStorage.dislikeReview(2, 1);

        review2.setUseful(-1);

        Review dislikedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(dislikedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        reviewDbStorage.removeDislikeFromReview(2, 1);
        Review review2WithoutDislike = reviewDbStorage.getReviewById(2).get();

        review2.setUseful(0);

        assertThat(review2WithoutDislike)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);
    }

    @Test
    public void remove_dislike_from_review_no_dislike_was_added_before_case() {
        Review review1 = new Review(1, "content", true, 1, 1, 0);
        Review review2 = new Review(2, "content1", true, 1, 1, 0);

        reviewDbStorage.createReview(review1);
        reviewDbStorage.createReview(review2);

        Review savedReview2 = reviewDbStorage.getReviewById(2).get();

        assertThat(savedReview2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);

        reviewDbStorage.removeDislikeFromReview(2, 1);
        Review savedReview2AfterRemovingDislike = reviewDbStorage.getReviewById(2).get();

        assertThat(savedReview2AfterRemovingDislike)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review2);
    }
}