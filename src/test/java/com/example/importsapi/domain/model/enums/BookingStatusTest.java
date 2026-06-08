package com.example.importsapi.domain.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookingStatus – transiciones")
class BookingStatusTest {

    @Nested
    @DisplayName("DRAFT puede transicionar a")
    class FromDraft {

        @Test
        @DisplayName("CONFIRMED → permitido")
        void draft_to_confirmed() {
            assertThat(BookingStatus.DRAFT.canTransitionTo(BookingStatus.CONFIRMED)).isTrue();
        }

        @Test
        @DisplayName("CANCELLED → permitido")
        void draft_to_cancelled() {
            assertThat(BookingStatus.DRAFT.canTransitionTo(BookingStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("DRAFT → no permitido")
        void draft_to_draft() {
            assertThat(BookingStatus.DRAFT.canTransitionTo(BookingStatus.DRAFT)).isFalse();
        }
    }

    @Nested
    @DisplayName("CONFIRMED puede transicionar a")
    class FromConfirmed {

        @Test
        @DisplayName("CANCELLED → permitido")
        void confirmed_to_cancelled() {
            assertThat(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("DRAFT → no permitido")
        void confirmed_to_draft() {
            assertThat(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.DRAFT)).isFalse();
        }

        @Test
        @DisplayName("CONFIRMED → no permitido")
        void confirmed_to_confirmed() {
            assertThat(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.CONFIRMED)).isFalse();
        }
    }

    @Nested
    @DisplayName("CANCELLED es terminal")
    class FromCancelled {

        @Test
        @DisplayName("CONFIRMED → no permitido")
        void cancelled_to_confirmed() {
            assertThat(BookingStatus.CANCELLED.canTransitionTo(BookingStatus.CONFIRMED)).isFalse();
        }

        @Test
        @DisplayName("DRAFT → no permitido")
        void cancelled_to_draft() {
            assertThat(BookingStatus.CANCELLED.canTransitionTo(BookingStatus.DRAFT)).isFalse();
        }

        @Test
        @DisplayName("CANCELLED → no permitido")
        void cancelled_to_cancelled() {
            assertThat(BookingStatus.CANCELLED.canTransitionTo(BookingStatus.CANCELLED)).isFalse();
        }
    }
}
