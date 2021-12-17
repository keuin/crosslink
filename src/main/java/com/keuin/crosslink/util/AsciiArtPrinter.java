package com.keuin.crosslink.util;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

final class AsciiArtPrinter {
    private static final Random random = new Random();
    private static final List<List<String>> ASCII_ARTS = List.of(
            List.of(
                    " _____                    _     _       _    ",
                    "/  __ \\                  | |   (_)     | |   ",
                    "| /  \\/_ __ ___  ___ ___ | |    _ _ __ | | __",
                    "| |   | '__/ _ \\/ __/ __|| |   | | '_ \\| |/ /",
                    "| \\__/\\ | | (_) \\__ \\__ \\| |___| | | | |   < ",
                    " \\____/_|  \\___/|___/___/\\_____/_|_| |_|_|\\_\\"
            ),
            List.of(
                    "   ___                     __ _       _    ",
                    "  / __\\ __ ___  ___ ___   / /(_)_ __ | | __",
                    " / / | '__/ _ \\/ __/ __| / / | | '_ \\| |/ /",
                    "/ /__| | | (_) \\__ \\__ \\/ /__| | | | |   < ",
                    "\\____/_|  \\___/|___/___/\\____/_|_| |_|_|\\_\\"
            )
    );


    public static void print(Consumer<String> linePrinter) {
        linePrinter.accept("");
        ASCII_ARTS.get(random.nextInt(ASCII_ARTS.size())).forEach(linePrinter);
        linePrinter.accept("");
    }
}
