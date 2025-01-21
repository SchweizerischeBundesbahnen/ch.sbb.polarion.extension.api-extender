package ch.sbb.polarion.extension.api.extender.util;

import ch.sbb.polarion.extension.generic.util.ScopeUtils;
import com.polarion.alm.projects.IProjectService;
import com.polarion.alm.projects.model.IProject;
import com.polarion.platform.core.IPlatform;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.security.ISecurityService;
import com.polarion.subterra.base.data.identification.ContextId;
import com.polarion.subterra.base.data.identification.IContextId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolesUtilsTest {
    private ISecurityService securityService;

    private IProjectService projectService;

    @BeforeEach
    void setUp() {
        securityService = mock(ISecurityService.class);
        projectService = mock(IProjectService.class);
    }

    @Test
    void testGetProjectRolesWithValidScope() {
        try (MockedStatic<PlatformContext> platformContextMock = mockStatic(PlatformContext.class);
             MockedStatic<ScopeUtils> scopeUtilsMock = mockStatic(ScopeUtils.class)) {

            IPlatform iPlatform = mock(IPlatform.class);
            when(PlatformContext.getPlatform()).thenReturn(iPlatform);
            when(iPlatform.lookupService(ISecurityService.class)).thenReturn(securityService);
            when(iPlatform.lookupService(IProjectService.class)).thenReturn(projectService);

            String scope = "testScope";
            String projectId = "testProject";
            IContextId contextId = ContextId.getContextIdFromContext("contextId");
            List<String> contextRoles = List.of("CONTEXT_ADMIN", "CONTEXT_USER");

            scopeUtilsMock.when(() -> ScopeUtils.getProjectFromScope(scope)).thenReturn(projectId);

            IProject project = mock(IProject.class);
            when(projectService.getProject(projectId)).thenReturn(project);
            when(project.getContextId()).thenReturn(contextId);
            when(securityService.getContextRoles(contextId)).thenReturn(contextRoles);

            Collection<String> result = RolesUtils.getProjectRoles(scope);
            assertEquals(contextRoles, result);
        }
    }

    @Test
    void testGetProjectRolesWithInvalidScope() {
        try (MockedStatic<PlatformContext> platformContextMock = mockStatic(PlatformContext.class);
             MockedStatic<ScopeUtils> scopeUtilsMock = mockStatic(ScopeUtils.class)) {

            IPlatform iPlatform = mock(IPlatform.class);
            when(PlatformContext.getPlatform()).thenReturn(iPlatform);

            String scope = "invalidScope";
            scopeUtilsMock.when(() -> ScopeUtils.getProjectFromScope(scope)).thenReturn(null);

            Collection<String> result = RolesUtils.getProjectRoles(scope);
            assertEquals(Set.of(), result);
        }
    }
}
