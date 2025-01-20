package ch.sbb.polarion.extension.api.extender.velocity;

import ch.sbb.polarion.extension.api.extender.project.GlobalRecords;
import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.generic.context.CurrentContextConfig;
import ch.sbb.polarion.extension.generic.rest.model.Version;
import ch.sbb.polarion.extension.generic.test_extensions.PlatformContextMockExtension;
import ch.sbb.polarion.extension.generic.util.VersionUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, PlatformContextMockExtension.class})
@CurrentContextConfig("api-extender")
class VelocityReadOnlyGlobalRecordsTest {
    private VelocityReadOnlyGlobalRecords velocityReadOnlyGlobalRecords;

    @BeforeEach
    void setUp() {
        velocityReadOnlyGlobalRecords = new VelocityReadOnlyGlobalRecords();
    }

    @Test
    void testGetVersion() {
        try (MockedStatic<VersionUtils> mockedVersionUtils = Mockito.mockStatic(VersionUtils.class)) {
            Version mockedVersion = mock(Version.class);
            when(mockedVersion.getBundleVersion()).thenReturn("1.0.0");
            mockedVersionUtils.when(VersionUtils::getVersion).thenReturn(mockedVersion);

            String version = velocityReadOnlyGlobalRecords.getVersion();
            assertNotNull(version);
            assertEquals("1.0.0", version);
        }
    }

    @Test
    @SneakyThrows
    void testGetRecord() {
        String key = "testKey";
        Field mockedField = mock(Field.class);
        try (MockedConstruction<GlobalRecords> mockedGlobalRecords1 = Mockito.mockConstruction(GlobalRecords.class, (mock, context) ->
                when(mock.getRecord(key)).thenReturn(mockedField))) {

            Field field = velocityReadOnlyGlobalRecords.getRecord(key);
            assertNotNull(field);
            assertEquals(mockedField, field);
        }
    }
}
