package com.keuin.crosslink.util;

import net.time4j.PlainDate;
import net.time4j.calendar.ChineseCalendar;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

final class EggFactory {
    public static Optional<String> getEgg(@NotNull PlainDate today) {
        var lunarDate = today.transform(ChineseCalendar.class);
        if (lunarDate.getDayOfYear() == 1) {
            return Optional.of("Today is Chinese New Year. 新年快乐！");
        }
        return Optional.empty();
    }
}
