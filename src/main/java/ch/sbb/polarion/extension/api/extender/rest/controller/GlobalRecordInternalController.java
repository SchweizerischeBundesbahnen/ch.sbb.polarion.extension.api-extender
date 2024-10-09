package ch.sbb.polarion.extension.api.extender.rest.controller;

import ch.sbb.polarion.extension.api.extender.project.GlobalRecords;
import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.api.extender.settings.GlobalRecordsSettings;
import ch.sbb.polarion.extension.api.extender.settings.GlobalRecordsSettingsModel;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.generic.settings.NamedSettings;
import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import ch.sbb.polarion.extension.generic.settings.SettingId;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@Tag(name = "Global Records")
@Hidden
@Path("/internal")
public class GlobalRecordInternalController {

    protected final PolarionService polarionService;

    public GlobalRecordInternalController() {
        polarionService = new PolarionService();
    }

    @VisibleForTesting
    GlobalRecordInternalController(PolarionService polarionService) {
        this.polarionService = polarionService;
    }

    @GET
    @Path("/records/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns global record value",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the global record value",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = Field.class)
                            )
                    )
            }
    )
    public Field getRecordValue(@PathParam("key") String key) throws JAXBException {
        final GlobalRecords globalRecords = new GlobalRecords();
        Field field = globalRecords.getRecord(key);
        if (field == null) {
            throw new NotFoundException("key '" + key + "' not found");
        } else {
            return field;
        }
    }

    @POST
    @Path("/records/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @SneakyThrows
    @Operation(summary = "Saves global record",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully saved the global record"
                    )
            }
    )
    public void setRecordValue(@PathParam("key") String key, Field field) {
        checkPermissions();

        if (field == null) {
            throw new IllegalArgumentException("value is not provided");
        }

        final GlobalRecords globalRecords = new GlobalRecords();
        globalRecords.setRecord(key, field.getValue());
    }

    @DELETE
    @Path("/records/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Removes global record",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted the global record")
            }
    )
    @SneakyThrows
    public void deleteRecordValue(@PathParam("key") String key) {
        checkPermissions();

        final GlobalRecords globalRecords = new GlobalRecords();
        globalRecords.setRecord(key, null);
    }

    private void checkPermissions() {
        if (!isModificationAllowed()) {
            throw new ForbiddenException("You are not authorized to modify records");
        }
    }

    private boolean isModificationAllowed() {
        String currentUser = polarionService.getSecurityService().getCurrentUser();
        Collection<String> userRoles = polarionService.getSecurityService().getRolesForUser(currentUser);

        GlobalRecordsSettingsModel globalRecordsSettingsModel = (GlobalRecordsSettingsModel) NamedSettingsRegistry.INSTANCE.getByFeatureName(GlobalRecordsSettings.FEATURE_NAME)
                .read(GenericNamedSettings.DEFAULT_SCOPE, SettingId.fromName(NamedSettings.DEFAULT_NAME), null);

        List<String> allowedRoles = globalRecordsSettingsModel.getAllRoles();

        return userRoles.stream()
                .anyMatch(allowedRoles::contains);
    }

}
