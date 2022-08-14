package com.keuin.crosslink.messaging.config.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.keuin.crosslink.messaging.action.*;
import com.keuin.crosslink.messaging.config.ConfigSyntaxError;
import com.keuin.crosslink.messaging.endpoint.IEndpoint;
import com.keuin.crosslink.messaging.filter.IFilter;
import com.keuin.crosslink.messaging.filter.ReIdFilter;
import com.keuin.crosslink.messaging.router.IRouterConfigurable;
import com.keuin.crosslink.messaging.rule.IRule;
import com.keuin.crosslink.messaging.rule.ImmutableRule;
import com.keuin.crosslink.messaging.rule.ObjectType;
import com.keuin.crosslink.util.LoggerNaming;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Configure {@link IRouterConfigurable} with supplied JSON config node.
 * The config is typically read from config file.
 * I know this class is compact and harder to maintain... But it works.
 */
public class RouterConfigurer implements IRouterConfigurer {
    private final JsonNode config;
    private static final Logger logger =
            LoggerFactory.getLogger(LoggerNaming.name().of("config").of("router").toString());
    private final List<IRule> prefixRules = new ArrayList<>();

    public RouterConfigurer(@NotNull JsonNode config) {
        Objects.requireNonNull(config);
        this.config = config;
    }

    public RouterConfigurer(@NotNull JsonNode config, List<IRule> prefixRules) {
        Objects.requireNonNull(config);
        this.config = config;
        this.prefixRules.addAll(prefixRules);
    }

    private static class ActionConstructionException extends Exception {
        public ActionConstructionException() {
        }

        public ActionConstructionException(String message) {
            super(message);
        }

        public ActionConstructionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private interface ActionConstructor {
        IAction construct(IRouterConfigurable router, JsonNode jsonNode) throws ActionConstructionException;
    }

    private static final Map<String, ActionConstructor> actionConstructors = new HashMap<>();

    static {
        // register action decoders
        actionConstructors.put("drop", (r, j) -> {
            var iter = j.fieldNames();
            while (iter.hasNext()) {
                var field = iter.next();
                if (!Objects.equals(field, "type")) {
                    throw new ActionConstructionException(String.format("Unnecessary field \"%s\"", field));
                }
            }
            return new DropAction();
        });
        actionConstructors.put("format", (r, j) -> {
            var iter = j.fields();
            var actions = new ArrayList<FormatAction>();
            while (iter.hasNext()) {
                var field = iter.next();
                switch (field.getKey()) {
                    case "type":
                        break;
                    case "color":
                        var text = field.getValue().textValue();
                        var color = NamedTextColor.NAMES.value(text);
                        if (color == null) {
                            throw new ActionConstructionException(String.format("Invalid color \"%s\"", text));
                        }
                        actions.add(new FormatAction(color));
                        break;
                    default:
                        throw new ActionConstructionException(String.format("Invalid field \"%s\"", field.getKey()));
                }
            }
            return IAction.compounded(actions.toArray(new IAction[0]));
        });
        actionConstructors.put("replace", (r, j) -> {
            try {
                String from = null, to = null;
                var iter = j.fields();
                while (iter.hasNext()) {
                    var field = iter.next();
                    switch (field.getKey()) {
                        case "type":
                            break;
                        case "from":
                            from = field.getValue().textValue();
                            break;
                        case "to":
                            to = field.getValue().textValue();
                            break;
                        default:
                            throw new ActionConstructionException(String.format("Invalid field \"%s\"", field.getKey()));
                    }
                }
                if (from == null) throw new ActionConstructionException("Missing field \"from\"");
                if (to == null) throw new ActionConstructionException("Missing field \"to\"");
                var fromPattern = Pattern.compile(from);
                return new Re2placeAction(fromPattern, to);
            } catch (PatternSyntaxException ex) {
                throw new ActionConstructionException("Invalid regexp in field \"from\"", ex);
            }
        });
        actionConstructors.put("filter", (r, j) -> {
            try {
                String pattern = null;
                var iter = j.fields();
                while (iter.hasNext()) {
                    var field = iter.next();
                    switch (field.getKey()) {
                        case "type":
                            break;
                        case "pattern":
                            pattern = field.getValue().textValue();
                            break;
                        default:
                            throw new ActionConstructionException(String.format("Invalid field \"%s\"", field.getKey()));
                    }
                }
                if (pattern == null) throw new ActionConstructionException("Missing field \"pattern\"");
                var p = Pattern.compile(pattern);
                return new ReFilterAction(p);
            } catch (PatternSyntaxException ex) {
                throw new ActionConstructionException("Invalid regexp in field \"from\"", ex);
            }
        });
        actionConstructors.put("route", (r, j) -> {
            try {
                String to = null;
                var backFlow = true; // true by default
                var iter = j.fields();
                while (iter.hasNext()) {
                    var field = iter.next();
                    switch (field.getKey()) {
                        case "type":
                            break;
                        case "to":
                            to = field.getValue().textValue();
                            break;
                        case "backflow":
                            if (field.getValue().isBoolean())
                                backFlow = field.getValue().booleanValue();
                            else
                                throw new ActionConstructionException("Field \"backflow\" expects a boolean value");
                            break;
                        default:
                            throw new ActionConstructionException(String.format("Invalid field \"%s\"", field.getKey()));
                    }
                }
                logger.debug("Read entry: route to \"" + to + "\", backflow=" + backFlow);
                if (to == null) throw new ActionConstructionException("Missing field \"to\"");
                var split = to.split(":");
                if (split.length != 2) throw new ActionConstructionException("Invalid field \"to\": wrong format");
                return new RouteAction(new Supplier<>() {
                    private final String namespace = split[0];
                    private final Pattern idPattern = Pattern.compile(split[1]);

                    @Override
                    public Set<IEndpoint> get() {
                        return r.resolveEndpoints(namespace, idPattern);
                    }

                    @Override
                    public String toString() {
                        return "DestinationResolver{namespace=" + namespace + ", idPattern=" + idPattern + "}";
                    }
                }, backFlow);
            } catch (PatternSyntaxException ex) {
                throw new ActionConstructionException("Invalid regexp in field \"to\"", ex);
            }
        });
    }

    private @NotNull @UnmodifiableView List<IRule> loadRuleChain(@NotNull IRouterConfigurable router,
                                                                 @NotNull JsonNode config)
            throws ConfigSyntaxError {
        Objects.requireNonNull(config);
        if (!config.isArray()) {
            throw new ConfigSyntaxError("Routing rules should be a JSON array");
        }
        var ruleCounter = 1;
        try {
            var ruleList = new ArrayList<IRule>();
            for (var jRule : config) {
                Object object = jRule.get("object");
                Object from = jRule.get("from");
                var jActions = jRule.get("actions");
                if (object == null) throw new ConfigSyntaxError("Missing field \"object\"");
                if (from == null) throw new ConfigSyntaxError("Missing field \"from\"");
                if (jActions == null) throw new ConfigSyntaxError("Missing field \"actions\"");
                object = ((JsonNode) object).textValue();
                from = ((JsonNode) from).textValue();
                if (object == null) throw new ConfigSyntaxError("Invalid field \"object\"");
                if (from == null) throw new ConfigSyntaxError("Invalid field \"from\"");
                object = ObjectType.of((String) object);
                if (object == null)
                    throw new ConfigSyntaxError("Invalid field \"object\", unknown enum");
                var fromFilter = IFilter.fromPatternString((String) from);
                var actionCounter = 1;
                var actions = new ArrayList<IAction>();
                try {
                    for (var jAction : jActions) {
                        if (jAction == null) throw new ConfigSyntaxError("Invalid action %d");
                        var aType = jAction.get("type").textValue(); // action type
                        var constructor = actionConstructors.get(aType);
                        if (constructor == null)
                            throw new ConfigSyntaxError(String.format("Invalid action type: %s", aType));
                        var iAction = constructor.construct(router, jAction);
                        actions.add(iAction);
                        ++actionCounter;
                    }
                } catch (ConfigSyntaxError | ActionConstructionException ex) {
                    throw new ConfigSyntaxError(ex.getMessage() + ", in action " + actionCounter, ex);
                }
                ruleList.add(new ImmutableRule((ObjectType) object, fromFilter, actions));
                logger.debug("Load rule: OBJECT: {} | FROM: {} | ACTION: {}", object, from, jActions);
                ++ruleCounter;
            }
            return Collections.unmodifiableList(ruleList);
        } catch (ReIdFilter.InvalidPatternStringException ex) {
            throw new ConfigSyntaxError(ex);
        } catch (ConfigSyntaxError ex) {
            throw new ConfigSyntaxError(ex.getMessage() + ", in rule " + ruleCounter, ex);
        }
    }

    @Override
    public void configure(IRouterConfigurable router) throws JsonProcessingException, ConfigSyntaxError {
        var rules = new ArrayList<>(prefixRules);
        rules.addAll(loadRuleChain(router, config));
        router.clearEndpoints();
        router.updateRuleChain(rules);
    }
}
