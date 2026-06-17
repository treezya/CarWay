package com.example.auto;

import com.example.auto.CarWayData.Car;
import com.example.auto.CarWayData.CarFilter;
import com.example.auto.CarWayData.CarImageLoader;
import com.example.auto.CarWayData.CarMapper;
import com.example.auto.CarWayData.CarService;
import com.example.auto.CarWayData.CheckoutDraft;
import com.example.auto.CarWayData.OrderService;
import com.example.auto.CarWayData.RentalService;
import com.example.auto.CarWayData.SelectedCarContext;
import com.example.auto.CarWayData.SupabaseConfig;
import com.example.auto.CarWayData.SupabaseRestClient;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public final class CarWayScreensMain {

    private CarWayScreensMain() {
    }

    public static class MainController implements Initializable {
    
        private static final Logger LOG = CarWaySupport.AppLogger.get(MainController.class);
        private static final double CAR_PHOTO_WIDTH = 276;
        private static final double CAR_PHOTO_HEIGHT = 168;
        private static final double CAR_PHOTO_INSET = 8;
    
        @FXML private ComboBox<String> brandCombo;
        @FXML private TextField priceFromField;
        @FXML private TextField priceToField;
        @FXML private ComboBox<String> colorCombo;
        @FXML private ComboBox<String> bodyCombo;
        @FXML private ComboBox<String> fuelCombo;
        @FXML private ComboBox<String> mileageCombo;
        @FXML private TextField yearFromField;
        @FXML private TextField yearToField;
    
        @FXML private TilePane carTilePane;
        @FXML private ScrollPane catalogScrollPane;
        @FXML private TextField catalogSearchField;
        @FXML private Label catalogCountLabel;
        @FXML private Label selectedCarLabel;
        @FXML private Label subtotalLabel;
        @FXML private Label feesLabel;
        @FXML private Label totalLabel;
    
        @FXML private TextField customerNameField;
        @FXML private TextField customerPhoneField;
        @FXML private TextField customerEmailField;
        @FXML private ComboBox<String> paymentMethodCombo;
        @FXML private TextField cardNumberField;
        @FXML private TextField cardHolderField;
        @FXML private TextField cardExpiryField;
        @FXML private TextField cardCvvField;
        @FXML private VBox cardFieldsBox;
    
        private final List<CarWayData.Car> allCars = new ArrayList<>();
        private final CarService carService = new CarWayData.CarService();
        private final OrderService orderService = new CarWayData.OrderService();
        private final RentalService rentalService = new CarWayData.RentalService();
        private final CarWayData.CarDetailsService detailsService = new CarWayData.CarDetailsService();
        private CarWayData.Car selectedCar;
    
        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            allCars.clear();
            allCars.addAll(carService.seedLocalCars());
            setupComboFromCars(brandCombo, c -> c.getName().split(" ")[0]);
            setupComboFromCars(colorCombo, CarWayData.Car::getColor);
            setupComboFromCars(bodyCombo, CarWayData.Car::getBodyType);
            setupComboFromCars(fuelCombo, CarWayData.Car::getFuelType);
    
            mileageCombo.getItems().setAll(CarWayData.CarService.ANY, "до 30 000 км", "до 60 000 км", "до 100 000 км", "более 100 000 км");
            mileageCombo.setValue(CarWayData.CarService.ANY);
    
            paymentMethodCombo.getItems().setAll("Банковская карта", "Кредит");
            paymentMethodCombo.setValue("Банковская карта");
    
            carTilePane.setPrefTileWidth(300);
            carTilePane.setPrefTileHeight(500);
            catalogScrollPane.viewportBoundsProperty().addListener((obs, oldV, newV) -> updateCatalogColumns(newV.getWidth()));
            updateCatalogColumns(catalogScrollPane.getViewportBounds().getWidth());
    
            catalogSearchField.textProperty().addListener((obs, oldV, newV) -> applyCatalogFilters());
            paymentMethodCombo.setOnAction(e -> updatePaymentFieldsVisibility());
            updatePaymentFieldsVisibility();
    
            applyCatalogFilters();
            loadSupabaseCatalogIfConfigured();
            Platform.runLater(this::resizeMainWindow);
        }
    
        private void resizeMainWindow() {
            if (catalogScrollPane == null || catalogScrollPane.getScene() == null) {
                return;
            }
            Stage stage = (Stage) catalogScrollPane.getScene().getWindow();
            if (stage != null && stage.getWidth() < 1000) {
                stage.setWidth(1140);
                stage.setHeight(740);
            }
        }
    
        private void loadSupabaseCatalogIfConfigured() {
            CarWayData.SupabaseConfig cfg = CarWayData.SupabaseConfig.fromEnvOrNull();
            if (cfg != null) {
                new Thread(() -> {
                    try {
                        CarWayData.SupabaseRestClient client = new CarWayData.SupabaseRestClient(cfg);
                        var rows = client.fetchCars();
                        var cars = rows.stream().map(CarWayData.CarMapper::toCar).toList();
                        javafx.application.Platform.runLater(() -> {
                            if (!cars.isEmpty()) {
                                CarWayData.CarImageLoader.clearCache();
                                allCars.clear();
                                allCars.addAll(cars);
                                setupComboFromCars(brandCombo, c -> c.getName().split(" ")[0]);
                                setupComboFromCars(colorCombo, CarWayData.Car::getColor);
                                setupComboFromCars(bodyCombo, CarWayData.Car::getBodyType);
                                setupComboFromCars(fuelCombo, CarWayData.Car::getFuelType);
                                applyCatalogFilters();
                            }
                        });
                    } catch (IOException | InterruptedException e) {
                        CarWaySupport.AppLogger.logError(LOG, "Не удалось загрузить каталог из Supabase", e);
                    }
                }, "supabase-cars-loader").start();
            }
        }
    
        @FXML
        private void filterCars() {
            applyCatalogFilters();
        }
    
        @FXML
        private void resetFilters() {
            brandCombo.setValue(CarWayData.CarService.ANY);
            colorCombo.setValue(CarWayData.CarService.ANY);
            bodyCombo.setValue(CarWayData.CarService.ANY);
            fuelCombo.setValue(CarWayData.CarService.ANY);
            mileageCombo.setValue(CarWayData.CarService.ANY);
            priceFromField.clear();
            priceToField.clear();
            yearFromField.clear();
            yearToField.clear();
            if (catalogSearchField != null) {
                catalogSearchField.clear();
            }
            applyCatalogFilters();
        }
    
        private void applyCatalogFilters() {
            CarWayData.CarFilter filter = new CarWayData.CarFilter(
                    normalizeAny(brandCombo.getValue()),
                    normalizeAny(colorCombo.getValue()),
                    normalizeAny(bodyCombo.getValue()),
                    normalizeAny(fuelCombo.getValue()),
                    normalizeAny(mileageCombo.getValue()),
                    parseDoubleOrNull(priceFromField.getText()),
                    parseDoubleOrNull(priceToField.getText()),
                    parseIntOrNull(yearFromField.getText()),
                    parseIntOrNull(yearToField.getText())
            );
    
            List<CarWayData.Car> filtered = carService.filterCars(allCars, filter);
            filtered = applySearchFilter(filtered);
            catalogCountLabel.setText("В каталоге: " + filtered.size() + " "
                    + pluralAuto(filtered.size()));
    
            renderCars(filtered);
            if (filtered.isEmpty()) {
                selectedCar = null;
                updateCheckout(null);
            } else if (selectedCar != null && carInList(selectedCar, filtered)) {
                updateCheckout(selectedCar);
            } else {
                selectCar(filtered.get(0));
            }
        }
    
        @FXML
        private void placeOrder(ActionEvent event) {
            if (selectedCar == null) {
                showInfo("Сначала выберите автомобиль.");
                return;
            }
            if (!orderService.hasValidCustomerData(
                    customerNameField.getText(),
                    customerPhoneField.getText(),
                    customerEmailField.getText()
            )) {
                showInfo("Заполните данные клиента.");
                return;
            }
            String payment = paymentMethodCombo.getValue();
            if ("Кредит".equals(payment)) {
                CarWayData.CheckoutDraft.set(orderService.buildDraft(
                        selectedCar,
                        customerNameField.getText(),
                        customerPhoneField.getText(),
                        customerEmailField.getText()
                ));
                openCreditScreen(event);
                return;
            }
            if (!orderService.hasValidCardData(
                    cardNumberField.getText(),
                    cardHolderField.getText(),
                    cardExpiryField.getText(),
                    cardCvvField.getText()
            )) {
                showInfo("Заполните все данные банковской карты.");
                return;
            }
            CarWayData.SupabaseConfig cfg = CarWayData.SupabaseConfig.fromEnvOrNull();
            if (cfg != null && selectedCar.getId() != null) {
                try {
                    CarWayData.SupabaseRestClient client = new CarWayData.SupabaseRestClient(cfg);
                    client.createOrderWithCard(orderService.buildCardOrderRequest(
                            selectedCar,
                            customerNameField.getText(),
                            customerPhoneField.getText(),
                            customerEmailField.getText(),
                            cardNumberField.getText(),
                            cardHolderField.getText(),
                            cardExpiryField.getText()
                    ));
                    showInfo("Покупка оформлена и сохранена в Supabase.");
                    return;
                } catch (IOException | InterruptedException e) {
                    CarWaySupport.AppLogger.logError(LOG, "Ошибка сохранения заказа в Supabase", e);
                    showInfo("Не удалось сохранить заказ в Supabase, заказ оформлен.");
                }
            }
            showInfo("Покупка оформлена успешно.");
        }
    
        @FXML
        private void goBackToStart(ActionEvent event) {
            navigate(event, "/com/example/auto/start-view.fxml");
        }
    
        private List<CarWayData.Car> applySearchFilter(List<CarWayData.Car> cars) {
            if (catalogSearchField == null) {
                return cars;
            }
            String query = catalogSearchField.getText();
            if (query == null || query.trim().isEmpty()) {
                return cars;
            }
            String lower = query.trim().toLowerCase(Locale.ROOT);
            return cars.stream()
                    .filter(c -> c.getName().toLowerCase(Locale.ROOT).contains(lower)
                            || c.getColor().toLowerCase(Locale.ROOT).contains(lower)
                            || c.getBodyType().toLowerCase(Locale.ROOT).contains(lower))
                    .toList();
        }
    
        private void updatePaymentFieldsVisibility() {
            boolean card = "Банковская карта".equals(paymentMethodCombo.getValue());
            cardFieldsBox.setVisible(card);
            cardFieldsBox.setManaged(card);
        }
    
        private static String pluralAuto(int n) {
            int mod10 = n % 10;
            int mod100 = n % 100;
            if (mod100 >= 11 && mod100 <= 14) {
                return "автомобилей";
            }
            if (mod10 == 1) {
                return "автомобиль";
            }
            if (mod10 >= 2 && mod10 <= 4) {
                return "автомобиля";
            }
            return "автомобилей";
        }
    
        private void openCreditScreen(ActionEvent event) {
            navigate(event, "/com/example/auto/credit-view.fxml");
        }
    
        private void navigate(ActionEvent event, String resourcePath) {
            CarWaySupport.SceneNavigator.navigate(event, getClass(), resourcePath);
        }
    
        private void showInfo(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        private void showCarDetails(CarWayData.Car car) {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("CarWay — характеристики");
            dialog.setHeaderText(car.getName());
            dialog.getDialogPane().getStyleClass().add("car-details-dialog");

            GridPane grid = new GridPane();
            grid.setHgap(16);
            grid.setVgap(8);
            grid.setPadding(new Insets(4, 8, 8, 8));
            grid.getStyleClass().add("car-details-grid");

            List<CarWayData.CarDetailsService.DetailLine> lines = detailsService.buildDetails(car);
            for (int i = 0; i < lines.size(); i++) {
                CarWayData.CarDetailsService.DetailLine line = lines.get(i);
                Label caption = new Label(line.caption());
                caption.getStyleClass().add("car-spec-caption");
                Label value = new Label(line.value());
                value.getStyleClass().add("car-spec-value");
                value.setWrapText(true);
                value.setMaxWidth(260);
                grid.add(caption, 0, i);
                grid.add(value, 1, i);
            }

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            if (catalogScrollPane != null && catalogScrollPane.getScene() != null) {
                dialog.getDialogPane().getStylesheets().addAll(catalogScrollPane.getScene().getStylesheets());
            }
            dialog.showAndWait();
        }
    
        private interface CarField {
            String valueOf(CarWayData.Car c);
        }
    
        private void setupComboFromCars(ComboBox<String> combo, CarField field) {
            combo.getItems().clear();
            combo.getItems().add(CarWayData.CarService.ANY);
            List<String> sorted = carService.extractSortedValues(allCars, field::valueOf);
            combo.getItems().addAll(sorted);
            combo.setValue(CarWayData.CarService.ANY);
        }
    
        private void openServicesWindow(CarWayData.Car car, int tabIndex) {
            selectCar(car);
            CarWayData.SelectedCarContext.set(
                    car,
                    tabIndex,
                    customerNameField.getText(),
                    customerPhoneField.getText()
            );
            CarWaySupport.SceneNavigator.openNewWindow(
                    getClass(),
                    "/com/example/auto/services-view.fxml",
                    "Услуги — " + car.getName(),
                    960,
                    720
            );
        }
    
        private void renderCars(List<CarWayData.Car> cars) {
            carTilePane.getChildren().clear();
            if (cars.isEmpty()) {
                VBox empty = new VBox(10);
                empty.setAlignment(Pos.CENTER);
                empty.getStyleClass().add("catalog-empty-state");
                empty.setMinWidth(360);
                empty.setMinHeight(260);
                Label title = new Label("Ничего не найдено");
                title.getStyleClass().add("catalog-empty-title");
                Label hint = new Label("Измените фильтры или нажмите «Сбросить»");
                hint.getStyleClass().add("catalog-empty-hint");
                hint.setWrapText(true);
                hint.setMaxWidth(320);
                hint.setAlignment(Pos.CENTER);
                empty.getChildren().addAll(title, hint);
                carTilePane.getChildren().add(empty);
                return;
            }
            for (CarWayData.Car car : cars) {
                carTilePane.getChildren().add(buildCarCard(car));
            }
            refreshCardHighlights();
        }
    
        private VBox buildCarCard(CarWayData.Car car) {
            VBox card = new VBox(10);
            card.setPrefWidth(300);
            card.setMinWidth(300);
            card.setMaxWidth(300);
            card.getStyleClass().add("car-card");
            card.setUserData(car);
    
            HBox badges = new HBox(6);
            Label stockBadge = new Label("В наличии");
            stockBadge.getStyleClass().add("car-badge-stock");
            badges.getChildren().add(stockBadge);
            if (car.getYear() >= 2020) {
                Label freshBadge = new Label("Свежий");
                freshBadge.getStyleClass().add("car-badge-fresh");
                badges.getChildren().add(freshBadge);
            }
            if (rentalService.isRentable(car)) {
                Label rentalBadge = new Label("Аренда");
                rentalBadge.getStyleClass().add("car-badge-rental");
                badges.getChildren().add(rentalBadge);
            }
    
            Label title = new Label(car.getName());
            title.getStyleClass().add("car-title");
            title.setWrapText(true);
    
            GridPane specs = new GridPane();
            specs.setHgap(12);
            specs.setVgap(4);
            specs.getStyleClass().add("car-specs-grid");
            addSpec(specs, 0, "Год", String.valueOf(car.getYear()));
            addSpec(specs, 1, "Пробег", formatPrice(car.getMileageKm()) + " км");
            addSpec(specs, 2, "Кузов", car.getBodyType());
            addSpec(specs, 3, "Топливо", car.getFuelType());
    
            Label colorLine = new Label(car.getColor());
            colorLine.getStyleClass().add("car-color-line");
    
            Label price = new Label(formatPrice(car.getPrice()) + " ₽");
            price.getStyleClass().add("car-price");
    
            Button buyBtn = new Button("Купить");
            buyBtn.getStyleClass().addAll("button", "car-btn-primary");
            buyBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(buyBtn, Priority.ALWAYS);
            buyBtn.setOnAction(e -> selectCar(car));
    
            Button detailsBtn = new Button("Подробнее");
            detailsBtn.getStyleClass().addAll("button", "car-btn-details");
            detailsBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(detailsBtn, Priority.ALWAYS);
            detailsBtn.setOnAction(e -> showCarDetails(car));

            Button servicesBtn = new Button("Услуги");
            servicesBtn.getStyleClass().addAll("button", "premium-preset");
            servicesBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(servicesBtn, Priority.ALWAYS);
            servicesBtn.setOnAction(e -> openServicesWindow(car, 0));

            Button tireBtn = new Button("Шиномонтаж");
            tireBtn.getStyleClass().addAll("button", "premium-preset");
            tireBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tireBtn, Priority.ALWAYS);
            tireBtn.setOnAction(e -> openServicesWindow(car, 2));

            HBox topActions = new HBox(8, buyBtn, detailsBtn);
            HBox bottomActions = new HBox(8, servicesBtn, tireBtn);
            VBox mainActions = new VBox(8, topActions, bottomActions);
    
            card.getChildren().addAll(
                    createCarPhoto(car),
                    badges,
                    title,
                    specs,
                    colorLine,
                    price,
                    mainActions
            );
            return card;
        }
    
        private static void addSpec(GridPane grid, int row, String caption, String value) {
            Label cap = new Label(caption);
            cap.getStyleClass().add("car-spec-caption");
            Label val = new Label(value);
            val.getStyleClass().add("car-spec-value");
            grid.add(cap, 0, row);
            grid.add(val, 1, row);
        }
    
        private void refreshCardHighlights() {
            for (var node : carTilePane.getChildren()) {
                if (!(node instanceof VBox card)) {
                    continue;
                }
                card.getStyleClass().remove("car-card-selected");
                Object data = card.getUserData();
                if (data instanceof CarWayData.Car car && selectedCar != null
                        && car.getName().equals(selectedCar.getName())) {
                    if (!card.getStyleClass().contains("car-card-selected")) {
                        card.getStyleClass().add("car-card-selected");
                    }
                }
            }
        }
    
        private StackPane createCarPhoto(CarWayData.Car car) {
            StackPane frame = buildPhotoFrame();
            Region backdrop = (Region) frame.getChildren().get(0);
            attachCarImage(frame, backdrop, car);
            return frame;
        }
    
        private StackPane buildPhotoFrame() {
            StackPane frame = new StackPane();
            frame.getStyleClass().add("car-image-frame");
            frame.setMinSize(CAR_PHOTO_WIDTH, CAR_PHOTO_HEIGHT);
            frame.setPrefSize(CAR_PHOTO_WIDTH, CAR_PHOTO_HEIGHT);
            frame.setMaxSize(CAR_PHOTO_WIDTH, CAR_PHOTO_HEIGHT);
            frame.setAlignment(Pos.CENTER);
    
            Rectangle clip = new Rectangle();
            clip.setArcWidth(14);
            clip.setArcHeight(14);
            clip.widthProperty().bind(frame.widthProperty());
            clip.heightProperty().bind(frame.heightProperty());
            frame.setClip(clip);
    
            Region backdrop = new Region();
            backdrop.getStyleClass().add("car-image-backdrop");
            backdrop.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            frame.getChildren().add(backdrop);
            return frame;
        }
    
        private void attachCarImage(StackPane frame, Region backdrop, CarWayData.Car car) {
            int fitW = (int) (CAR_PHOTO_WIDTH - CAR_PHOTO_INSET * 2);
            int fitH = (int) (CAR_PHOTO_HEIGHT - CAR_PHOTO_INSET * 2);
            Image image = CarWayData.CarImageLoader.load(getClass(), car, fitW, fitH);
            if (image != null && !image.isError()) {
                showImageInFrame(frame, backdrop, image);
            } else {
                showEmptyPhoto(frame, backdrop);
            }
        }
    
        private void showImageInFrame(StackPane frame, Region backdrop, Image image) {
            frame.getChildren().clear();
            ImageView imageView = new ImageView(image);
            imageView.getStyleClass().add("car-image-view");
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setFitWidth(CAR_PHOTO_WIDTH - CAR_PHOTO_INSET * 2);
            imageView.setFitHeight(CAR_PHOTO_HEIGHT - CAR_PHOTO_INSET * 2);
            StackPane.setAlignment(imageView, Pos.CENTER);
            frame.getChildren().addAll(backdrop, imageView);
        }
    
        private void showEmptyPhoto(StackPane frame, Region backdrop) {
            frame.getChildren().clear();
            VBox emptyState = new VBox(6);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.getStyleClass().add("car-image-empty");
            Label emptyIcon = new Label("◆");
            emptyIcon.getStyleClass().add("car-image-empty-icon");
            Label emptyText = new Label("Нет фото");
            emptyText.getStyleClass().add("car-image-empty-label");
            emptyState.getChildren().addAll(emptyIcon, emptyText);
            frame.getChildren().addAll(backdrop, emptyState);
        }
    
        private void updateCatalogColumns(double viewportWidth) {
            double tile = carTilePane.getPrefTileWidth();
            double gap = carTilePane.getHgap();
            if (tile <= 0) {
                tile = 320;
            }
            int cols = (int) Math.max(1, Math.floor((viewportWidth + gap) / (tile + gap)));
            carTilePane.setPrefColumns(cols);
        }
    
        private void selectCar(CarWayData.Car car) {
            selectedCar = car;
            updateCheckout(car);
            refreshCardHighlights();
        }
    
        private void updateCheckout(CarWayData.Car car) {
            if (car == null) {
                selectedCarLabel.setText("Выберите авто в каталоге");
                subtotalLabel.setText("Автомобиль: —");
                feesLabel.setText("Оформление: " + formatPrice(orderService.getFees()) + " ₽");
                totalLabel.setText("Итого: —");
                return;
            }
            double subtotal = car.getPrice();
            double total = subtotal + orderService.getFees();
            selectedCarLabel.setText(car.getName());
            subtotalLabel.setText("Автомобиль: " + formatPrice(subtotal) + " ₽");
            feesLabel.setText("Оформление: " + formatPrice(orderService.getFees()) + " ₽");
            totalLabel.setText("Итого: " + formatPrice(total) + " ₽");
        }
    
        private static boolean carInList(CarWayData.Car car, List<CarWayData.Car> list) {
            for (CarWayData.Car item : list) {
                if (item.getName().equals(car.getName())) {
                    return true;
                }
            }
            return false;
        }
    
        private static String normalizeAny(String v) {
            if (v == null || v.trim().isEmpty()) {
                return CarWayData.CarService.ANY;
            }
            return v;
        }
    
        private static Double parseDoubleOrNull(String v) {
            if (v == null || v.trim().isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(v.trim().replace(" ", "").replace(",", "."));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    
        private static Integer parseIntOrNull(String v) {
            if (v == null || v.trim().isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(v.trim().replace(" ", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    
        private static String formatPrice(double value) {
            long rounded = Math.round(value);
            return NumberFormat.getIntegerInstance(new Locale("ru", "RU")).format(rounded);
        }
    }
}
