package org.faketri.net.handlers;

import org.faketri.net.io.dto.request.ProcessedRequest;
import org.faketri.net.io.dto.request.Request;


public interface RequestProcessing {
    ProcessedRequest process(Request request);
}
