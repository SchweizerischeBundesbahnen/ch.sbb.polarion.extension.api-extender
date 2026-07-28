package ch.sbb.polarion.extension.api_extender.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Schema(description = "Available global and project roles for the current scope, used to configure who may write")
public record RolesInfo(
        @Schema(description = "Global roles", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull List<String> globalRoles,
        @Schema(description = "Project roles (empty when the scope is not a project)", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull List<String> projectRoles) {
}
