package org.faketri.net.io.dto.response;

import org.faketri.net.EStatusCode;

import java.io.Serializable;

public record ResponseEntry<T>(EStatusCode code, T body) implements Serializable {

    public static <T> ResponseEntry<T> ok() {
        return new ResponseEntry<>(EStatusCode.OK, null);
    }

    public static <T> ResponseEntry<T> ok(T body) {
        return new ResponseEntry<>(EStatusCode.OK, body);
    }

    public static <T> ResponseEntry<T> badRequest(T body) {
        return new ResponseEntry<>(EStatusCode.BAD_REQUEST, body);
    }

    public static <T> ResponseEntry<T> badRequest() {
        return new ResponseEntry<>(EStatusCode.BAD_REQUEST, null);
    }

    public static <T> ResponseEntry<T> internal() {
        return new ResponseEntry<>(EStatusCode.INTERNAL, null);
    }

    public static <T> ResponseEntry<T> internal(T body) {
        return new ResponseEntry<>(EStatusCode.INTERNAL, body);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private EStatusCode code;
        private T body;

        public Builder<T> code(EStatusCode code) {
            this.code = code;
            return this;
        }

        public Builder<T> body(T body) {
            this.body = body;
            return this;
        }

        public ResponseEntry<T> build() {
            return new ResponseEntry<>(code, body);
        }
    }
}
