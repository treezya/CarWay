package com.example.auto;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.TestDriveService;
import com.example.auto.CarWayData.ServiceBookingRegistry;
import com.example.auto.CarWayData.TestDriveBooking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceBookingRegistryTest {

    @BeforeEach
    void clear() {
        ServiceBookingRegistry.getInstance().clear();
    }

    @Test
    void addTestDrive_appearsFirstInList() {
        TestDriveService service = new TestDriveService();
        Car car = new Car("BMW 320", "Чёрный", "Седан", "Бензин", 2021, 20_000, 3_500_000, "img");
        TestDriveBooking booking = service.createBooking(
                car, "Иван", "+7999", LocalDate.now().plusDays(1),
                TestDriveService.TIME_SLOTS.get(0), TestDriveService.ROUTES.get(0));

        ServiceBookingRegistry.getInstance().addTestDrive(booking);
        assertEquals(1, ServiceBookingRegistry.getInstance().getEntries().size());
        assertTrue(ServiceBookingRegistry.getInstance().getEntries().get(0).displayLine().contains("Тест-драйв"));
    }
}
