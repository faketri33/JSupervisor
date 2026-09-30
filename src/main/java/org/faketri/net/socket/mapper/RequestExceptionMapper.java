package org.faketri.net.socket.mapper;

import org.faketri.infrastructure.exceptions.unixserver.ErrorRequest;
import org.faketri.net.socket.EStatusCode;
import org.faketri.net.socket.dto.response.ErrorResponse;
import org.faketri.net.socket.dto.response.Response;

public class RequestExceptionMapper {

    public static <T extends ErrorRequest> Response map(T ex) {
        return new ErrorResponse(ex.getCode(), ex.getMessage());
    }

    public static Response map(Exception ex) {
        return new ErrorResponse(EStatusCode.INTERNAL.name(), ex.getMessage());
    }
}
