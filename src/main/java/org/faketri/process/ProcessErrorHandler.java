package org.faketri.process;


@FunctionalInterface
public interface ProcessErrorHandler {

    void handelException(Throwable ex);
}
