package group.msg.at.cloud.common.observability.rest.trace;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * {@code JAX-RS ClientRequestFilter} which traces outbound requests to downstream services.
 * <p>
 * Since Payara (5.2020) does not handle CDI-Injections into ClientRequestFilters registered via {@code @RegisterProvider}
 * on MicroProfile REST clients very well, the actual MicroProfile configuration values must be looked up programmatically.
 * </p>
 * <p>
 * Quarkus (1.5) complains about CDI injection into JAX-RS providers but supports it nevertheless.
 * </p>
 * <p>
 * <strong>Attention:</strong> With Payara, this {@code ClientRequestFilter} has to be registered explicitly on
 * MicroProfile REST clients to be actually applied to REST client invocations. Quarkus picks all ClientRequestFilters
 * automatically.
 * </p>
 */
@Provider
@Priority(Priorities.USER)
public class RestTraceClientRequestFilter implements ClientRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTraceConstants.REST_TRACE_LOGGER_NAME);

    Boolean enabled;

    @Inject
    RestTraceMessageBuilder messageBuilder;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (shouldFilter(requestContext)) {
            StringBuilder traceMessage = new StringBuilder();
            getMessageBuilder().build(traceMessage, requestContext);
            LOGGER.info(traceMessage.toString());
        }
    }

    private boolean shouldFilter(ClientRequestContext requestContext) {
        return isEnabled() && LOGGER.isInfoEnabled();
    }

    private boolean isEnabled() {
        if (enabled == null) {
            ConfigProvider.getConfig().getOptionalValue(RestTraceConstants.ENABLED_CONFIG_KEY, Boolean.class).ifPresentOrElse(v -> enabled = v, () -> enabled = Boolean.FALSE);
        }
        return enabled;
    }

    private RestTraceMessageBuilder getMessageBuilder() {
        if (messageBuilder == null) {
            messageBuilder = new DefaultRestTraceMessageBuilder();
        }
        return messageBuilder;
    }
}
