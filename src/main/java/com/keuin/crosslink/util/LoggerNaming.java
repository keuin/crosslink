package com.keuin.crosslink.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoggerNaming {
    public static class NamedNode {
        private final List<String> names;

        public NamedNode(List<String> previousNames) {
            Objects.requireNonNull(previousNames);
            this.names = List.copyOf(previousNames);
        }

        public NamedNode of(String sectionName) {
            var list = new ArrayList<>(names);
            list.add(sectionName);
            return new NamedNode(list);
        }

        @Override
        public String toString() {
            return String.join(".", names);
        }
    }

    public static NamedNode name() {
        return new NamedNode(List.of("crosslink"));
    }
}
