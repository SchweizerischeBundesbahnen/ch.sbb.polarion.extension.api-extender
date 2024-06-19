package ch.sbb.polarion.extension.api.extender.rest.model.xml;

import ch.sbb.polarion.extension.api.extender.rest.model.Records;
import ch.sbb.polarion.extension.generic.jaxb.JAXBUtils;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordsTest {

    private static final String XML_CONTENT = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + System.lineSeparator() +
            "<records>" + System.lineSeparator() +
            "    <field id=\"key1\">recordvalue1</field>" + System.lineSeparator() +
            "    <field id=\"key2\">recordvalue2</field>" + System.lineSeparator() +
            "</records>" + System.lineSeparator() +
            "";

    @Test
    void testGetField() throws JAXBException, IOException {
        final Records records = JAXBUtils.deserialize(Records.class, XML_CONTENT);
        assertEquals("recordvalue1", records.getField("key1").getValue());
        assertEquals("recordvalue2", records.getField("key2").getValue());

        final String serialized = JAXBUtils.serialize(records);
        assertEquals(XML_CONTENT.replaceAll(System.lineSeparator(), "\n"), serialized);
    }

    @Test
    void testSetField() throws JAXBException, IOException {
        final Records records = JAXBUtils.deserialize(Records.class, XML_CONTENT);
        records.setField("key", "value");
        assertEquals("value", records.getField("key").getValue());

        final String serialized = JAXBUtils.serialize(records);
        assertTrue(serialized.contains("<field id=\"key\">value</field>"));
    }

}