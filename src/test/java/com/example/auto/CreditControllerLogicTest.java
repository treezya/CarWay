package com.example.auto;

import com.example.auto.CarWayScreensExtra.CreditController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditControllerLogicTest {

    @Test
    void parseLong_acceptsSpacedNumericInput() throws Exception {
        long value = (long) invokeStatic("parseLong", "1 200 000");
        assertEquals(1_200_000L, value);
    }

    @Test
    void parseInt_acceptsSpacedNumericInput() throws Exception {
        int value = (int) invokeStatic("parseInt", " 36 ");
        assertEquals(36, value);
    }

    @Test
    void parseLong_throwsForInvalidValue() {
        assertThrows(NumberFormatException.class, () -> invokeStatic("parseLong", "abc"));
    }

    @Test
    void isBlank_detectsEmptyAndFilledValues() throws Exception {
        assertTrue((Boolean) invokeStatic("isBlank", " "));
        assertTrue(!(Boolean) invokeStatic("isBlank", "Иванов"));
    }

    private static Object invokeStatic(String methodName, String input) throws Exception {
        Method m = CreditController.class.getDeclaredMethod(methodName, String.class);
        m.setAccessible(true);
        try {
            return m.invoke(null, input);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }
    }
}
