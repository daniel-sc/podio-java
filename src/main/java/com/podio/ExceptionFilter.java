package com.podio;

import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response.Status.Family;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExceptionFilter implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        try {
            if (responseContext.getStatusInfo() == null
                    || responseContext.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
                if (responseContext.hasEntity()) {
                    var errorData = new ObjectMapper().readValue(responseContext.getEntityStream(), Map.class);

                    throw new APIApplicationException(
                            responseContext.getStatusInfo(),
                            (String) errorData.get("error"),
                            (String) errorData.get("error_description"),
                            (Map<String, String>) errorData.get("parameters"));
                } else {
                    throw new APIApplicationException(responseContext.getStatusInfo(), "unknown error", "empty response", new HashMap<>());
                }
            }
        } catch (Exception e) {
            throw new APITransportException(e.getCause());
        }
    }
}
