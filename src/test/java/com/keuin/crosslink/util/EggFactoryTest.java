package com.keuin.crosslink.util;

import net.time4j.PlainDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EggFactoryTest {

    @Test
    void getEgg() {
        // 2021 lunar new year (2021.02.12)
        assertFalse(EggFactory.getEgg(PlainDate.of(2021, 2, 11)).isPresent());
        assertTrue(EggFactory.getEgg(PlainDate.of(2021, 2, 12)).isPresent());
        assertFalse(EggFactory.getEgg(PlainDate.of(2021, 2, 13)).isPresent());

        // 2022 lunar new year (2022.02.01)
        assertFalse(EggFactory.getEgg(PlainDate.of(2022, 1, 31)).isPresent());
        assertTrue(EggFactory.getEgg(PlainDate.of(2022, 2, 1)).isPresent());
        assertFalse(EggFactory.getEgg(PlainDate.of(2022, 2, 2)).isPresent());

        // 2023 lunar new year (2023.01.22)
        assertFalse(EggFactory.getEgg(PlainDate.of(2023, 1, 21)).isPresent());
        assertTrue(EggFactory.getEgg(PlainDate.of(2023, 1, 22)).isPresent());
        assertFalse(EggFactory.getEgg(PlainDate.of(2023, 1, 23)).isPresent());

        // 2024 lunar new year (2024.02.10)
        assertFalse(EggFactory.getEgg(PlainDate.of(2024, 2, 9)).isPresent());
        assertTrue(EggFactory.getEgg(PlainDate.of(2024, 2, 10)).isPresent());
        assertFalse(EggFactory.getEgg(PlainDate.of(2024, 2, 11)).isPresent());

        // 2025 lunar new year (2025.01.29)
        assertFalse(EggFactory.getEgg(PlainDate.of(2025, 1, 28)).isPresent());
        assertTrue(EggFactory.getEgg(PlainDate.of(2025, 1, 29)).isPresent());
        assertFalse(EggFactory.getEgg(PlainDate.of(2025, 1, 30)).isPresent());
    }
}