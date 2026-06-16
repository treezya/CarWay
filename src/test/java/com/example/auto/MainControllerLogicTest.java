package com.example.auto;

import com.example.auto.CarWayData;
import com.example.auto.CarWayData.CarService;
import com.example.auto.CarWayScreensMain.MainController;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainControllerLogicTest {

    @Test
    void parseDoubleOrNull_acceptsLocalizedNumbers() throws Exception {
        Double value = (Double) invokeStatic("parseDoubleOrNull", new Class[]{String.class}, "1 234,56");
        assertEquals(1234.56, value, 0.0001);
    }

    @Test
    void parseDoubleOrNull_returnsNullForBlankOrInvalid() throws Exception {
        assertNull(invokeStatic("parseDoubleOrNull", new Class[]{String.class}, " "));
        assertNull(invokeStatic("parseDoubleOrNull", new Class[]{String.class}, "abc"));
    }

    @Test
    void parseIntOrNull_acceptsSpacedInteger() throws Exception {
        Integer value = (Integer) invokeStatic("parseIntOrNull", new Class[]{String.class}, "12 345");
        assertEquals(12345, value);
    }

    @Test
    void parseIntOrNull_returnsNullForInvalid() throws Exception {
        assertNull(invokeStatic("parseIntOrNull", new Class[]{String.class}, "10.5"));
    }

    @Test
    void last4_extractsOnlyDigitsAndKeepsTail() throws Exception {
        String last4 = (String) invokeOrderServiceStatic("last4", new Class[]{String.class}, "1234 5678 9999 0001");
        assertEquals("0001", last4);
    }

    @Test
    void formatPrice_usesRussianGrouping() throws Exception {
        String formatted = (String) invokeStatic("formatPrice", new Class[]{double.class}, 3035000.0);
        assertTrue(formatted.contains("3"));
        assertTrue(formatted.contains("035"));
    }

    @Test
    void matchMileageRule_matchesBoundaries() throws Exception {
        CarService carService = new CarService();
        assertTrue((Boolean) invokeInstance(carService, "matchMileageRule", new Class[]{int.class, String.class}, 30_000, "до 30 000 км"));
        assertTrue((Boolean) invokeInstance(carService, "matchMileageRule", new Class[]{int.class, String.class}, 120_000, "более 100 000 км"));
    }

    private static Object invokeStatic(String methodName, Class<?>[] signature, Object... args) throws Exception {
        Method method = MainController.class.getDeclaredMethod(methodName, signature);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    private static Object invokeOrderServiceStatic(String methodName, Class<?>[] signature, Object... args) throws Exception {
        Method method = CarWayData.OrderService.class.getDeclaredMethod(methodName, signature);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    private static Object invokeInstance(Object target, String methodName, Class<?>[] signature, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, signature);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw unwrap(e);
        }
    }

    private static Exception unwrap(InvocationTargetException e) throws Exception {
        Throwable cause = e.getCause();
        if (cause instanceof Exception ex) {
            return ex;
        }
        throw e;
    }
}
