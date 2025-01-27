package ch.sbb.polarion.extension.api_extender.rest.controller;

import ch.sbb.polarion.extension.api_extender.rest.model.Field;
import ch.sbb.polarion.extension.generic.rest.filter.Secured;

import javax.ws.rs.Path;

@Secured
@Path("/api")
public class ProjectCustomFieldApiController extends ProjectCustomFieldInternalController {

    @Override
    public Field getCustomValue(String projectId, String key) {
        return polarionService.callPrivileged(() -> super.getCustomValue(projectId, key));
    }

    @Override
    public void setCustomValue(String projectId, String key, Field field) {
        polarionService.callPrivileged(() -> super.setCustomValue(projectId, key, field));
    }

    @Override
    public void deleteCustomValue(String projectId, String key) {
        polarionService.callPrivileged(() -> super.deleteCustomValue(projectId, key));
    }

}
