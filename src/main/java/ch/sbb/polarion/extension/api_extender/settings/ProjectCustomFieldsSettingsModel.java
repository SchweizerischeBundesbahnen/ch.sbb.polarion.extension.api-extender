package ch.sbb.polarion.extension.api_extender.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectCustomFieldsSettingsModel extends AuthSettingsModel {

    @Override
    protected String serializeModelData() {
        return serializeEntry(GLOBAL_ROLES, serializeRoles(globalRoles)) +
                serializeEntry(PROJECT_ROLES, serializeRoles(projectRoles));
    }

    @Override
    protected void deserializeModelData(String serializedString) {
        globalRoles = deserializeRoles(GLOBAL_ROLES, serializedString);
        projectRoles = deserializeRoles(PROJECT_ROLES, serializedString);
    }

    @JsonIgnore
    public List<String> getAllRoles() {
        List<String> roles = new ArrayList<>();
        roles.addAll(getGlobalRoles());
        roles.addAll(getProjectRoles());
        return roles;
    }

}
