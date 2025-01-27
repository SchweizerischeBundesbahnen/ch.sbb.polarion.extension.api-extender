package ch.sbb.polarion.extension.api_extender.rest.model.xml;

import ch.sbb.polarion.extension.api_extender.rest.model.Project;
import ch.sbb.polarion.extension.generic.jaxb.JAXBUtils;
import ch.sbb.polarion.extension.api_extender.util.CustomFieldUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectTest {

    private static final String XML_CONTENT = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + System.lineSeparator() +
            "<project>" + System.lineSeparator() +
            "    <field id=\"active\">true</field>" + System.lineSeparator() +
            "    <field id=\"description\">This model project demonstrates how an actual software project might be set up using the 'Agile Software Project' template.</field>" + System.lineSeparator() +
            "    <field id=\"lead\">rProject</field>" + System.lineSeparator() +
            "    <field id=\"name\">E-Library</field>" + System.lineSeparator() +
            "    <field id=\"start\">2016-10-01</field>" + System.lineSeparator() +
            "    <field id=\"trackerPrefix\">EL</field>" + System.lineSeparator() +
            "    <field id=\"color\">#8E9D23</field>" + System.lineSeparator() +
            "    <field id=\"icon\">/polarion/icons/default/topicIcons/App_985-demo-elibrary.svg</field>" + System.lineSeparator() +
            "    <field id=\"custom_field\">custom_value</field>" + System.lineSeparator() +
            "</project>" + System.lineSeparator() +
            "";

    @Test
    void testGetField() throws JAXBException, IOException {
        final Project project = JAXBUtils.deserialize(Project.class, XML_CONTENT);
        assertEquals("custom_value", project.getField("custom_field").getValue());
        assertEquals("#8E9D23", project.getField("color").getValue());

        final String serialized = JAXBUtils.serialize(project);
        assertEquals(XML_CONTENT.replaceAll(System.lineSeparator(), "\n"), serialized);
    }

    @Test
    void testSetCustomField() throws JAXBException, IOException {
        try (final MockedStatic<CustomFieldUtils> customFieldUtilsMockedStatic = Mockito.mockStatic(CustomFieldUtils.class)) {
            customFieldUtilsMockedStatic.when(() -> CustomFieldUtils.isStandardField("key")).thenReturn(false);

            final Project project = JAXBUtils.deserialize(Project.class, XML_CONTENT);
            project.setField("key", "value");
            assertEquals("value", project.getField("key").getValue());

            final String serialized = JAXBUtils.serialize(project);
            assertTrue(serialized.contains("<field id=\"key\">value</field>"));
        }
    }

    @Test
    @SneakyThrows
    void testSetStandardField() {
        try (final MockedStatic<CustomFieldUtils> customFieldUtilsMockedStatic = Mockito.mockStatic(CustomFieldUtils.class)) {
            customFieldUtilsMockedStatic.when(() -> CustomFieldUtils.isStandardField("description")).thenReturn(true);

            final Project project = JAXBUtils.deserialize(Project.class, XML_CONTENT);
            IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                project.setField("description", "value");
            });

            assertEquals("'description' is a standard field of project", illegalArgumentException.getMessage());
        }
    }
}
