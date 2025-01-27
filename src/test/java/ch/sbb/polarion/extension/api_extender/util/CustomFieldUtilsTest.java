package ch.sbb.polarion.extension.api_extender.util;

import com.polarion.alm.projects.model.IProject;
import com.polarion.platform.core.IPlatform;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.persistence.IDataService;
import com.polarion.platform.persistence.model.IPrototype;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomFieldUtilsTest {
    private MockedStatic<PlatformContext> platformContextMock;
    private IPrototype mockPrototype;

    @BeforeEach
    void setUp() {
        platformContextMock = Mockito.mockStatic(PlatformContext.class);

        IDataService mockDataService = mock(IDataService.class);
        mockPrototype = mock(IPrototype.class);

        IPlatform mockPlatform = mock(IPlatform.class);
        platformContextMock.when(PlatformContext::getPlatform).thenReturn(mockPlatform);
        when(mockPlatform.lookupService(IDataService.class)).thenReturn(mockDataService);
        when(mockDataService.getPrototype(IProject.PROTO)).thenReturn(mockPrototype);
    }

    @Test
    void isStandardFieldKeyDefined() {
        String key = "standardKey";
        when(mockPrototype.isKeyDefined(key)).thenReturn(true);

        boolean result = CustomFieldUtils.isStandardField(key);

        assertTrue(result);
        verify(mockPrototype, times(1)).isKeyDefined(key);
    }

    @Test
    void isStandardFieldKeyNotDefined() {
        String key = "nonStandardKey";
        when(mockPrototype.isKeyDefined(key)).thenReturn(false);

        boolean result = CustomFieldUtils.isStandardField(key);

        assertFalse(result);
        verify(mockPrototype, times(1)).isKeyDefined(key);
    }

    @AfterEach
    void closeContext() {
        platformContextMock.close();
    }
}
