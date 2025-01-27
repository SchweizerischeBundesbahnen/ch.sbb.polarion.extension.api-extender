package ch.sbb.polarion.extension.api_extender.velocity;

import ch.sbb.polarion.extension.api_extender.project.GlobalRecords;
import ch.sbb.polarion.extension.api_extender.rest.model.Field;
import ch.sbb.polarion.extension.generic.util.VersionUtils;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBException;

@NoArgsConstructor
public class VelocityReadOnlyGlobalRecords {

    @Nullable
    public String getVersion() {
        return VersionUtils.getVersion().getBundleVersion();
    }

    @Nullable
    public Field getRecord(@NotNull String key) throws JAXBException {
        return new GlobalRecords().getRecord(key);
    }
}
