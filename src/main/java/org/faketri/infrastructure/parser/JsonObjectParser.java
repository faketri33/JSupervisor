package org.faketri.infrastructure.parser;

import org.faketri.infrastructure.exceptions.unixserver.BadRequestException;
import org.faketri.infrastructure.exceptions.unixserver.JsonProcessingException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;


public class JsonObjectParser {
    private final JsonMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public <O> O decode(byte[] frame, Class<O> targetClass) throws BadRequestException {
        return mapper.readValue(frame, targetClass);
    }


    public <I> byte[] encode(I response) throws JsonProcessingException {
        return mapper.writeValueAsBytes(response);
    }
}
