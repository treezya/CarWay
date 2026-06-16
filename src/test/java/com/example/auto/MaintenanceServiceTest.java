package com.example.auto;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.MaintenanceService;
import com.example.auto.CarWayData.MaintenanceBooking;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaintenanceServiceTest {

    @Test
    void createBooking_storesServiceType() {
        MaintenanceService service = new MaintenanceService();
        Car car = new Car("Skoda Octavia", "Синий", "Седан", "Бензин", 2020, 41_000, 2_950_000, "img");

        MaintenanceBooking booking = service.createBooking(
                car, "Пётр", "+7999", LocalDate.now().plusDays(1), "Полное ТО");
        assertEquals("Полное ТО", booking.serviceType());
        assertEquals(18_900, booking.estimatedPriceRub());
        assertEquals(6_500, service.estimatePriceRub("Замена масла"));
        assertTrue(service.canBook("Пётр", "+7999", LocalDate.now().plusDays(1)));
    }
}
