package com.example.auto;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.CarFilter;
import com.example.auto.CarWayData.CarService;
import com.example.auto.CarWayData.TestDriveService;
import com.example.auto.CarWayData.MaintenanceService;
import com.example.auto.CarWayData.TireService;
import com.example.auto.CarWayData.RentalService;
import com.example.auto.CarWayData.ServiceBookingRegistry;
import com.example.auto.CarWayData.TestDriveBooking;
import com.example.auto.CarWayData.MaintenanceBooking;
import com.example.auto.CarWayData.TireServiceBooking;
import com.example.auto.CarWayData.RentalBooking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Table31FeatureTest {

    private static final Car CAR_2020 = new Car(
            "Skoda Octavia", "Синий", "Седан", "Бензин", 2020, 50_000, 2_000_000, "img");
    private static final Car CAR_2012 = new Car(
            "Honda Accord", "Синий", "Седан", "Бензин", 2012, 120_000, 1_250_000, "img");
    private static final Car CAR_TOYOTA = new Car(
            "Toyota Camry", "Чёрный", "Седан", "Гибрид", 2018, 60_000, 3_000_000, "img");

    @BeforeEach
    void clearRegistry() {
        ServiceBookingRegistry.getInstance().clear();
    }

    @Test
    @DisplayName(" Test drive booking — valid data")
    void t01_testDrive_validBooking() {
        TestDriveService service = new TestDriveService();
        assertTrue(service.canBook("Иван", "+7999", LocalDate.now().plusDays(1)));
        TestDriveBooking booking = service.createBooking(
                CAR_2020, "Иван", "+7999",
                LocalDate.now().plusDays(1), "10:00", "По городу");
        assertEquals("Skoda Octavia", booking.carName());
    }

    @Test
    @DisplayName("Test drive booking — past date rejected")
    void t02_testDrive_pastDateRejected() {
        TestDriveService service = new TestDriveService();
        assertFalse(service.canBook("Иван", "+7999", LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName(" Maintenance booking — service type")
    void t03_maintenance_validBooking() {
        MaintenanceService service = new MaintenanceService();
        assertTrue(service.canBook("Пётр", "+7999", LocalDate.now().plusDays(1)));
        MaintenanceBooking booking = service.createBooking(
                CAR_2020, "Пётр", "+7999", LocalDate.now().plusDays(1), "Полное ТО");
        assertEquals("Полное ТО", booking.serviceType());
        assertEquals(18_900, booking.estimatedPriceRub());
    }

    @Test
    @DisplayName(" Maintenance booking — empty name rejected")
    void t04_maintenance_emptyNameRejected() {
        MaintenanceService service = new MaintenanceService();
        assertFalse(service.canBook("   ", "+7999", LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName(" Car filter — Toyota brand")
    void t05_filter_byToyotaBrand() {
        CarService service = new CarService();
        List<Car> all = service.seedLocalCars();
        CarFilter filter = new CarFilter(
                "Toyota", CarService.ANY, CarService.ANY, CarService.ANY, CarService.ANY,
                null, null, null, null);
        List<Car> result = service.filterCars(all, filter);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> service.extractBrand(c).equalsIgnoreCase("Toyota")));
    }

    @Test
    @DisplayName(" Car filter — empty query shows all cars")
    void t06_filter_emptyQueryShowsAllCars() {
        CarService service = new CarService();
        List<Car> all = service.seedLocalCars();
        CarFilter filter = new CarFilter(
                CarService.ANY, CarService.ANY, CarService.ANY, CarService.ANY, CarService.ANY,
                null, null, null, null);
        List<Car> result = service.filterCars(all, filter);
        assertEquals(all.size(), result.size());
    }

    @Test
    @DisplayName(" Car rental — 2020 car, 5 days")
    void t07_rental_validBooking() {
        RentalService service = new RentalService();
        assertTrue(service.isRentable(CAR_2020));
        assertTrue(service.canBook("Иван", "+7999", 5));
        RentalBooking booking = service.createBooking(CAR_2020, "Иван", "+7999", 5);
        assertEquals(5, booking.days());
        assertEquals(service.totalCostRub(CAR_2020, 5), booking.totalRub());
    }

    @Test
    @DisplayName(" Car rental — 2012 car not rentable")
    void t08_rental_oldCarNotRentable() {
        RentalService service = new RentalService();
        assertFalse(service.isRentable(CAR_2012));
    }

    @Test
    @DisplayName(" Tire service booking — summer season")
    void t09_tireService_validBooking() {
        TireService service = new TireService();
        assertTrue(service.canBook("Иван", "+7999", LocalDate.now().plusDays(2)));
        TireServiceBooking booking = service.createBooking(
                CAR_TOYOTA, "Иван", "+7999", LocalDate.now().plusDays(2), "Лето");
        assertEquals("Лето", booking.season());
        assertEquals(4_500, booking.estimatedPriceRub());
    }

    @Test
    @DisplayName(" Booking registry — empty list then save")
    void t10_registry_emptyThenSave() {
        ServiceBookingRegistry registry = ServiceBookingRegistry.getInstance();
        assertEquals(0, registry.getEntries().size());

        TestDriveService service = new TestDriveService();
        TestDriveBooking booking = service.createBooking(
                CAR_TOYOTA, "Иван", "+7999",
                LocalDate.now().plusDays(1), "13:00", "Трасса");
        registry.addTestDrive(booking);

        assertEquals(1, registry.getEntries().size());
    }
}
