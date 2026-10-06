package org.faketri.net.io.json;

import org.faketri.net.io.json.exception.JsonProcessingException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;


public class JsonObjectMapper {
    private final JsonMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public <O> O decode(byte[] frame, Class<O> targetClass) throws JsonProcessingException {
        try {
            return mapper.readValue(frame, targetClass);
        } catch (JacksonException ex) {
            throw new JsonProcessingException(ex.getMessage());
        }
    }


    public <I> byte[] encode(I response) throws JsonProcessingException {
        try {
            return mapper.writeValueAsBytes(response);
        } catch (JacksonException ex) {
            throw new JsonProcessingException(ex.getMessage());
        }
    }
}
