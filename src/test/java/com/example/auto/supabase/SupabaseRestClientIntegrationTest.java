package com.example.auto.supabase;

import com.example.auto.CarWayData.SupabaseCarRow;
import com.example.auto.CarWayData.SupabaseConfig;
import com.example.auto.CarWayData.SupabaseRestClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SupabaseRestClientIntegrationTest {

    private HttpServer server;
    private String lastBody = "";
    private String lastPath = "";

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchCars_readsJsonFromPostgrestEndpoint() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/rest/v1/cars", exchange -> {
            lastPath = exchange.getRequestURI().toString();
            send(exchange, 200, """
                    [
                      {
                        "id":"9a6d9fca-c50a-4412-9c95-9f505545cf3b",
                        "name":"BMW M5",
                        "brand":"BMW",
                        "color":"Оранжевый",
                        "body_type":"Седан",
                        "fuel_type":"Бензин",
                        "year":2021,
                        "mileage_km":35000,
                        "price_rub":6000000,
                        "image_path":"cars/bmw.webp"
                      }
                    ]
                    """);
        });
        server.start();

        SupabaseConfig config = new SupabaseConfig(url(), "test-anon-key");
        SupabaseRestClient client = new SupabaseRestClient(config);
        List<SupabaseCarRow> rows = client.fetchCars();

        assertEquals(1, rows.size());
        assertEquals("BMW M5", rows.get(0).name);
        assertTrue(lastPath.contains("select=id,name,brand,color,body_type,fuel_type,year,mileage_km,price_rub,image_path"));
    }

    @Test
    void createOrderWithCard_postsRpcPayload() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/rest/v1/rpc/create_order_with_card", exchange -> {
            lastPath = exchange.getRequestURI().toString();
            lastBody = body(exchange);
            send(exchange, 200, "\"5cb51aa5-fceb-411b-9708-c8f615f51fe6\"");
        });
        server.start();

        SupabaseRestClient client = new SupabaseRestClient(new SupabaseConfig(url(), "test-anon-key"));
        client.createOrderWithCard(new SupabaseRestClient.CardOrderRequest(
                "8f89e1b9-8449-4689-a2ba-d6741061df08",
                "Иванов Иван",
                "+79990000000",
                "ivanov@example.com",
                3000000,
                35000,
                3035000,
                "1234",
                "IVAN IVANOV",
                "12/28"
        ));

        assertEquals("/rest/v1/rpc/create_order_with_card", lastPath);
        assertTrue(lastBody.contains("\"card_last4\":\"1234\""));
        assertTrue(lastBody.contains("\"car_id\":\"8f89e1b9-8449-4689-a2ba-d6741061df08\""));
    }

    @Test
    void createOrderWithCredit_postsRpcPayload() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/rest/v1/rpc/create_order_with_credit", exchange -> {
            lastPath = exchange.getRequestURI().toString();
            lastBody = body(exchange);
            send(exchange, 200, "\"2f9d85f1-8d16-4594-8651-ff9b3ce6cf90\"");
        });
        server.start();

        SupabaseRestClient client = new SupabaseRestClient(new SupabaseConfig(url(), "test-anon-key"));
        client.createOrderWithCredit(new SupabaseRestClient.CreditOrderRequest(
                "8f89e1b9-8449-4689-a2ba-d6741061df08",
                "Петров Петр",
                "+79990000001",
                "petrov@example.com",
                2_000_000,
                35_000,
                2_035_000,
                "Петров Петр Петрович",
                "1234 567890",
                180_000,
                "ООО Тест",
                500_000,
                36
        ));

        assertEquals("/rest/v1/rpc/create_order_with_credit", lastPath);
        assertTrue(lastBody.contains("\"borrower_full_name\":\"Петров Петр Петрович\""));
        assertTrue(lastBody.contains("\"months\":36"));
    }

    @Test
    void fetchCars_throwsWhenSupabaseRespondsWithError() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/rest/v1/cars", exchange -> send(exchange, 500, "{\"error\":\"boom\"}"));
        server.start();

        SupabaseRestClient client = new SupabaseRestClient(new SupabaseConfig(url(), "test-anon-key"));
        IOException ex = assertThrows(IOException.class, client::fetchCars);
        assertTrue(ex.getMessage().contains("Supabase REST error 500"));
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String body(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
