package com.keuin.crosslink.plugin.common.module;

import com.google.inject.AbstractModule;
import com.keuin.crosslink.messaging.history.HistoricMessageRecorder;
import com.keuin.crosslink.messaging.history.IHistoricMessageRecorder;

public class CommonHistoricMessageRecorderModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IHistoricMessageRecorder.class).to(HistoricMessageRecorder.class);
    }
}
