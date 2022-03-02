package group.msg.at.cloud.common.observability.rest.trace;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;

/**
 * {@code Builder} for REST request and response trace messages.
 */
public interface RestTraceMessageBuilder {

    /**
     * Builds a trace message for the given outgoing request.
     *
     * @param traceMessage string builder receiving the generated message
     * @param request      outgoing request
     */
    void build(@NotNull StringBuilder traceMessage, @NotNull ClientRequestContext request);

    /**
     * Builds a trace message for the given incoming request.
     *
     * @param traceMessage string builder receiving the generated message
     * @param request      incoming request
     */
    void build(StringBuilder traceMessage, ContainerRequestContext request);

    /**
     * Builds a trace message for the given incoming response.
     *
     * @param traceMessage string builder receiving the generated message
     * @param request      outgoing request
     * @param response     incoming response
     */
    void build(StringBuilder traceMessage, ClientRequestContext request, ClientResponseContext response);

    /**
     * Builds a trace message for the given outgoing response.
     *
     * @param traceMessage string builder receiving the generated message
     * @param request      incoming request
     * @param response     outgoing response
     */
    void build(StringBuilder traceMessage, ContainerRequestContext request, ContainerResponseContext response);
}
