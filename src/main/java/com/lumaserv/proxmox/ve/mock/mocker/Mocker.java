package com.lumaserv.proxmox.ve.mock.mocker;

import com.lumaserv.proxmox.ve.ProxMoxVEException;
import org.javawebstack.httpclient.HTTPRequest;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class Mocker {

    protected static void throwError(int status, String message) throws ProxMoxVEException {
        throw new ProxMoxVEException(mockHTTPResponse(status, message));
    }

    public static void verifyRequiredParam(String name, Object value) throws ProxMoxVEException {
        if(value == null)
            throw new ProxMoxVEException(mockHTTPResponse(400, "Missing required parameter '" + name + "'"));
    }

    public static HTTPRequest mockHTTPResponse(int status, String statusMessage) {
        return mockHTTPResponse(status, statusMessage, new byte[0]);
    }

    public static HTTPRequest mockHTTPResponse(int status, String statusMessage, String body) {
        return mockHTTPResponse(status, statusMessage, body.getBytes(StandardCharsets.UTF_8));
    }

    public static HTTPRequest mockHTTPResponse(int status, String statusMessage, byte[] body) {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.status()).thenReturn(status);
        when(request.statusMessage()).thenReturn(statusMessage);
        when(request.bytes()).thenReturn(body);
        when(request.string()).thenCallRealMethod();
        when(request.data()).thenCallRealMethod();
        when(request.object(any())).thenCallRealMethod();
        return request;
    }

}
