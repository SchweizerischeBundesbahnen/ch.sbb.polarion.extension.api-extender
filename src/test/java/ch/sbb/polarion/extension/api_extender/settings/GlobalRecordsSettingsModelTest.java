package ch.sbb.polarion.extension.api_extender.settings;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalRecordsSettingsModelTest {

    @Test
    void testDeserializeRoles() {
        GlobalRecordsSettingsModel model = new GlobalRecordsSettingsModel();
        model.setGlobalRoles("rolesPlaceholderToReplace");
        String serializedString = model.serializeModelData();

        model.deserialize(serializedString.replace("rolesPlaceholderToReplace", " role1,, \t  \n    role2   \n"));
        List<String> roles = model.getGlobalRoles();
        assertEquals(2, roles.size());
        assertTrue(roles.containsAll(Arrays.asList("role1", "role2")));
    }
}
