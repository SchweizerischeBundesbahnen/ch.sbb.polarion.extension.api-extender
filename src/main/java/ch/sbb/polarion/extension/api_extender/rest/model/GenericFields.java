package ch.sbb.polarion.extension.api_extender.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "A generic class representing a list of fields")
public abstract class GenericFields {

    @XmlElement(name = "field")
    @Schema(description = "List of fields represented as key-value pairs")
    protected List<Field> fields = new ArrayList<>();

    public void setField(@NotNull String key, @Nullable String value) {
        if (value == null) {
            fields.removeIf(f -> f.getKey().equals(key));
        } else {
            final Field field = getField(key);
            if (field != null) {
                field.setValue(value);
            } else {
                fields.add(new Field(key, value));
            }
        }
    }

    @Nullable
    public Field getField(@NotNull String key) {
        return fields.stream()
                .filter(f -> f.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }
}
