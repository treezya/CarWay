package com.example.auto;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.TireService;
import com.example.auto.CarWayData.TireServiceBooking;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TireServiceTest {

    @Test
    void estimatePriceRub_higherForWinter() {
        TireService service = new TireService();
        assertTrue(service.estimatePriceRub("Зима") > service.estimatePriceRub("Лето"));
    }

    @Test
    void createBooking_includesPrice() {
        TireService service = new TireService();
        Car car = new Car("VW Tiguan", "Синий", "SUV", "Дизель", 2019, 56_000, 3_300_000, "img");

        TireServiceBooking booking = service.createBooking(
                car, "Иван", "+7999", LocalDate.now().plusDays(3), "Зима");
        assertEquals(service.estimatePriceRub("Зима"), booking.estimatedPriceRub());
    }
}
