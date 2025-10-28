package ch.sbb.polarion.extension.api_extender.rest;

import ch.sbb.polarion.extension.generic.context.CurrentContextExtension;
import ch.sbb.polarion.extension.generic.test_extensions.PlatformContextMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith({MockitoExtension.class, CurrentContextExtension.class, PlatformContextMockExtension.class})
class ApiExtenderRestApplicationTest {

    @Test
    void testConstructor() {
        ApiExtenderRestApplication application = new ApiExtenderRestApplication();
        assertDoesNotThrow(application::getExtensionControllerSingletons);
    }

}
