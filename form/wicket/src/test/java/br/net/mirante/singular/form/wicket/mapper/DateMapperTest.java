package br.net.mirante.singular.form.wicket.mapper;

import br.net.mirante.singular.form.mform.MIComposto;
import br.net.mirante.singular.form.mform.MTipoComposto;
import br.net.mirante.singular.form.wicket.enums.ViewMode;
import br.net.mirante.singular.util.wicket.output.BOutputPanel;
import org.apache.wicket.Component;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

import java.util.Optional;

import static br.net.mirante.singular.form.wicket.hepers.TestFinders.findId;
import static org.junit.Assert.*;

public class DateMapperTest extends MapperBaseTest {

    @Override
    public void appendPackageFields(MTipoComposto<? extends MIComposto> form) {
        form.addCampoData("data");
    }

    @Override
    public void mockFormValues(MIComposto formInstance) {
       formInstance.setValor("data", "01/07/1991");
    }

    @Test
    public void testEditRendering() {
        FormTester formTester = startPage(ViewMode.EDITION);
        Optional<String> data = findId(formTester.getForm(), "data");
        Optional<String> _outputdata= findId(formTester.getForm(), "_outputdata");
        assertTrue(data.isPresent());
        assertFalse(_outputdata.isPresent());
    }

    @Test
    public void testVisualizationRendering() {
        FormTester formTester = startPage(ViewMode.VISUALIZATION);

        Optional<String> data = findId(formTester.getForm(), "data");
        assertFalse(data.isPresent());

        Optional<String> _outputdata = findId(formTester.getForm(), "_outputdata");
        assertTrue(_outputdata.isPresent());

        Component panel = formTester.getForm().get(_outputdata.get());
        assertNotNull(panel);
        assertTrue(panel instanceof BOutputPanel);

        Optional<String> outputFieldId = findId((BOutputPanel) panel, "output");
        assertTrue(outputFieldId.isPresent());

        Component output = panel.get(outputFieldId.get());
        assertNotNull(panel);

        assertEquals("01/07/1991", output.getDefaultModelObject());
    }
}