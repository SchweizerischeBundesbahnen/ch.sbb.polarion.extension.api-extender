package ch.sbb.polarion.extension.api_extender;

import ch.sbb.polarion.extension.generic.GenericUiServlet;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExtenderAppServletTest {

    /**
     * The webapp name has to match the context registered in plugin.xml and the paths hivemodule.xml
     * opens; a mismatch serves nothing and stays invisible until an administration page is opened.
     */
    @Test
    void servesTheReactAppWebapp() throws Exception {
        ApiExtenderAppServlet servlet = new ApiExtenderAppServlet();

        Field field = GenericUiServlet.class.getDeclaredField("webAppName");
        field.setAccessible(true);

        assertEquals("api-extender-app", field.get(servlet));
    }
}
