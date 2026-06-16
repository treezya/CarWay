package com.example.auto;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.CarFilter;
import com.example.auto.CarWayData.CarService;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarServiceFilterTest {

    @Test
    void filterCars_filtersByBrand() {
        CarService service = new CarService();
        List<Car> all = service.seedLocalCars();
        CarFilter filter = new CarFilter(
                "Toyota", CarService.ANY, CarService.ANY, CarService.ANY, CarService.ANY,
                null, null, null, null
        );
        List<Car> result = service.filterCars(all, filter);
        assertTrue(result.stream().allMatch(c -> service.extractBrand(c).equalsIgnoreCase("Toyota")));
    }

    @Test
    void filterCars_filtersByPriceRange() {
        CarService service = new CarService();
        List<Car> all = service.seedLocalCars();
        CarFilter filter = new CarFilter(
                CarService.ANY, CarService.ANY, CarService.ANY, CarService.ANY, CarService.ANY,
                1_500_000.0, 2_000_000.0, null, null
        );
        List<Car> result = service.filterCars(all, filter);
        assertTrue(result.stream().allMatch(c -> c.getPrice() >= 1_500_000 && c.getPrice() <= 2_000_000));
    }
}
