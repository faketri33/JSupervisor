package org.faketri.unixsocket.dto.response;


public sealed interface Response permits AppsResponse, OkResponse, ErrorResponse {}

