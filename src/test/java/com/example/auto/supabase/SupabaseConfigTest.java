package com.example.auto.supabase;

import com.example.auto.CarWayData.SupabaseConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SupabaseConfigTest {

    @Test
    void constructor_storesValues() {
        SupabaseConfig cfg = new SupabaseConfig("https://demo.supabase.co", "anon-key");
        assertEquals("https://demo.supabase.co", cfg.getUrl());
        assertEquals("anon-key", cfg.getAnonKey());
    }

    @Test
    void constructor_rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new SupabaseConfig(null, "k"));
        assertThrows(NullPointerException.class, () -> new SupabaseConfig("u", null));
    }
}
