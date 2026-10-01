package org.faketri.net.io.dto.response;


import java.io.Serializable;

public sealed interface Response extends Serializable permits AppsResponse, OkResponse, ErrorResponse {
}

