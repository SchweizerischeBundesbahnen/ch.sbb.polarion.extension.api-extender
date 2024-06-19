package ch.sbb.polarion.extension.api.extender.rest.controller;

import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.generic.rest.filter.Secured;

import javax.ws.rs.Path;

@Secured
@Path("/api")
public class GlobalRecordApiController extends GlobalRecordInternalController {

    @Override
    public Field getRecordValue(String key) {
        return polarionService.callPrivileged(() -> super.getRecordValue(key));
    }

    @Override
    public void setRecordValue(String key, Field field) {
        polarionService.callPrivileged(() -> super.setRecordValue(key, field));
    }

    @Override
    public void deleteRecordValue(String key) {
        polarionService.callPrivileged(() -> super.deleteRecordValue(key));
    }

}
