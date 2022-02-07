package com.keuin.crosslink.messaging.config.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.keuin.crosslink.messaging.config.ConfigSyntaxError;
import com.keuin.crosslink.messaging.router.IRouterConfigurable;

public interface IRouterConfigurer {
    /**
     * Parse and configure the router with internal configuration string.
     * All existing endpoints and rule chains will be removed.
     * @throws JsonProcessingException cannot parse JSON string.
     * @throws ConfigSyntaxError config content is invalid.
     */
    void configure(IRouterConfigurable router) throws JsonProcessingException, ConfigSyntaxError;

//    @NotNull IRouterConfigurable getRouter();


}
