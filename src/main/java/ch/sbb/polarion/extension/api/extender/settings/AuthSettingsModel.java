package ch.sbb.polarion.extension.api.extender.settings;

import ch.sbb.polarion.extension.generic.settings.SettingsModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.polarion.core.util.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("squid:S2160") //ignore suggestion to create equals() - parent's implementation is sufficient by design
public abstract class AuthSettingsModel extends SettingsModel {

    public static final String GLOBAL_ROLES = "globalRoles";
    public static final String PROJECT_ROLES = "projectRoles";

    @Getter
    protected List<String> globalRoles;
    @Getter
    protected List<String> projectRoles;

    public void setGlobalRoles(String... roles) {
        globalRoles = Arrays.asList(roles);
    }

    public void setProjectRoles(String... roles) {
        projectRoles = Arrays.asList(roles);
    }

    @JsonIgnore
    public abstract List<String> getAllRoles();

    @NotNull
    protected String serializeRoles(@Nullable List<String> roles) {
        return roles == null ? "" : String.join(",", roles);
    }

    @NotNull
    protected List<String> deserializeRoles(@NotNull String what, @NotNull String serializedString) {
        final String roles = deserializeEntry(what, serializedString);
        return Arrays.stream(roles.split(",")).filter(s -> !StringUtils.isEmpty(s)).map(String::trim).toList();
    }

}
