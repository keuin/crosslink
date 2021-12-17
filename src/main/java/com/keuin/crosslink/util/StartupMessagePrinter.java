package com.keuin.crosslink.util;

import net.time4j.SystemClock;

import java.util.function.Consumer;

public final class StartupMessagePrinter {
    public static void print(Consumer<String> linePrinter, String mode) {
        AsciiArtPrinter.print(linePrinter);
        linePrinter.accept(String.format("CrossLink is loading in %s mode.", mode));
        EggFactory.getEgg(SystemClock.inLocalView().today()).ifPresent(linePrinter);
    }
}
