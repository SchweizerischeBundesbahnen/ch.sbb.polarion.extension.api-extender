package ch.sbb.polarion.extension.api_extender.settings;

import ch.sbb.polarion.extension.generic.settings.AuthorizationModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Who may write global records. Global records belong to the repository, not to a project, so only
 * global roles are stored - the project roles the shared model carries are deliberately left out of
 * both the stored data and the permission check.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalRecordsSettingsModel extends AuthorizationModel {

    @Override
    protected String serializeModelData() {
        return serializeEntry(GLOBAL_ROLES, serializeRoles(globalRoles));
    }

    @Override
    protected void deserializeModelData(String serializedString) {
        globalRoles = deserializeRoles(GLOBAL_ROLES, serializedString);
    }

    @Override
    @JsonIgnore
    public List<String> getAllRoles() {
        return globalRoles == null ? List.of() : globalRoles;
    }
}
