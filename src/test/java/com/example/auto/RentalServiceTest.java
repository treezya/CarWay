package com.example.auto;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.RentalService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RentalServiceTest {

    @Test
    void isRentable_checksYearAndMileage() {
        RentalService service = new RentalService();
        Car ok = new Car("Skoda", "Синий", "Седан", "Бензин", 2020, 50_000, 2_000_000, "img");
        Car old = new Car("Honda", "Синий", "Седан", "Бензин", 2012, 50_000, 1_000_000, "img");

        assertTrue(service.isRentable(ok));
        assertFalse(service.isRentable(old));
    }

    @Test
    void totalCostRub_multipliesDailyRateByDays() {
        RentalService service = new RentalService();
        Car car = new Car("VW Tiguan", "Синий", "SUV", "Дизель", 2019, 56_000, 3_300_000, "img");
        long daily = service.dailyRateRub(car);
        assertEquals(daily * 5, service.totalCostRub(car, 5));
    }
}
