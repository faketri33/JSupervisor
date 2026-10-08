package org.faketri.net.socket.mapper;

import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestExceptionMapper {
    private static final Logger log = LoggerFactory.getLogger(RequestExceptionMapper.class);

    private RequestExceptionMapper() {
        /* This utility class should not be instantiated */
    }


    public static <T extends ErrorRequest> ResponseEntry<?> map(T ex) {
        log.error(ex.toString());
        return ResponseEntry.builder().code(ex.getCode()).body(ex.getMessage()).build();
    }

    public static ResponseEntry<?> map(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntry.internal(ex.getMessage());
    }
}
