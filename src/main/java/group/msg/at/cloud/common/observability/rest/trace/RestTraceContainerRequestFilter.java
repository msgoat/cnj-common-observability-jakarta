package group.msg.at.cloud.common.observability.rest.trace;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * {@code JAX-RS ContainerRequestFilter} which traces inbound requests from upstream services or frontends.
 */
@Dependent
@Provider
public class RestTraceContainerRequestFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTraceConstants.REST_TRACE_LOGGER_NAME);

    @Inject
    @ConfigProperty(name = RestTraceConstants.ENABLED_CONFIG_KEY, defaultValue = RestTraceConstants.ENABLED_DEFAULT_VALUE)
    Boolean enabled;

    @Inject
    RestTraceMessageBuilder messageBuilder;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (shouldFilter(requestContext)) {
            StringBuilder traceMessage = new StringBuilder();
            getMessageBuilder().build(traceMessage, requestContext);
            LOGGER.info(traceMessage.toString());
        }
    }

    private boolean shouldFilter(ContainerRequestContext requestContext) {
        return isEnabled() && LOGGER.isInfoEnabled() && !requestContext.getUriInfo().getPath().contains("probes");
    }

    private boolean isEnabled() {
        if (enabled == null) {
            ConfigProvider.getConfig().getOptionalValue(RestTraceConstants.ENABLED_CONFIG_KEY, Boolean.class).ifPresentOrElse(v -> enabled = v, () -> enabled = Boolean.FALSE);
        }
        return enabled;
    }

    private RestTraceMessageBuilder getMessageBuilder() {
        if (messageBuilder == null) {
            throw new IllegalStateException("Missing required reference to RestTraceMessageBuilder! Does your MicroProfile server supports CDI-injection in JAX-RS beans?");
        }
        return messageBuilder;
    }

}
