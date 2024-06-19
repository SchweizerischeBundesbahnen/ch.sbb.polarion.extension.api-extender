package ch.sbb.polarion.extension.api.extender.util;

import com.polarion.alm.projects.model.IProject;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.persistence.IDataService;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CustomFieldUtils {

    public static boolean isStandardField(String key) {
        return PlatformContext.getPlatform().lookupService(IDataService.class)
                .getPrototype(IProject.PROTO)
                .isKeyDefined(key);
    }
}
