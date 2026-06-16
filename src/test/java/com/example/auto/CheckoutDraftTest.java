package com.example.auto;

import com.example.auto.CarWayData.CheckoutDraft;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CheckoutDraftTest {

    @AfterEach
    void cleanUp() {
        CheckoutDraft.clear();
    }

    @Test
    void setAndGet_returnsSameDraft() {
        CheckoutDraft.Draft draft = new CheckoutDraft.Draft(
                "car-id",
                "Иван Иванов",
                "+79990000000",
                "ivan@example.com",
                3_000_000,
                35_000,
                3_035_000
        );

        CheckoutDraft.set(draft);

        assertSame(draft, CheckoutDraft.get());
        assertEquals("car-id", CheckoutDraft.get().carId());
        assertEquals(3_035_000, CheckoutDraft.get().totalRub());
    }

    @Test
    void clear_resetsCurrentDraft() {
        CheckoutDraft.set(new CheckoutDraft.Draft("id", "n", "p", "e", 1, 2, 3));
        CheckoutDraft.clear();

        assertNull(CheckoutDraft.get());
    }
}
