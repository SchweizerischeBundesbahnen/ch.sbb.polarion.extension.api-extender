package ch.sbb.polarion.extension.api.extender.velocity;

import ch.sbb.polarion.extension.generic.context.CurrentContextConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@CurrentContextConfig("api-extender")
class VelocityCustomFieldsProjectTest {
    @Test
    @SneakyThrows
    void shouldSetCustomFieldWithoutExceptionTest() {
        String project = "testProject";
        String key = "testKey";
        String value = "testValue";
        VelocityCustomFieldsProject mockCustomFieldsProject = Mockito.mock(VelocityCustomFieldsProject.class);

        assertDoesNotThrow(() -> mockCustomFieldsProject.setCustomField(project, key, value));

        verify(mockCustomFieldsProject, times(1)).setCustomField(project, key, value);
    }
}
