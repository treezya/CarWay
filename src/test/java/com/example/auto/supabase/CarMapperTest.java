package com.example.auto.supabase;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.CarMapper;
import com.example.auto.CarWayData.SupabaseCarRow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CarMapperTest {

    @Test
    void toCar_mapsAllSupportedFields() {
        SupabaseCarRow row = new SupabaseCarRow();
        row.id = "3f87d412-8b5f-4c58-96b5-703389a91f2f";
        row.name = "Toyota Camry";
        row.brand = "Toyota";
        row.color = "Черный";
        row.bodyType = "Седан";
        row.fuelType = "Бензин";
        row.year = 2021;
        row.mileageKm = 35000;
        row.priceRub = 3400000;
        row.imagePath = "cars/camry.webp";

        Car car = CarMapper.toCar(row);

        assertEquals(row.id, car.getId());
        assertEquals(row.name, car.getName());
        assertEquals(row.color, car.getColor());
        assertEquals(row.bodyType, car.getBodyType());
        assertEquals(row.fuelType, car.getFuelType());
        assertEquals(row.year, car.getYear());
        assertEquals(row.mileageKm, car.getMileageKm());
        assertEquals(row.priceRub, Math.round(car.getPrice()));
        assertEquals(row.imagePath, car.getImageUrl());
    }
}
