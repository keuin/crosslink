package com.keuin.psmb4j.util.error;

import java.io.IOException;

public class StringLengthExceededException extends IOException {
    public StringLengthExceededException(long length) {
        super(String.format("String is too long (%d Bytes)", length));
    }
}
