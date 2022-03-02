package group.msg.at.cloud.common.observability.rest.trace;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * {@code JAX-RS ContainerResponseFilter} which traces outbound responses to upstream services or frontends.
 * <p>
 * In contrast to ClientRequestFilters both CDI injection and automatic detection works well for ContainerRequestFilters
 * on all MicroProfile capable application servers.
 * </p>
 */
@Provider
public class RestTraceContainerResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTraceConstants.REST_TRACE_LOGGER_NAME);

    @Inject
    @ConfigProperty(name = RestTraceConstants.ENABLED_CONFIG_KEY, defaultValue = RestTraceConstants.ENABLED_DEFAULT_VALUE)
    Boolean enabled;

    @Inject
    RestTraceMessageBuilder messageBuilder;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (shouldFilter(requestContext, responseContext)) {
            StringBuilder traceMessage = new StringBuilder();
            getMessageBuilder().build(traceMessage, requestContext, responseContext);
            LOGGER.info(traceMessage.toString());
        }
    }

    private boolean shouldFilter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        return isEnabled() && LOGGER.isInfoEnabled() && !requestContext.getUriInfo().getPath().contains("probes");
    }

    private boolean isEnabled() {
        if (enabled == null) {
            ConfigProvider.getConfig().getOptionalValue(RestTraceConstants.ENABLED_CONFIG_KEY, Boolean.class).ifPresentOrElse(v -> enabled = v, () -> enabled = Boolean.FALSE);
        }
        return enabled;
    }

    private RestTraceMessageBuilder getMessageBuilder() {
        if (this.messageBuilder == null) {
            throw new IllegalStateException("Missing required reference to RestTraceMessageBuilder! Does your MicroProfile server supports CDI-injection in JAX-RS beans?");
        }
        return this.messageBuilder;
    }
}
