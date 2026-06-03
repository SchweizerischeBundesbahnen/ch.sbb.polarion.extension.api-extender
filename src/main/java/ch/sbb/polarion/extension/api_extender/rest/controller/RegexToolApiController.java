package ch.sbb.polarion.extension.api_extender.rest.controller;

import ch.sbb.polarion.extension.generic.rest.filter.Secured;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

@Singleton
@Secured
@Path("/api/regex-tool")
public class RegexToolApiController extends RegexToolInternalController {

}
