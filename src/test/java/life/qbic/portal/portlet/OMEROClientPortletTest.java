package life.qbic.portal.portlet;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link OMEROClientPortlet}.
 */
public class OMEROClientPortletTest {

    @Test
    public void mainUIExtendsQBiCPortletUI() {
        assertTrue("The main UI class must extend life.qbic.portlet.QBiCPortletUI", 
            QBiCPortletUI.class.isAssignableFrom(OMEROClientPortlet.class));
    }

    @Test
    public void mainUIIsNotQBiCPortletUI() {
        assertFalse("The main UI class must be different to life.qbic.portlet.QBiCPortletUI", 
            QBiCPortletUI.class.equals(OMEROClientPortlet.class));
    }
}