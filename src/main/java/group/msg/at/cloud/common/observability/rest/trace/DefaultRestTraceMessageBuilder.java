package group.msg.at.cloud.common.observability.rest.trace;

import jakarta.inject.Singleton;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of a {@code RestTraceMessageBuilder}.
 */
@Singleton
public class DefaultRestTraceMessageBuilder implements RestTraceMessageBuilder {

    private static final Set<String> CONFIDENTIAL_HEADER_NAMES = Set.of("Authorization", "authorization");

    @Override
    public void build(StringBuilder traceMessage, ClientRequestContext request) {
        traceMessage.append("*** REST REQUEST OUT *** { ");
        appendRequest(traceMessage, request, true);
        traceMessage.append(" }");
    }

    @Override
    public void build(StringBuilder traceMessage, ContainerRequestContext request) {
        traceMessage.append("*** REST REQUEST IN *** { ");
        appendRequest(traceMessage, request, true);
        traceMessage.append(" }");
    }

    @Override
    public void build(StringBuilder traceMessage, ClientRequestContext request, ClientResponseContext response) {
        traceMessage.append("*** REST RESPONSE IN *** { ");
        appendRequest(traceMessage, request, false);
        traceMessage.append(", ");
        appendResponse(traceMessage, response);
        traceMessage.append(" }");
    }

    @Override
    public void build(StringBuilder traceMessage, ContainerRequestContext request, ContainerResponseContext response) {
        traceMessage.append("*** REST RESPONSE OUT *** { ");
        appendRequest(traceMessage, request, false);
        traceMessage.append(", ");
        appendResponse(traceMessage, response);
        traceMessage.append(" }");
    }

    private void appendRequest(StringBuilder traceMessage, ClientRequestContext request, boolean withHeaders) {
        traceMessage.append("request : { ");
        traceMessage.append("uri : \"").append(request.getUri()).append("\"");
        traceMessage.append(", method : \"").append(request.getMethod()).append("\"");
        if (withHeaders) {
            traceMessage.append(", ");
            appendTypedHeaders(traceMessage, request.getHeaders());
        }
        traceMessage.append(" }");
    }

    private void appendRequest(StringBuilder traceMessage, ContainerRequestContext request, boolean withHeaders) {
        traceMessage.append("request : { ");
        traceMessage.append("uri : \"").append(request.getUriInfo().getRequestUri()).append("\"");
        traceMessage.append(", method : \"").append(request.getMethod()).append("\"");
        if (withHeaders) {
            traceMessage.append(", ");
            appendStringHeaders(traceMessage, request.getHeaders());
        }
        traceMessage.append(" }");
    }

    private void appendResponse(StringBuilder traceMessage, ClientResponseContext response) {
        traceMessage.append("response { ");
        traceMessage.append("statusCode : ").append(response.getStatus());
        String statusText = response.getStatusInfo().getReasonPhrase();
        if (statusText != null && !statusText.isEmpty()) {
            traceMessage.append(", statusText : \"").append(statusText).append("\"");
        }
        traceMessage.append(", ");
        appendStringHeaders(traceMessage, response.getHeaders());
        traceMessage.append(" }");
    }

    private void appendResponse(StringBuilder traceMessage, ContainerResponseContext response) {
        traceMessage.append("response { ");
        traceMessage.append("statusCode : ").append(response.getStatus());
        traceMessage.append(", ");
        appendStringHeaders(traceMessage, response.getStringHeaders());
        traceMessage.append(" }");
    }

    private void appendStringHeaders(StringBuilder traceEntry, MultivaluedMap<String, String> headers) {
        traceEntry.append("headers : { ");
        int headerIndex = 0;
        for (Map.Entry<String, List<String>> currentHeader : headers.entrySet()) {
            if (headerIndex > 0) {
                traceEntry.append(", ");
            }
            traceEntry.append(currentHeader.getKey()).append(" : ");
            if (currentHeader.getValue().size() == 1) {
                traceEntry.append("\"").append(filterConfidentialHeaderValue(currentHeader.getKey(), currentHeader.getValue().get(0))).append("\"");
            } else {
                traceEntry.append("[");
                int valueIndex = 0;
                for (String currentHeaderValue : currentHeader.getValue()) {
                    if (valueIndex > 0) {
                        traceEntry.append(", ");
                    }
                    traceEntry.append("\"").append(filterConfidentialHeaderValue(currentHeader.getKey(), currentHeaderValue)).append("\"");
                    valueIndex++;
                }
                traceEntry.append("]");
            }
            headerIndex++;
        }
        traceEntry.append(" }");
    }

    private void appendTypedHeaders(StringBuilder traceEntry, MultivaluedMap<String, Object> headers) {
        traceEntry.append("headers : { ");
        int headerIndex = 0;
        for (Map.Entry<String, List<Object>> currentHeader : headers.entrySet()) {
            if (headerIndex > 0) {
                traceEntry.append(", ");
            }
            traceEntry.append(currentHeader.getKey()).append(" : ");
            if (currentHeader.getValue().size() == 1) {
                traceEntry.append("\"").append(filterConfidentialHeaderValue(currentHeader.getKey(), currentHeader.getValue().get(0).toString())).append("\"");
            } else {
                traceEntry.append("[");
                int valueIndex = 0;
                for (Object currentHeaderValue : currentHeader.getValue()) {
                    if (valueIndex > 0) {
                        traceEntry.append(", ");
                    }
                    traceEntry.append("\"").append(filterConfidentialHeaderValue(currentHeader.getKey(), currentHeaderValue.toString())).append("\"");
                    valueIndex++;
                }
                traceEntry.append("]");
            }
            headerIndex++;
        }
        traceEntry.append(" }");
    }

    private String filterConfidentialHeaderValue(String headerName, String headerValue) {
        return CONFIDENTIAL_HEADER_NAMES.contains(headerName) ? String.format("_redacted(%d)_", headerValue.length()) : headerValue;
    }
}
