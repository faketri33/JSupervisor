package org.faketri.net.handlers;

import org.faketri.net.io.dto.request.DispatchableRequest;
import org.faketri.net.io.dto.request.InternalRequest;


public interface RequestProcessing {
    DispatchableRequest process(InternalRequest request);
}
