package ch.sbb.polarion.extension.api.extender.rest.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericFields {
    @XmlElement(name = "field")
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
