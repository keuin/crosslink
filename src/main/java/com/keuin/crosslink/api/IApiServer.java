package com.keuin.crosslink.api;

import com.keuin.crosslink.api.error.ApiStartupException;

import java.net.InetSocketAddress;

public interface IApiServer {
    /**
     * Start serving the API in other threads.
     * @throws ApiStartupException if failed to startup.
     */
    void startup(InetSocketAddress listen) throws ApiStartupException;

    /**
     * Shutdown the API server.
     * If the API server is not started, this method does nothing.
     */
    void shutdown();
}
