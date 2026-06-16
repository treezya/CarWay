package com.example.auto;

import com.example.auto.CarWayData.Car;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CarTest {

    @Test
    void fullConstructor_setsAllFields() {
        Car car = new Car(
                "car-1",
                "Toyota Camry",
                "Черный",
                "Седан",
                "Гибрид",
                2020,
                45_000,
                3_200_000,
                "cars/camry.webp"
        );

        assertEquals("car-1", car.getId());
        assertEquals("Toyota Camry", car.getName());
        assertEquals("Черный", car.getColor());
        assertEquals("Седан", car.getBodyType());
        assertEquals("Гибрид", car.getFuelType());
        assertEquals(2020, car.getYear());
        assertEquals(45_000, car.getMileageKm());
        assertEquals(3_200_000, car.getPrice());
        assertEquals("cars/camry.webp", car.getImageUrl());
    }

    @Test
    void shortConstructor_setsNullId() {
        Car car = new Car(
                "BMW M5",
                "Оранжевый",
                "Седан",
                "Бензин",
                2021,
                35_000,
                6_000_000,
                "cars/bmw.webp"
        );

        assertNull(car.getId());
        assertEquals("BMW M5", car.getName());
    }
}
