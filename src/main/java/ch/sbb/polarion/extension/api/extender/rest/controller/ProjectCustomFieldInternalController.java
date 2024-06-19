package ch.sbb.polarion.extension.api.extender.rest.controller;

import ch.sbb.polarion.extension.api.extender.project.CustomFieldsProject;
import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.api.extender.settings.ProjectCustomFieldsSettings;
import ch.sbb.polarion.extension.api.extender.settings.ProjectCustomFieldsSettingsModel;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import ch.sbb.polarion.extension.generic.settings.NamedSettings;
import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import ch.sbb.polarion.extension.generic.settings.SettingId;
import ch.sbb.polarion.extension.generic.util.ScopeUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.jetbrains.annotations.VisibleForTesting;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import java.util.Collection;
import java.util.List;

@Tag(name = "Project custom fields")
@Hidden
@Path("/internal")
public class ProjectCustomFieldInternalController {

    protected final PolarionService polarionService;

    public ProjectCustomFieldInternalController() {
        this.polarionService = new PolarionService();
    }

    @VisibleForTesting
    ProjectCustomFieldInternalController(PolarionService polarionService) {
        this.polarionService = polarionService;
    }

    @Operation(summary = "Returns custom field value")
    @GET
    @Path("/projects/{projectId}/keys/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Field getCustomValue(@PathParam("projectId") String projectId, @PathParam("key") String key) throws JAXBException {
        final CustomFieldsProject customFieldsProject = new CustomFieldsProject(projectId);
        Field field = customFieldsProject.getCustomField(key);
        if (field == null) {
            throw new NotFoundException("key '" + key + "' for project '" + projectId + "' not found");
        } else {
            return field;
        }
    }

    @Operation(summary = "Saves custom field")
    @POST
    @Path("/projects/{projectId}/keys/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @SneakyThrows
    public void setCustomValue(@PathParam("projectId") String projectId, @PathParam("key") String key, Field field) {
        checkPermissions(projectId);

        if (field == null) {
            throw new IllegalArgumentException("value is not provided");
        }

        final CustomFieldsProject customFieldsProject = new CustomFieldsProject(projectId);
        customFieldsProject.setCustomField(key, field.getValue());
    }

    @Operation(summary = "Removes custom field")
    @DELETE
    @Path("/projects/{projectId}/keys/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @SneakyThrows
    public void deleteCustomValue(@PathParam("projectId") String projectId, @PathParam("key") String key) {
        checkPermissions(projectId);

        final CustomFieldsProject customFieldsProject = new CustomFieldsProject(projectId);
        customFieldsProject.setCustomField(key, null);
    }

    private void checkPermissions(String projectId) {
        String scope = ScopeUtils.getScopeFromProject(projectId);
        if (!isModificationAllowed(scope)) {
            throw new ForbiddenException("You are not authorized to modify custom fields");
        }
    }

    private boolean isModificationAllowed(String scope) {
        String currentUser = polarionService.getSecurityService().getCurrentUser();
        Collection<String> userRoles = polarionService.getSecurityService().getRolesForUser(currentUser);

        ProjectCustomFieldsSettingsModel projectCustomFieldsSettingsModel = (ProjectCustomFieldsSettingsModel)
                NamedSettingsRegistry.INSTANCE.getByFeatureName(ProjectCustomFieldsSettings.FEATURE_NAME).read(
                        scope, SettingId.fromName(NamedSettings.DEFAULT_NAME), null);

        List<String> allowedRoles = projectCustomFieldsSettingsModel.getAllRoles();

        return userRoles.stream()
                .anyMatch(allowedRoles::contains);
    }

}
