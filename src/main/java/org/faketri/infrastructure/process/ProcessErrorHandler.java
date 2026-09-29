package org.faketri.infrastructure.process;


@FunctionalInterface
public interface ProcessErrorHandler {

    void handelException(Throwable ex);
}
