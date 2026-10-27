package org.faketri.net.socket.mapper;

import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestExceptionMapper {
    private static final Logger log = LoggerFactory.getLogger(RequestExceptionMapper.class);

    private RequestExceptionMapper() {
        /* This utility class should not be instantiated */
    }


    public static <E extends ErrorRequest> ResponseEntry<?> map(E ex) {
        log.error(ex.getMessage());
        return ResponseEntry.builder().code(ex.getCode()).body(ex.getMessage()).build();
    }

    public static ResponseEntry<String> map(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntry.internal("Internal error, please wait.");
    }
}
