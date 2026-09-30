package org.faketri.net.socket.dto.response;


import java.io.Serializable;

public sealed interface Response extends Serializable permits AppsResponse, OkResponse, ErrorResponse {
}

