package ch.sbb.polarion.extension.api_extender.rest;

import ch.sbb.polarion.extension.generic.context.CurrentContextExtension;
import ch.sbb.polarion.extension.generic.rest.controller.roles.RolesApiController;
import ch.sbb.polarion.extension.generic.rest.controller.roles.RolesInternalController;
import ch.sbb.polarion.extension.generic.test_extensions.PlatformContextMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({MockitoExtension.class, CurrentContextExtension.class, PlatformContextMockExtension.class})
class ApiExtenderRestApplicationTest {

    @Test
    void testConstructor() {
        ApiExtenderRestApplication application = new ApiExtenderRestApplication();
        assertDoesNotThrow(application::getExtensionControllerClasses);
    }

    @Test
    void testRolesEndpointsAreRegistered() {
        // Generic serves the role endpoints only to the extensions that name them, and both
        // administration pages fill their checkboxes from /roles - dropping either class blanks them.
        Set<Class<?>> controllers = new ApiExtenderRestApplication().getExtensionControllerClasses();

        assertTrue(controllers.contains(RolesInternalController.class));
        assertTrue(controllers.contains(RolesApiController.class));
    }

}
