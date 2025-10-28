package ch.sbb.polarion.extension.api_extender.rest.controller;

import ch.sbb.polarion.extension.api_extender.velocity.RegexTool;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Tag(name = "Regex Tool")
@Hidden
@Path("/internal/regex-tool")
public class RegexToolInternalController {

    private final RegexTool regexTool = new RegexTool();

    @POST
    @Path("/find-matches")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find regex matches in text",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Regex matches found",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    )
    public Map<Integer, String[]> findMatches(
            @Parameter(description = "Text to search", required = true) @FormDataParam("text") String text,
            @Parameter(description = "Regular expression", required = true) @FormDataParam("regex") String regex) {
        return regexTool.findMatches(regex, text);
    }

}
