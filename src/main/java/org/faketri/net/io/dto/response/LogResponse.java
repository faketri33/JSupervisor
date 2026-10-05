package org.faketri.net.io.dto.response;

import java.util.Collection;

public record LogResponse(Collection<String> notes) implements Response {}
