package com.example.auto;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.TestDriveService;
import com.example.auto.CarWayData.TestDriveBooking;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDriveServiceTest {

    @Test
    void canBook_acceptsValidData() {
        TestDriveService service = new TestDriveService();
        assertTrue(service.canBook("Иван", "+7999", LocalDate.now().plusDays(1)));
    }

    @Test
    void canBook_rejectsPastDate() {
        TestDriveService service = new TestDriveService();
        assertFalse(service.canBook("Иван", "+7999", LocalDate.now().minusDays(1)));
    }

    @Test
    void createBooking_returnsRecord() {
        TestDriveService service = new TestDriveService();
        Car car = new Car("Toyota Camry", "Чёрный", "Седан", "Гибрид", 2020, 40_000, 3_000_000, "img");

        TestDriveBooking booking = service.createBooking(
                car, "Иван", "+7999", LocalDate.now().plusDays(2), "13:00", "Трасса");
        assertEquals("Трасса", booking.route());
    }
}
