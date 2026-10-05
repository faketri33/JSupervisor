package org.faketri.net.socket.mapper;

import org.faketri.infrastructure.exceptions.unixserver.ErrorRequest;
import org.faketri.net.EStatusCode;
import org.faketri.net.io.dto.response.ErrorResponse;
import org.faketri.net.io.dto.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestExceptionMapper {
    private static final Logger log = LoggerFactory.getLogger(RequestExceptionMapper.class);

    private RequestExceptionMapper() {
        /* This utility class should not be instantiated */
    }


    public static <T extends ErrorRequest> Response map(T ex) {
        log.error(ex.toString());
        return new ErrorResponse(ex.getCode(), ex.getMessage());
    }

    public static Response map(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ErrorResponse(EStatusCode.INTERNAL.name(), ex.getMessage());
    }
}
