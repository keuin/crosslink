package com.keuin.crosslink.messaging.endpoint.remote.psmb;

import org.slf4j.Logger;

import static com.keuin.crosslink.messaging.endpoint.remote.psmb.PsmbEndpoint.RETRY_INTERVAL_MILLIS;

public class Util {
    public static void SleepBeforeReconnect(Logger logger) throws InterruptedException {
        String time;
        if (RETRY_INTERVAL_MILLIS > 1000) {
            time = String.format("%.1fs", RETRY_INTERVAL_MILLIS / 1000.0);
        } else {
            time = String.format("%dms", RETRY_INTERVAL_MILLIS);
        }
        logger.info("Wait for {} before reconnecting.", time);
        Thread.sleep(RETRY_INTERVAL_MILLIS);
    }
}
