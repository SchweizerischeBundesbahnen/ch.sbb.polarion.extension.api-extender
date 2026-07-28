package ch.sbb.polarion.extension.api_extender.rest.controller;

import ch.sbb.polarion.extension.api_extender.rest.model.RolesInfo;
import ch.sbb.polarion.extension.api_extender.util.RolesUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;

/**
 * Lists the roles the authorization settings pages offer as checkboxes.
 * <p>
 * The pages used to be JSPs that called {@link RolesUtils} in a scriptlet while rendering. Their React
 * replacements cannot, so the same two calls are exposed here. Both settings - project custom fields
 * and global records - grant the same kinds of role, hence one endpoint rather than one per setting.
 */
@Tag(name = "Authorization")
@Path("/internal")
public class RolesInternalController {

    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the global and project roles available in the specified scope",
            responses = @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved the available roles",
                    content = @Content(schema = @Schema(implementation = RolesInfo.class))))
    public RolesInfo listRoles(
            @Parameter(description = "Scope, e.g. project/<projectId>/ (empty for global scope)") @QueryParam("scope") String scope) {
        return new RolesInfo(new ArrayList<>(RolesUtils.getGlobalRoles()), new ArrayList<>(RolesUtils.getProjectRoles(scope)));
    }
}
