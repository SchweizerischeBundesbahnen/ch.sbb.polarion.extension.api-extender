package ch.sbb.polarion.extension.api_extender.settings;

import ch.sbb.polarion.extension.generic.settings.AuthorizationModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Who may write global records. Global records belong to the repository, not to a project, so only
 * global roles are stored - the project roles the shared model carries are deliberately left out of
 * both the stored data and the permission check.
 * <p>
 * Equality comes from {@link AuthorizationModel}: this class overrides behaviour but adds no state,
 * so generating it here would only add a type check nothing asks for - the settings framework keys
 * models by feature name and never compares two of them.
 */
@NoArgsConstructor
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
