package ch.sbb.polarion.extension.api_extender.project;

import ch.sbb.polarion.extension.api_extender.rest.model.Field;
import ch.sbb.polarion.extension.api_extender.rest.model.Records;
import ch.sbb.polarion.extension.generic.util.ScopeUtils;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.service.repository.IRepositoryService;
import com.polarion.subterra.base.location.ILocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class GlobalRecords extends GenericFields<Records> {
    private static final String POLARION_RECORDS_XML = ".polarion/records.xml";

    public GlobalRecords() {
        super(PlatformContext.getPlatform().lookupService(IRepositoryService.class));
    }

    @VisibleForTesting
    GlobalRecords(IRepositoryService repositoryService) {
        super(repositoryService);
    }

    @Override
    protected ILocation getLocation() {
        return ScopeUtils.getDefaultLocation().append(POLARION_RECORDS_XML);
    }

    @Nullable
    public Field getRecord(@NotNull String key) throws JAXBException {
        final Records records = deserialize();
        return records.getField(key);
    }

    public void setRecord(@NotNull String key, @Nullable String value) throws JAXBException, IOException {
        final Records records = deserialize();
        records.setField(key, value);
        serialize(records);
    }

}
