package com.example.auto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

public final class CarWayData {

    private CarWayData() {
    }

    public static class Car {
        private final String id;
        private final String name;
        private final String color;
        private final String bodyType;
        private final String fuelType;
        private final int year;
        private final int mileageKm;
        private final double price;
        private final String imageUrl;

        public Car(String id,
                   String name,
                   String color,
                   String bodyType,
                   String fuelType,
                   int year,
                   int mileageKm,
                   double price,
                   String imageUrl) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.bodyType = bodyType;
            this.fuelType = fuelType;
            this.year = year;
            this.mileageKm = mileageKm;
            this.price = price;
            this.imageUrl = imageUrl;
        }

        public Car(String name,
                   String color,
                   String bodyType,
                   String fuelType,
                   int year,
                   int mileageKm,
                   double price,
                   String imageUrl) {
            this(null, name, color, bodyType, fuelType, year, mileageKm, price, imageUrl);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        public String getBodyType() {
            return bodyType;
        }

        public String getFuelType() {
            return fuelType;
        }

        public int getYear() {
            return year;
        }

        public int getMileageKm() {
            return mileageKm;
        }

        public double getPrice() {
            return price;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    public static record CarFilter(
            String brand,
            String color,
            String bodyType,
            String fuelType,
            String mileageRule,
            Double priceFrom,
            Double priceTo,
            Integer yearFrom,
            Integer yearTo
    ) {
    }

    public static final class CheckoutDraft {
        private static volatile Draft current;
    
        private CheckoutDraft() {
        }
    
        public static void set(Draft draft) {
            current = draft;
        }
    
        public static Draft get() {
            return current;
        }
    
        public static void clear() {
            current = null;
        }
    
        public record Draft(
                String carId,
                String customerName,
                String customerPhone,
                String customerEmail,
                long subtotalRub,
                long feesRub,
                long totalRub
        ) {
        }
    }

    public static final class SelectedCarContext {
    
        private static Car selectedCar;
        private static int initialServiceTab;
        private static String customerName = "";
        private static String customerPhone = "";
    
        private SelectedCarContext() {
        }
    
        public static void set(Car car) {
            set(car, 0, null, null);
        }
    
        public static void set(Car car, int serviceTabIndex) {
            set(car, serviceTabIndex, null, null);
        }
    
        public static void set(Car car, int serviceTabIndex, String name, String phone) {
            selectedCar = car;
            initialServiceTab = Math.max(0, serviceTabIndex);
            customerName = name == null ? "" : name.trim();
            customerPhone = phone == null ? "" : phone.trim();
        }
    
        public static int getInitialServiceTab() {
            return initialServiceTab;
        }
    
        public static void clearInitialServiceTab() {
            initialServiceTab = 0;
        }
    
        public static Car get() {
            return selectedCar;
        }
    
        public static String getCustomerName() {
            return customerName;
        }
    
        public static String getCustomerPhone() {
            return customerPhone;
        }
    
        public static void clear() {
            selectedCar = null;
            customerName = "";
            customerPhone = "";
        }
    
        public static boolean hasCar() {
            return selectedCar != null;
        }
    }

    public static record TestDriveBooking(
            String carName,
            String customerName,
            String customerPhone,
            LocalDate date,
            String timeSlot,
            String route
    ) {
    }

    public static record MaintenanceBooking(
            String carName,
            String customerName,
            String customerPhone,
            LocalDate date,
            String serviceType,
            long estimatedPriceRub
    ) {
    }

    public static record TireServiceBooking(
            String carName,
            String customerName,
            String customerPhone,
            LocalDate date,
            String season,
            long estimatedPriceRub
    ) {
    }

    public static record RentalBooking(
            String carName,
            String customerName,
            String customerPhone,
            int days,
            long dailyRateRub,
            long totalRub
    ) {
    }

    public static record ServiceBookingEntry(
            String typeLabel,
            String summary,
            String status,
            LocalDateTime createdAt
    ) {
        public String displayLine() {
            return typeLabel + " · " + summary + " — " + status;
        }
    }

    public static class CarService {
        public static final String ANY = "Любой";

        public List<Car> seedLocalCars() {
            List<Car> cars = new ArrayList<>();
            cars.add(car("Ford Focus", "Белый", "Хэтчбек", "Бензин", 2018, 74000, 1_850_000));
            cars.add(car("Chevrolet Malibu", "Белый", "Седан", "Бензин", 2019, 63000, 2_400_000));
            cars.add(car("Kia Sportage", "Тёмно-зелёный", "SUV", "Дизель", 2017, 80000, 2_700_000));
            cars.add(car("Skoda Octavia", "Синий", "Седан", "Бензин", 2020, 41000, 2_950_000));
            cars.add(car("Toyota Camry", "Чёрный", "Седан", "Гибрид", 2018, 60000, 3_000_000));
            cars.add(car("Volkswagen Tiguan", "Синий", "SUV", "Дизель", 2019, 56000, 3_300_000));
            cars.add(car("Nissan Qashqai", "Синий", "SUV", "Бензин", 2020, 45000, 3_500_000));
            cars.add(car("Mazda CX-5", "Красный", "SUV", "Бензин", 2021, 33000, 4_100_000));
            cars.add(car("Subaru Outback", "Зелёный", "Универсал", "Бензин", 2021, 31000, 4_300_000));
            cars.add(car("Audi A6", "Серый", "Седан", "Дизель", 2019, 52000, 4_500_000));
            cars.add(car("Honda Accord", "Синий", "Седан", "Бензин", 2012, 120000, 1_250_000));
            cars.add(car("BMW M5", "Оранжевый", "Седан", "Бензин", 2021, 35000, 6_000_000));
            cars.add(car("Mercedes C200", "Красный", "Купе", "Бензин", 2019, 54000, 4_900_000));
            cars.add(car("Volvo XC60", "Синий", "SUV", "Гибрид", 2020, 60000, 5_600_000));
            cars.add(car("Renault Duster", "Белый", "SUV", "Бензин", 2019, 78000, 1_550_000));
            cars.sort(Comparator.comparing(Car::getPrice));
            return cars;
        }

        private static Car car(String name, String color, String body, String fuel,
                               int year, int mileage, double price) {
            return new Car(name, color, body, fuel, year, mileage, price, imagePath(name));
        }

        public static String imagePath(String name) {
            String file = switch (name) {
                case "Mazda CX-5" -> "mazda-cx5";
                default -> name.toLowerCase(Locale.ROOT).replace(' ', '-');
            };
            return "/com/example/auto/images/cars/" + file + ".png";
        }

        public List<Car> filterCars(List<Car> allCars, CarFilter filter) {
            List<Car> filtered = new ArrayList<>();
            for (Car car : allCars) {
                String carBrand = extractBrand(car);

                if (!ANY.equals(filter.brand()) && !carBrand.equalsIgnoreCase(filter.brand())) continue;
                if (!ANY.equals(filter.color()) && !car.getColor().equalsIgnoreCase(filter.color())) continue;
                if (!ANY.equals(filter.bodyType()) && !car.getBodyType().equalsIgnoreCase(filter.bodyType())) continue;
                if (!ANY.equals(filter.fuelType()) && !car.getFuelType().equalsIgnoreCase(filter.fuelType())) continue;

                if (filter.priceFrom() != null && car.getPrice() < filter.priceFrom()) continue;
                if (filter.priceTo() != null && car.getPrice() > filter.priceTo()) continue;
                if (filter.yearFrom() != null && car.getYear() < filter.yearFrom()) continue;
                if (filter.yearTo() != null && car.getYear() > filter.yearTo()) continue;

                if (!matchMileageRule(car.getMileageKm(), filter.mileageRule())) continue;

                filtered.add(car);
            }
            return filtered;
        }

        private boolean matchMileageRule(int mileage, String mileageRule) {
            if (ANY.equals(mileageRule)) {
                return true;
            }
            if ("до 30 000 км".equals(mileageRule)) {
                return mileage <= 30_000;
            }
            if ("до 60 000 км".equals(mileageRule)) {
                return mileage <= 60_000;
            }
            if ("до 100 000 км".equals(mileageRule)) {
                return mileage <= 100_000;
            }
            return mileage > 100_000;
        }

        public List<String> extractSortedValues(List<Car> cars, Function<Car, String> fieldExtractor) {
            Set<String> values = new HashSet<>();
            for (Car c : cars) {
                values.add(fieldExtractor.apply(c));
            }
            List<String> sorted = new ArrayList<>(values);
            sorted.sort(String::compareToIgnoreCase);
            return sorted;
        }

        public String extractBrand(Car car) {
            return car.getName().split(" ")[0];
        }
    }

    public static class OrderService {
        private static final double FEES = 35_000.0;
        private String carId;
        private String customerName;
        private String customerPhone;
        private String customerEmail;
        private long subtotalRub;
        private long feesRub;
        private long totalRub;
        private String cardLast4;
        private String cardHolder;
        private String cardExpiry;

        public double getFees() {
            return FEES;
        }

        public boolean hasValidCustomerData(String customerName, String customerPhone, String customerEmail) {
            return !isBlank(customerName) && !isBlank(customerPhone) && !isBlank(customerEmail);
        }

        public boolean hasValidCardData(String cardNumber, String cardHolder, String cardExpiry, String cardCvv) {
            return !isBlank(cardNumber) && !isBlank(cardHolder) && !isBlank(cardExpiry) && !isBlank(cardCvv);
        }

        public CheckoutDraft.Draft buildDraft(Car selectedCar, String customerName, String customerPhone, String customerEmail) {
            fillOrderFields(selectedCar, customerName, customerPhone, customerEmail);
            return new CheckoutDraft.Draft(
                    carId,
                    this.customerName,
                    this.customerPhone,
                    this.customerEmail,
                    subtotalRub,
                    feesRub,
                    totalRub
            );
        }

        public SupabaseRestClient.CardOrderRequest buildCardOrderRequest(
                Car selectedCar,
                String customerName,
                String customerPhone,
                String customerEmail,
                String cardNumber,
                String cardHolder,
                String cardExpiry
        ) {
            fillOrderFields(selectedCar, customerName, customerPhone, customerEmail);
            this.cardLast4 = last4(cardNumber);
            this.cardHolder = cardHolder.trim();
            this.cardExpiry = cardExpiry.trim();
            return new SupabaseRestClient.CardOrderRequest(
                    carId,
                    this.customerName,
                    this.customerPhone,
                    this.customerEmail,
                    subtotalRub,
                    feesRub,
                    totalRub,
                    cardLast4,
                    this.cardHolder,
                    this.cardExpiry
            );
        }

        private void fillOrderFields(Car selectedCar, String customerName, String customerPhone, String customerEmail) {
            this.carId = selectedCar.getId();
            this.customerName = customerName.trim();
            this.customerPhone = customerPhone.trim();
            this.customerEmail = customerEmail.trim();
            this.subtotalRub = Math.round(selectedCar.getPrice());
            this.feesRub = Math.round(FEES);
            this.totalRub = Math.round(selectedCar.getPrice() + FEES);
        }

        private static boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }

        private static String last4(String cardNumber) {
            String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
            if (digits.length() <= 4) {
                return digits;
            }
            return digits.substring(digits.length() - 4);
        }
    }

    public static class TestDriveService {
        public static final List<String> TIME_SLOTS = List.of("10:00", "13:00", "16:00");
        public static final List<String> ROUTES = List.of("По городу", "Трасса");
    
        public boolean canBook(String name, String phone, LocalDate date) {
            if (name == null || name.trim().isEmpty()) {
                return false;
            }
            if (phone == null || phone.trim().isEmpty()) {
                return false;
            }
            if (date == null) {
                return false;
            }
            if (date.isBefore(LocalDate.now())) {
                return false;
            }
            return true;
        }
        public TestDriveBooking createBooking(Car car, String name, String phone,
                                              LocalDate date, String time, String route) {
            TestDriveBooking booking = new TestDriveBooking(
                    car.getName(),
                    name.trim(),
                    phone.trim(),
                    date,
                    time,
                    route
            );
            return booking;
        }
    }

    public static class MaintenanceService {
    
        public static final List<String> SERVICE_TYPES = List.of(
                "Замена масла",
                "Диагностика",
                "Полное ТО"
        );
    
        public long estimatePriceRub(String serviceType) {
            if ("Замена масла".equals(serviceType)) {
                return 6_500;
            }
            if ("Диагностика".equals(serviceType)) {
                return 3_900;
            }
            if ("Полное ТО".equals(serviceType)) {
                return 18_900;
            }
            return 0;
        }
    
        public boolean canBook(String name, String phone, LocalDate date) {
            if (name == null || name.trim().isEmpty()) {
                return false;
            }
            if (phone == null || phone.trim().isEmpty()) {
                return false;
            }
            if (date == null) {
                return false;
            }
            if (date.isBefore(LocalDate.now())) {
                return false;
            }
            return true;
        }
    
        public MaintenanceBooking createBooking(Car car, String name, String phone,
                                                LocalDate date, String serviceType) {
            long price = estimatePriceRub(serviceType);
            MaintenanceBooking booking = new MaintenanceBooking(
                    car.getName(),
                    name.trim(),
                    phone.trim(),
                    date,
                    serviceType,
                    price
            );
            return booking;
        }
    }

    public static class TireService {
    
        public static final List<String> SEASONS = List.of("Зима", "Лето");
        public static final long BASE_PRICE_RUB = 4_500;
    
        public long estimatePriceRub(String season) {
            if ("Зима".equals(season)) {
                return BASE_PRICE_RUB + 1_500;
            }
            return BASE_PRICE_RUB;
        }
    
        public boolean canBook(String name, String phone, LocalDate date) {
            if (name == null || name.trim().isEmpty()) {
                return false;
            }
            if (phone == null || phone.trim().isEmpty()) {
                return false;
            }
            if (date == null) {
                return false;
            }
            if (date.isBefore(LocalDate.now())) {
                return false;
            }
            return true;
        }
    
        public TireServiceBooking createBooking(Car car, String name, String phone,
                                                LocalDate date, String season) {
            long price = estimatePriceRub(season);
            TireServiceBooking booking = new TireServiceBooking(
                    car.getName(),
                    name.trim(),
                    phone.trim(),
                    date,
                    season,
                    price
            );
            return booking;
        }
    }

    public static class RentalService {
    
        public static final int MIN_DAYS = 1;
        public static final int MAX_DAYS = 30;
        public static final long BASE_DAILY_RATE = 3_500;
    
        public boolean isRentable(Car car) {
            if (car.getYear() < 2018) {
                return false;
            }
            if (car.getMileageKm() > 100_000) {
                return false;
            }
            return true;
        }
    
        public long dailyRateRub(Car car) {
            long rate = BASE_DAILY_RATE;
            if ("SUV".equalsIgnoreCase(car.getBodyType())) {
                rate = 5_500;
            } else if ("Универсал".equalsIgnoreCase(car.getBodyType())) {
                rate = 4_800;
            } else if ("Седан".equalsIgnoreCase(car.getBodyType())) {
                rate = 4_000;
            }
            if (car.getPrice() >= 4_000_000) {
                rate += 1_500;
            }
            return rate;
        }
    
        public long totalCostRub(Car car, int days) {
            return dailyRateRub(car) * normalizeDays(days);
        }
    
        public boolean canBook(String name, String phone, int days) {
            if (name == null || name.trim().isEmpty()) {
                return false;
            }
            if (phone == null || phone.trim().isEmpty()) {
                return false;
            }
            if (days < MIN_DAYS) {
                return false;
            }
            if (days > MAX_DAYS) {
                return false;
            }
            return true;
        }
    
        public RentalBooking createBooking(Car car, String name, String phone, int days) {
            int daysCount = normalizeDays(days);
            long daily = dailyRateRub(car);
            long total = daily * daysCount;
            RentalBooking booking = new RentalBooking(
                    car.getName(),
                    name.trim(),
                    phone.trim(),
                    daysCount,
                    daily,
                    total
            );
            return booking;
        }
    
        private static int normalizeDays(int days) {
            if (days < MIN_DAYS) {
                return MIN_DAYS;
            }
            if (days > MAX_DAYS) {
                return MAX_DAYS;
            }
            return days;
        }
    }

    public static final class ServiceBookingRegistry {
    
        private static final ServiceBookingRegistry INSTANCE = new ServiceBookingRegistry();
        private static final String STATUS_ACCEPTED = "Принято";
        private static final DateTimeFormatter DATE_FMT =
                DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru"));
    
        private final List<ServiceBookingEntry> entries = new ArrayList<>();
    
        private ServiceBookingRegistry() {
        }
    
        public static ServiceBookingRegistry getInstance() {
            return INSTANCE;
        }
    
        public void addTestDrive(TestDriveBooking booking) {
            String details = booking.carName() + ", " + DATE_FMT.format(booking.date()) + " " + booking.timeSlot();
            addEntry("Тест-драйв", details);
        }
    
        public void addMaintenance(MaintenanceBooking booking) {
            String details = booking.carName() + ", " + booking.serviceType();
            addEntry("ТО", details);
        }
    
        public void addTire(TireServiceBooking booking) {
            String details = booking.carName() + ", " + booking.season();
            addEntry("Шиномонтаж", details);
        }
    
        public void addRental(RentalBooking booking) {
            String details = booking.carName() + ", " + booking.days() + " дн.";
            addEntry("Аренда", details);
        }
    
        private void addEntry(String serviceType, String details) {
            entries.add(0, new ServiceBookingEntry(
                    serviceType,
                    details,
                    STATUS_ACCEPTED,
                    LocalDateTime.now()
            ));
        }
    
        public List<ServiceBookingEntry> getEntries() {
            return Collections.unmodifiableList(entries);
        }
    
        public void clear() {
            entries.clear();
        }
    }

    public static final class CarImageLoader {
    
        private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();
    
        private CarImageLoader() {
        }
    
        public static Image load(Class<?> resourceOwner, Car car, int width, int height) {
            if (car == null) {
                return null;
            }
            String path = car.getImageUrl();
            String cacheKey = path + "@" + width + "x" + height;
            return CACHE.computeIfAbsent(cacheKey, key -> loadUncached(resourceOwner, car, path, width, height));
        }
    
        private static Image loadUncached(Class<?> owner, Car car, String path, int width, int height) {
            Image fromClasspath = loadClasspath(owner, path);
            if (fromClasspath != null) {
                return fromClasspath;
            }
            if (path != null && !path.isBlank() && (path.startsWith("http://") || path.startsWith("https://"))) {
                try {
                    Image remote = new Image(path, width * 2, height * 2, true, true, true);
                    if (!remote.isError() && remote.getWidth() > 0) {
                        return remote;
                    }
                } catch (Exception ignored) {
                }
            }
            if (car.getName() != null && !car.getName().isBlank()) {
                Image byName = loadClasspath(owner, CarService.imagePath(car.getName()));
                if (byName != null) {
                    return byName;
                }
            }
            return SwingFXUtils.toFXImage(CarImageRenderer.render(car, width, height), null);
        }
    
        private static Image loadClasspath(Class<?> owner, String path) {
            if (path == null || path.isBlank() || !path.startsWith("/")) {
                return null;
            }
            try (InputStream in = owner.getResourceAsStream(path)) {
                if (in != null) {
                    var buffered = ImageIO.read(in);
                    if (buffered != null) {
                        return SwingFXUtils.toFXImage(buffered, null);
                    }
                }
            } catch (IOException ignored) {
                return null;
            }
            return null;
        }
    
        public static void clearCache() {
            CACHE.clear();
        }
    }

    public static final class CarImageRenderer {
    
        private CarImageRenderer() {
        }
    
        public static BufferedImage render(Car car, int width, int height) {
            return render(car.getName(), car.getColor(), width, height);
        }
    
        public static BufferedImage render(String title, String colorName, int width, int height) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    
            int hash = Math.abs(title.hashCode());
            Color top = new Color(30 + hash % 40, 15 + hash % 25, 55 + hash % 50);
            Color bottom = new Color(12, 8, 22);
            g.setPaint(new GradientPaint(0, 0, top, 0, height, bottom));
            g.fillRect(0, 0, width, height);
    
            Color accent = accentFromColorName(colorName);
            g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
            g.fillOval(width / 4, height / 6, width / 2, height / 2);
    
            drawCarSilhouette(g, width, height, accent);
    
            g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(14, width / 18)));
            g.setColor(new Color(237, 233, 254));
            FontMetrics fm = g.getFontMetrics();
            String line = title.length() > 22 ? title.substring(0, 21) + "…" : title;
            int textX = (width - fm.stringWidth(line)) / 2;
            int textY = height - Math.max(14, height / 14);
            g.drawString(line, textX, textY);
    
            g.dispose();
            return image;
        }
    
        private static void drawCarSilhouette(Graphics2D g, int width, int height, Color accent) {
            double cx = width * 0.5;
            double cy = height * 0.42;
            double bodyW = width * 0.62;
            double bodyH = height * 0.22;
    
            g.setColor(accent.brighter());
            RoundRectangle2D body = new RoundRectangle2D.Double(
                    cx - bodyW / 2, cy - bodyH / 2, bodyW, bodyH, bodyH, bodyH);
            g.fill(body);
    
            g.setColor(accent.brighter().brighter());
            RoundRectangle2D cabin = new RoundRectangle2D.Double(
                    cx - bodyW * 0.22, cy - bodyH * 0.95, bodyW * 0.5, bodyH * 0.75, bodyH * 0.4, bodyH * 0.4);
            g.fill(cabin);
    
            g.setStroke(new BasicStroke((float) Math.max(2, width / 120f)));
            g.setColor(new Color(20, 15, 35));
            double wheelR = bodyH * 0.42;
            g.fill(new Ellipse2D.Double(cx - bodyW * 0.32 - wheelR, cy + bodyH * 0.15 - wheelR, wheelR * 2, wheelR * 2));
            g.fill(new Ellipse2D.Double(cx + bodyW * 0.32 - wheelR, cy + bodyH * 0.15 - wheelR, wheelR * 2, wheelR * 2));
    
            g.setColor(new Color(180, 170, 210));
            g.fill(new Ellipse2D.Double(cx - bodyW * 0.32 - wheelR * 0.55, cy + bodyH * 0.15 - wheelR * 0.55, wheelR * 1.1, wheelR * 1.1));
            g.fill(new Ellipse2D.Double(cx + bodyW * 0.32 - wheelR * 0.55, cy + bodyH * 0.15 - wheelR * 0.55, wheelR * 1.1, wheelR * 1.1));
        }
    
        private static Color accentFromColorName(String colorName) {
            if (colorName == null) {
                return new Color(124, 58, 237);
            }
            String lower = colorName.toLowerCase();
            if (lower.contains("бел") || lower.contains("white")) {
                return new Color(200, 200, 210);
            }
            if (lower.contains("чёрн") || lower.contains("черн") || lower.contains("black")) {
                return new Color(55, 55, 70);
            }
            if (lower.contains("син")) {
                return new Color(59, 130, 246);
            }
            if (lower.contains("красн")) {
                return new Color(220, 60, 80);
            }
            if (lower.contains("зел")) {
                return new Color(34, 150, 90);
            }
            if (lower.contains("оранж")) {
                return new Color(230, 120, 40);
            }
            if (lower.contains("сер")) {
                return new Color(120, 125, 140);
            }
            return new Color(139, 92, 246);
        }
    }

    public static final class SupabaseCarRow {
        public String id;
        public String name;
        public String brand;
        public String color;

        @JsonProperty("body_type")
        public String bodyType;

        @JsonProperty("fuel_type")
        public String fuelType;

        public int year;

        @JsonProperty("mileage_km")
        public int mileageKm;

        @JsonProperty("price_rub")
        public long priceRub;

        @JsonProperty("image_path")
        public String imagePath;
    }

    public static final class SupabaseConfig {
        private final String url;
        private final String anonKey;

        public SupabaseConfig(String url, String anonKey) {
            this.url = Objects.requireNonNull(url);
            this.anonKey = Objects.requireNonNull(anonKey);
        }

        public String getUrl() {
            return url;
        }

        public String getAnonKey() {
            return anonKey;
        }

        public static SupabaseConfig fromEnvOrNull() {
            String url = System.getenv("SUPABASE_URL");
            String anon = System.getenv("SUPABASE_ANON_KEY");
            if (url == null || url.isBlank() || anon == null || anon.isBlank()) {
                return null;
            }
            return new SupabaseConfig(url.trim(), anon.trim());
        }
    }

    public static final class SupabaseRestClient {

        private static final Logger LOG = CarWaySupport.AppLogger.get(SupabaseRestClient.class);
        private final SupabaseConfig config;
        private final HttpClient http;
        private final ObjectMapper mapper;

        public SupabaseRestClient(SupabaseConfig config) {
            this(config, HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build());
        }

        public SupabaseRestClient(SupabaseConfig config, HttpClient httpClient) {
            this.config = config;
            this.http = httpClient;
            this.mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        public List<SupabaseCarRow> fetchCars() throws IOException, InterruptedException {
            String url = config.getUrl().replaceAll("/+$", "") +
                    "/rest/v1/cars" +
                    "?select=id,name,brand,color,body_type,fuel_type,year,mileage_km,price_rub,image_path" +
                    "&order=price_rub.asc";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("apikey", config.getAnonKey())
                    .header("Authorization", "Bearer " + config.getAnonKey())
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                IOException error = new IOException("Supabase REST error " + res.statusCode() + ": " + res.body());
                CarWaySupport.AppLogger.logError(LOG, "Ошибка получения каталога автомобилей", error);
                throw error;
            }
            String data = res.body();
            if (data == null || data.isBlank()) {
                return List.of();
            }
            return mapper.readValue(data,
                    mapper.getTypeFactory().constructCollectionType(List.class, SupabaseCarRow.class));
        }

        public void createOrderWithCard(CardOrderRequest request) throws IOException, InterruptedException {
            postRpc("create_order_with_card", request);
        }

        public void createOrderWithCredit(CreditOrderRequest request) throws IOException, InterruptedException {
            postRpc("create_order_with_credit", request);
        }

        private void postRpc(String method, Object body) throws IOException, InterruptedException {
            String url = config.getUrl().replaceAll("/+$", "") + "/rest/v1/rpc/" + method;
            String payload = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("apikey", config.getAnonKey())
                    .header("Authorization", "Bearer " + config.getAnonKey())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> res = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                IOException error = new IOException("Supabase RPC error " + res.statusCode() + ": " + res.body());
                CarWaySupport.AppLogger.logError(LOG, "Ошибка RPC-запроса: " + method, error);
                throw error;
            }
        }

        public record CardOrderRequest(
                String car_id,
                String customer_name,
                String customer_phone,
                String customer_email,
                long subtotal_rub,
                long fees_rub,
                long total_rub,
                String card_last4,
                String card_holder,
                String card_expiry
        ) {
        }

        public record CreditOrderRequest(
                String car_id,
                String customer_name,
                String customer_phone,
                String customer_email,
                long subtotal_rub,
                long fees_rub,
                long total_rub,
                String borrower_full_name,
                String passport,
                long income_rub,
                String work_place,
                long initial_payment_rub,
                int months
        ) {
        }
    }

    public static final class CarMapper {
        private CarMapper() {}

        public static Car toCar(SupabaseCarRow row) {
            String image = resolveImagePath(row.imagePath, row.name);
            return new Car(
                    row.id,
                    row.name,
                    row.color,
                    row.bodyType,
                    row.fuelType,
                    row.year,
                    row.mileageKm,
                    row.priceRub,
                    image
            );
        }

        private static String resolveImagePath(String imagePath, String carName) {
            if (imagePath == null || imagePath.isBlank()) {
                return CarService.imagePath(carName);
            }
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                return imagePath;
            }
            if (imagePath.startsWith("/com/example/auto/")) {
                return imagePath;
            }
            String file = imagePath.replace('\\', '/');
            int slash = file.lastIndexOf('/');
            if (slash >= 0) {
                file = file.substring(slash + 1);
            }
            return "/com/example/auto/images/cars/" + file;
        }
    }

    public static final class CarDetailsService {

        private final CarService carService = new CarService();
        private final RentalService rentalService = new RentalService();

        public record DetailLine(String caption, String value) {
        }

        public List<DetailLine> buildDetails(Car car) {
            int currentYear = LocalDate.now().getYear();
            int age = Math.max(0, currentYear - car.getYear());
            List<DetailLine> lines = new ArrayList<>();
            lines.add(new DetailLine("Модель", car.getName()));
            lines.add(new DetailLine("Марка", carService.extractBrand(car)));
            lines.add(new DetailLine("Год выпуска", String.valueOf(car.getYear())));
            lines.add(new DetailLine("Возраст", age + " " + pluralYear(age)));
            lines.add(new DetailLine("Пробег", formatKm(car.getMileageKm())));
            lines.add(new DetailLine("Кузов", car.getBodyType()));
            lines.add(new DetailLine("Топливо", car.getFuelType()));
            lines.add(new DetailLine("Цвет", car.getColor()));
            lines.add(new DetailLine("Цена", formatRub(Math.round(car.getPrice())) + " ₽"));
            lines.add(new DetailLine("Аренда", rentalService.isRentable(car) ? "Доступна" : "Недоступна"));
            if (car.getId() != null && !car.getId().isBlank()) {
                lines.add(new DetailLine("ID", car.getId()));
            }
            return lines;
        }

        private static String pluralYear(int n) {
            int mod10 = n % 10;
            int mod100 = n % 100;
            if (mod100 >= 11 && mod100 <= 14) {
                return "лет";
            }
            if (mod10 == 1) {
                return "год";
            }
            if (mod10 >= 2 && mod10 <= 4) {
                return "года";
            }
            return "лет";
        }

        private static String formatKm(long km) {
            return NumberFormat.getIntegerInstance(new Locale("ru", "RU")).format(km) + " км";
        }

        private static String formatRub(long value) {
            return NumberFormat.getIntegerInstance(new Locale("ru", "RU")).format(value);
        }
    }

    public static final class CreditCalculatorService {

        public static final double ANNUAL_RATE_PERCENT = 12.0;

        public record Quote(
                long carPriceRub,
                long initialPaymentRub,
                int months,
                long loanAmountRub,
                long monthlyPaymentRub,
                long totalPaidRub,
                long overpaymentRub
        ) {
        }

        public Quote calculate(long carPriceRub, long initialPaymentRub, int months) {
            long price = Math.max(0, carPriceRub);
            long initial = Math.max(0, initialPaymentRub);
            int term = Math.max(1, months);
            long loan = Math.max(0, price - initial);
            if (loan == 0) {
                return new Quote(price, initial, term, 0, 0, 0, 0);
            }
            double monthlyRate = ANNUAL_RATE_PERCENT / 100.0 / 12.0;
            double factor = Math.pow(1 + monthlyRate, term);
            double payment = loan * monthlyRate * factor / (factor - 1);
            long monthly = Math.round(payment);
            long totalPaid = monthly * term;
            long overpayment = Math.max(0, totalPaid - loan);
            return new Quote(price, initial, term, loan, monthly, totalPaid, overpayment);
        }

        public boolean hasValidInputs(String initialPayment, String months) {
            return parseLongOrNull(initialPayment) != null && parseIntOrNull(months) != null;
        }

        public static Long parseLongOrNull(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(value.trim().replace(" ", "").replace(",", ".").split("\\.")[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public static Integer parseIntOrNull(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(value.trim().replace(" ", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static final class OrderReceipt {

        private OrderReceipt() {
        }

        public record Data(
                String carName,
                long subtotalRub,
                long feesRub,
                long totalRub,
                String customerName,
                String customerPhone,
                String customerEmail,
                String paymentMethod,
                String paymentDetails
        ) {
        }

        public static String format(Data data) {
            StringBuilder sb = new StringBuilder();
            sb.append("Автомобиль: ").append(data.carName()).append('\n');
            sb.append("Стоимость: ").append(formatRub(data.subtotalRub())).append(" ₽\n");
            sb.append("Оформление: ").append(formatRub(data.feesRub())).append(" ₽\n");
            sb.append("Итого: ").append(formatRub(data.totalRub())).append(" ₽\n\n");
            sb.append("Покупатель: ").append(data.customerName()).append('\n');
            sb.append("Телефон: ").append(data.customerPhone()).append('\n');
            sb.append("E-mail: ").append(data.customerEmail()).append('\n\n');
            sb.append("Оплата: ").append(data.paymentMethod());
            if (data.paymentDetails() != null && !data.paymentDetails().isBlank()) {
                sb.append(" (").append(data.paymentDetails()).append(')');
            }
            sb.append('\n');
            sb.append("Дата: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            return sb.toString();
        }

        private static String formatRub(long value) {
            return NumberFormat.getIntegerInstance(new Locale("ru", "RU")).format(value);
        }
    }
}
