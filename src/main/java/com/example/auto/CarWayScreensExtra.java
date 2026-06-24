package com.example.auto;

import com.example.auto.CarWayData.*;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class CarWayScreensExtra {

    private CarWayScreensExtra() {
    }

    public static class ServicesController implements Initializable {
    
        @FXML private Label carNameLabel;
        @FXML private Label carMetaLabel;
        @FXML private Label carPriceLabel;
        @FXML private Label rentalBadgeLabel;
        @FXML private TextField sharedCustomerNameField;
        @FXML private TextField sharedCustomerPhoneField;
        @FXML private TabPane servicesTabPane;
        @FXML private ListView<CarWayData.ServiceBookingEntry> bookingsListView;
        @FXML private Label bookingsCountLabel;
        @FXML private Label bookingsEmptyHint;
    
        @FXML private DatePicker testDriveDatePicker;
        @FXML private ComboBox<String> testDriveTimeCombo;
        @FXML private ComboBox<String> testDriveRouteCombo;
    
        @FXML private DatePicker maintenanceDatePicker;
        @FXML private ComboBox<String> maintenanceTypeCombo;
        @FXML private Label maintenancePriceLabel;
    
        @FXML private DatePicker tireDatePicker;
        @FXML private ComboBox<String> tireSeasonCombo;
        @FXML private Label tirePriceLabel;
    
        @FXML private Label rentalAvailabilityLabel;
        @FXML private HBox rentalDaysPresets;
        @FXML private TextField rentalDaysField;
        @FXML private Label rentalRateLabel;
        @FXML private Label rentalTotalLabel;
        @FXML private Button rentalSubmitButton;
    
        private final TestDriveService testDriveService = new CarWayData.TestDriveService();
        private final MaintenanceService maintenanceService = new CarWayData.MaintenanceService();
        private final TireService tireService = new CarWayData.TireService();
        private final RentalService rentalService = new CarWayData.RentalService();
        private final CarWayData.ServiceBookingRegistry bookingRegistry = CarWayData.ServiceBookingRegistry.getInstance();
    
        private CarWayData.Car car;
    
        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            car = CarWayData.SelectedCarContext.get();
            sharedCustomerNameField.setText(CarWayData.SelectedCarContext.getCustomerName());
            sharedCustomerPhoneField.setText(CarWayData.SelectedCarContext.getCustomerPhone());
    
            if (car == null) {
                showNoCarState();
                return;
            }
    
            fillCarSummary();
            initTestDriveTab();
            initMaintenanceTab();
            initTireTab();
            initRentalTab();
            setupBookingsList();
            setupPremiumTabs();
            refreshBookingsList();
    
            int tab = CarWayData.SelectedCarContext.getInitialServiceTab();
            if (tab >= 0 && tab < servicesTabPane.getTabs().size()) {
                servicesTabPane.getSelectionModel().select(tab);
            }
            CarWayData.SelectedCarContext.clearInitialServiceTab();
        }
    
        private void showNoCarState() {
            carNameLabel.setText("Автомобиль не выбран");
            carMetaLabel.setText("Закройте окно и нажмите «Услуги» у нужной машины в каталоге");
            carPriceLabel.setText("");
            rentalBadgeLabel.setText("");
            servicesTabPane.setDisable(true);
            sharedCustomerNameField.setDisable(true);
            sharedCustomerPhoneField.setDisable(true);
            refreshBookingsList();
        }
    
        private void fillCarSummary() {
            carNameLabel.setText(car.getName());
            carMetaLabel.setText(car.getYear() + " · " + car.getBodyType() + " · "
                    + formatPrice(car.getMileageKm()) + " км · " + car.getColor());
            carPriceLabel.setText(formatPrice((long) car.getPrice()) + " ₽");
            if (rentalService.isRentable(car)) {
                rentalBadgeLabel.setText("●  Аренда доступна");
                rentalBadgeLabel.getStyleClass().remove("premium-rental-muted");
                if (!rentalBadgeLabel.getStyleClass().contains("premium-rental-badge")) {
                    rentalBadgeLabel.getStyleClass().add("premium-rental-badge");
                }
            } else {
                rentalBadgeLabel.setText("Аренда недоступна");
                rentalBadgeLabel.getStyleClass().remove("premium-rental-badge");
                if (!rentalBadgeLabel.getStyleClass().contains("premium-rental-muted")) {
                    rentalBadgeLabel.getStyleClass().add("premium-rental-muted");
                }
            }
        }
    
        private void setupPremiumTabs() {
            String[] styles = {
                    "premium-tab-drive",
                    "premium-tab-maintenance",
                    "premium-tab-tire",
                    "premium-tab-rental"
            };
            for (int i = 0; i < servicesTabPane.getTabs().size() && i < styles.length; i++) {
                Tab tab = servicesTabPane.getTabs().get(i);
                if (!tab.getStyleClass().contains(styles[i])) {
                    tab.getStyleClass().add(styles[i]);
                }
            }
        }
    
        private void initTestDriveTab() {
            testDriveTimeCombo.getItems().setAll(CarWayData.TestDriveService.TIME_SLOTS);
            testDriveTimeCombo.setValue(CarWayData.TestDriveService.TIME_SLOTS.get(0));
            testDriveRouteCombo.getItems().setAll(CarWayData.TestDriveService.ROUTES);
            testDriveRouteCombo.setValue(CarWayData.TestDriveService.ROUTES.get(0));
            testDriveDatePicker.setValue(LocalDate.now().plusDays(1));
        }
    
        private void initMaintenanceTab() {
            maintenanceTypeCombo.getItems().setAll(CarWayData.MaintenanceService.SERVICE_TYPES);
            maintenanceTypeCombo.setValue(CarWayData.MaintenanceService.SERVICE_TYPES.get(0));
            maintenanceTypeCombo.setOnAction(e -> updateMaintenancePrice());
            maintenanceDatePicker.setValue(LocalDate.now().plusDays(2));
            updateMaintenancePrice();
        }
    
        private void initTireTab() {
            tireSeasonCombo.getItems().setAll(CarWayData.TireService.SEASONS);
            tireSeasonCombo.setValue(CarWayData.TireService.SEASONS.get(0));
            tireSeasonCombo.setOnAction(e -> updateTirePrice());
            tireDatePicker.setValue(LocalDate.now().plusDays(3));
            updateTirePrice();
        }
    
        private void initRentalTab() {
            rentalDaysField.setText("3");
            rentalDaysField.textProperty().addListener((obs, o, n) -> updateRentalQuote());
            for (int days : new int[] {1, 3, 7, 14}) {
                Button preset = new Button(days + " дн.");
                preset.getStyleClass().addAll("button", "premium-preset");
                int value = days;
                preset.setOnAction(e -> {
                    rentalDaysField.setText(String.valueOf(value));
                    updateRentalQuote();
                });
                rentalDaysPresets.getChildren().add(preset);
            }
            updateRentalQuote();
        }
    
        @FXML
        private void closeWindow(ActionEvent event) {
            Stage stage = (Stage) carNameLabel.getScene().getWindow();
            stage.close();
        }
    
        @FXML
        private void submitTestDrive() {
            if (car == null) {
                showInfo("Выберите автомобиль в каталоге и нажмите «Услуги».");
                return;
            }
    
            String name = sharedCustomerNameField.getText();
            String phone = sharedCustomerPhoneField.getText();
            LocalDate date = testDriveDatePicker.getValue();
            String time = testDriveTimeCombo.getValue();
            String route = testDriveRouteCombo.getValue();
    
            if (!testDriveService.canBook(name, phone, date)) {
                showInfo("Заполните ФИО, телефон и дату (не в прошлом).");
                return;
            }
    
            CarWayData.TestDriveBooking booking = testDriveService.createBooking(car, name, phone, date, time, route);
            bookingRegistry.addTestDrive(booking);
            refreshBookingsList();
            showSuccess("Тест-драйв", "Запись оформлена на " + date + " в " + time);
        }
    
        @FXML
        private void submitMaintenance() {
            if (car == null) {
                showInfo("Выберите автомобиль в каталоге и нажмите «Услуги».");
                return;
            }
    
            String name = sharedCustomerNameField.getText();
            String phone = sharedCustomerPhoneField.getText();
            LocalDate date = maintenanceDatePicker.getValue();
            String serviceType = maintenanceTypeCombo.getValue();
    
            if (!maintenanceService.canBook(name, phone, date)) {
                showInfo("Заполните ФИО, телефон и дату (не в прошлом).");
                return;
            }
    
            CarWayData.MaintenanceBooking booking = maintenanceService.createBooking(car, name, phone, date, serviceType);
            bookingRegistry.addMaintenance(booking);
            refreshBookingsList();
            showSuccess("ТО", booking.serviceType() + ", " + formatPrice(booking.estimatedPriceRub()) + " ₽");
        }
    
        @FXML
        private void submitTireService() {
            if (car == null) {
                showInfo("Выберите автомобиль в каталоге и нажмите «Услуги».");
                return;
            }
    
            String name = sharedCustomerNameField.getText();
            String phone = sharedCustomerPhoneField.getText();
            LocalDate date = tireDatePicker.getValue();
            String season = tireSeasonCombo.getValue();
    
            if (!tireService.canBook(name, phone, date)) {
                showInfo("Заполните ФИО, телефон и дату (не в прошлом).");
                return;
            }
    
            CarWayData.TireServiceBooking booking = tireService.createBooking(car, name, phone, date, season);
            bookingRegistry.addTire(booking);
            refreshBookingsList();
            showSuccess("Шиномонтаж", booking.season() + ", " + formatPrice(booking.estimatedPriceRub()) + " ₽");
        }
    
        @FXML
        private void submitRental() {
            if (car == null) {
                showInfo("Выберите автомобиль в каталоге и нажмите «Услуги».");
                return;
            }
            if (!rentalService.isRentable(car)) {
                showInfo("Автомобиль недоступен для аренды (год до 2018 или пробег более 100 000 км).");
                return;
            }
    
            int days = parseIntOrDefault(rentalDaysField.getText(), 0);
            String name = sharedCustomerNameField.getText();
            String phone = sharedCustomerPhoneField.getText();
    
            if (!rentalService.canBook(name, phone, days)) {
                showInfo("Заполните ФИО, телефон и срок от 1 до 30 дней.");
                return;
            }
    
            CarWayData.RentalBooking booking = rentalService.createBooking(car, name, phone, days);
            bookingRegistry.addRental(booking);
            refreshBookingsList();
            showSuccess("Аренда", booking.days() + " дн., " + formatPrice(booking.totalRub()) + " ₽");
        }
    
        private void setupBookingsList() {
            bookingsListView.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(CarWayData.ServiceBookingEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    Label type = new Label(item.typeLabel());
                    type.getStyleClass().add("booking-cell-type");
                    Label summary = new Label(item.summary());
                    summary.getStyleClass().add("booking-cell-summary");
                    summary.setWrapText(true);
                    Label status = new Label(item.status());
                    status.getStyleClass().add("booking-cell-status");
                    VBox box = new VBox(4, type, summary, status);
                    box.getStyleClass().add("premium-booking-cell");
                    setGraphic(box);
                }
            });
        }
    
        private void refreshBookingsList() {
            var entries = bookingRegistry.getEntries();
            bookingsListView.setItems(FXCollections.observableArrayList(entries));
            int count = entries.size();
            boolean hasEntries = count > 0;
            if (bookingsEmptyHint != null) {
                bookingsEmptyHint.setVisible(!hasEntries);
                bookingsEmptyHint.setManaged(!hasEntries);
            }
            if (count == 0) {
                bookingsCountLabel.setText("Пока пусто");
            } else {
                bookingsCountLabel.setText("Оформлено: " + count + " " + pluralZayavka(count));
            }
        }
    
        private static String pluralZayavka(int n) {
            int mod10 = n % 10;
            int mod100 = n % 100;
            if (mod100 >= 11 && mod100 <= 14) {
                return "заявок";
            }
            if (mod10 == 1) {
                return "заявка";
            }
            if (mod10 >= 2 && mod10 <= 4) {
                return "заявки";
            }
            return "заявок";
        }
    
        private void updateMaintenancePrice() {
            String type = maintenanceTypeCombo.getValue();
            if (type == null) {
                maintenancePriceLabel.setText("—");
                return;
            }
            maintenancePriceLabel.setText(formatPrice(maintenanceService.estimatePriceRub(type)) + " ₽");
        }
    
        private void updateTirePrice() {
            String season = tireSeasonCombo.getValue();
            if (season == null) {
                tirePriceLabel.setText("—");
                return;
            }
            tirePriceLabel.setText(formatPrice(tireService.estimatePriceRub(season)) + " ₽");
        }
    
        private void updateRentalQuote() {
            if (car == null) {
                return;
            }
            if (!rentalService.isRentable(car)) {
                rentalAvailabilityLabel.setText("Недоступно: год выпуска до 2018 или пробег более 100 000 км");
                rentalRateLabel.setText("Тариф: —");
                rentalTotalLabel.setText("К оплате: —");
                rentalSubmitButton.setDisable(true);
                return;
            }
            rentalAvailabilityLabel.setText("Аренда доступна. Тариф зависит от класса автомобиля и срока.");
            rentalSubmitButton.setDisable(false);
            int days = parseIntOrDefault(rentalDaysField.getText(), 3);
            long daily = rentalService.dailyRateRub(car);
            rentalRateLabel.setText("Тариф · " + formatPrice(daily) + " ₽ / сутки");
            rentalTotalLabel.setText(formatPrice(rentalService.totalCostRub(car, days))
                    + " ₽  ·  " + days + " дней");
        }
    
        private String customerName() {
            return sharedCustomerNameField.getText();
        }
    
        private String customerPhone() {
            return sharedCustomerPhoneField.getText();
        }
    
        private boolean ensureCar() {
            if (car == null) {
                showInfo("Автомобиль не выбран. Закройте окно и выберите авто в каталоге.");
                return false;
            }
            return true;
        }
    
        private static int parseIntOrDefault(String value, int defaultValue) {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value.trim().replace(" ", ""));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    
        private static String formatPrice(long value) {
            return NumberFormat.getIntegerInstance(new Locale("ru", "RU")).format(value);
        }
    
        private void showSuccess(String service, String details) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("CarWay Service");
            alert.setHeaderText(service + " — заявка принята");
            alert.setContentText(car.getName() + "\n" + details
                    + "\n\nЗапись добавлена в список справа. Напоминание придёт на указанный телефон.");
            alert.showAndWait();
        }
    
        private void showInfo(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    public static class CreditController implements Initializable {

        private static final Logger LOG = CarWaySupport.AppLogger.get(CreditController.class);
        private final CreditCalculatorService calculator = new CreditCalculatorService();

        @FXML
        private Label creditCarLabel;
        @FXML
        private Label creditOrderSubtotalLabel;
        @FXML
        private Label creditOrderFeesLabel;
        @FXML
        private Label creditOrderTotalLabel;
        @FXML
        private Label creditLoanLabel;
        @FXML
        private Label creditMonthlyLabel;
        @FXML
        private Label creditOverpayLabel;
        @FXML
        private Label creditRateHintLabel;
        @FXML
        private TextField fullNameField;
        @FXML
        private TextField passportField;
        @FXML
        private TextField incomeField;
        @FXML
        private TextField workPlaceField;
        @FXML
        private TextField initialPaymentField;
        @FXML
        private TextField monthsField;

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            creditRateHintLabel.setText(String.format(
                    Locale.forLanguageTag("ru-RU"),
                    "Ставка %.0f%% годовых, аннуитетный платёж",
                    CreditCalculatorService.ANNUAL_RATE_PERCENT));
            initialPaymentField.textProperty().addListener((obs, oldV, newV) -> updateCreditQuote());
            monthsField.textProperty().addListener((obs, oldV, newV) -> updateCreditQuote());
            updateCreditQuote();
        }

        private void updateCreditQuote() {
            CheckoutDraft.Draft draft = CheckoutDraft.get();
            Car car = SelectedCarContext.get();
            if (draft == null) {
                creditCarLabel.setText("Автомобиль: —");
                creditOrderSubtotalLabel.setText("Автомобиль: —");
                creditOrderFeesLabel.setText("Оформление: —");
                creditOrderTotalLabel.setText("Итого: —");
                creditLoanLabel.setText("Сумма кредита: —");
                creditMonthlyLabel.setText("Ежемесячный платёж: —");
                creditOverpayLabel.setText("Переплата: —");
                return;
            }

            String carName = car != null ? car.getName() : "выбранный автомобиль";
            creditCarLabel.setText("Автомобиль: " + carName);
            creditOrderSubtotalLabel.setText("Автомобиль: " + formatPrice(draft.subtotalRub()) + " ₽");
            creditOrderFeesLabel.setText("Оформление: " + formatPrice(draft.feesRub()) + " ₽");
            creditOrderTotalLabel.setText("Итого: " + formatPrice(draft.totalRub()) + " ₽");

            Long initial = CreditCalculatorService.parseLongOrNull(initialPaymentField.getText());
            Integer months = CreditCalculatorService.parseIntOrNull(monthsField.getText());
            if (initial == null || months == null) {
                creditLoanLabel.setText("Сумма кредита: —");
                creditMonthlyLabel.setText("Ежемесячный платёж: —");
                creditOverpayLabel.setText("Переплата: —");
                return;
            }

            CreditCalculatorService.Quote quote = calculator.calculate(draft.totalRub(), initial, months);
            creditLoanLabel.setText("Сумма кредита: " + formatPrice(quote.loanAmountRub()) + " ₽");
            creditMonthlyLabel.setText("Ежемесячный платёж: " + formatPrice(quote.monthlyPaymentRub()) + " ₽");
            creditOverpayLabel.setText("Переплата: " + formatPrice(quote.overpaymentRub()) + " ₽");
        }

        @FXML
        private void goBack(ActionEvent event) {
            CarWaySupport.SceneNavigator.navigate(event, getClass(), "/com/example/auto/main-view.fxml");
        }

        @FXML
        private void submitCredit(ActionEvent event) {
            if (isBlank(fullNameField.getText())
                    || isBlank(passportField.getText())
                    || isBlank(incomeField.getText())
                    || isBlank(workPlaceField.getText())
                    || isBlank(initialPaymentField.getText())
                    || isBlank(monthsField.getText())) {
                showInfo("Заполните все поля кредитной заявки.");
                return;
            }

            CheckoutDraft.Draft draft = CheckoutDraft.get();
            if (draft == null) {
                showInfo("Нет данных заказа. Вернитесь в каталог и выберите автомобиль.");
                return;
            }

            SupabaseConfig cfg = SupabaseConfig.fromEnvOrNull();
            if (cfg != null && draft.carId() != null) {
                try {
                    SupabaseRestClient client = new SupabaseRestClient(cfg);
                    client.createOrderWithCredit(new SupabaseRestClient.CreditOrderRequest(
                            draft.carId(),
                            draft.customerName(),
                            draft.customerPhone(),
                            draft.customerEmail(),
                            draft.subtotalRub(),
                            draft.feesRub(),
                            draft.totalRub(),
                            fullNameField.getText().trim(),
                            passportField.getText().trim(),
                            parseLong(incomeField.getText()),
                            workPlaceField.getText().trim(),
                            parseLong(initialPaymentField.getText()),
                            parseInt(monthsField.getText())
                    ));
                    CheckoutDraft.clear();
                    showInfo("Кредитная заявка отправлена и сохранена в Supabase.");
                    CarWaySupport.SceneNavigator.navigate(event, getClass(), "/com/example/auto/main-view.fxml");
                    return;
                } catch (IOException | InterruptedException e) {
                    CarWaySupport.AppLogger.logError(LOG, "Ошибка отправки кредитной заявки в Supabase", e);
                    showInfo("Не удалось отправить заявку в Supabase, заявка сохранена.");
                }
            }

            CheckoutDraft.clear();
            showInfo("Кредитная заявка отправлена.");
            CarWaySupport.SceneNavigator.navigate(event, getClass(), "/com/example/auto/main-view.fxml");
        }

        private void showInfo(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        private static String formatPrice(long value) {
            return NumberFormat.getIntegerInstance(new Locale("ru", "RU")).format(value);
        }

        private static boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }

        private static long parseLong(String value) {
            return Long.parseLong(value.trim().replace(" ", "").replace(",", ".").split("\\.")[0]);
        }

        private static int parseInt(String value) {
            return Integer.parseInt(value.trim().replace(" ", ""));
        }
    }
}
