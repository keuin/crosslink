package com.keuin.crosslink.messaging.config.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.keuin.crosslink.messaging.config.ConfigSyntaxError;
import com.keuin.crosslink.messaging.router.IRouterConfigurable;

public interface IRouterConfigurer {
    /**
     * Parse and configure the router with internal configuration string.
     * @throws JsonProcessingException cannot parse JSON string.
     * @throws ConfigSyntaxError config content is invalid.
     */
    void configure(IRouterConfigurable router) throws JsonProcessingException, ConfigSyntaxError;

//    @NotNull IRouterConfigurable getRouter();


}
