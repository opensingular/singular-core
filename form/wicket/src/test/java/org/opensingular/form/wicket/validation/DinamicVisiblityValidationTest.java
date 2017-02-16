package org.opensingular.form.wicket.validation;

import org.apache.wicket.markup.html.form.FormComponent;
import org.junit.Assert;
import org.junit.Test;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.wicket.IWicketComponentMapper;
import org.opensingular.form.wicket.helpers.SingularFormBaseTest;

public class DinamicVisiblityValidationTest extends SingularFormBaseTest {

    String testValue = "fvrw1e4r5t4e.r6";
    STypeString fieldOne;
    STypeString fieldTwo;

    @Override
    protected void buildBaseType(STypeComposite<?> mockType) {
        fieldOne = mockType.addFieldString("fieldOne");
        fieldOne.asAtr().required(true);
        fieldTwo = mockType.addFieldString("fieldTwo");

        fieldTwo.asAtr().dependsOn(fieldOne);
        fieldTwo.asAtr()
                .visible(instance -> instance.findNearestValue(fieldOne, String.class).orElse("").equals(testValue));
        fieldTwo.asAtr().required(true);
    }

    @Test
    public void testIfContaisErrorOnlyForFieldOne() {
        form.submit(page.getSingularValidationButton());
        Assert.assertTrue(findModelsByType(fieldOne).findFirst().get().getSInstance().hasValidationErrors());
        Assert.assertFalse(findModelsByType(fieldTwo).findFirst().get().getSInstance().hasValidationErrors());
    }

    @Test
    public void testIfNotContaisErrorForFieldTwoAfterChangeFieldOneValueWhithWrongValue() {
        form.setValue(findFieldOneFormComponent(), "abas" + testValue + "2132");
        tester.executeAjaxEvent(findFieldOneFormComponent(), IWicketComponentMapper.SINGULAR_PROCESS_EVENT);
        form.submit(page.getSingularValidationButton());
        Assert.assertTrue(findFormComponentsByType(fieldOne).findFirst().get().getFeedbackMessages().isEmpty());
        Assert.assertTrue(findFormComponentsByType(fieldTwo).findFirst().get().getFeedbackMessages().isEmpty());
    }

    @Test
    public void testIfContaisErrorForFieldTwoAfterChangeFieldOneValue() {
        form.setValue(findFieldOneFormComponent(), testValue);
        tester.executeAjaxEvent(findFieldOneFormComponent(), IWicketComponentMapper.SINGULAR_PROCESS_EVENT);
        form.submit(page.getSingularValidationButton());
        Assert.assertFalse(findModelsByType(fieldOne).findFirst().get().getSInstance().hasValidationErrors());
        Assert.assertTrue(findModelsByType(fieldTwo).findFirst().get().getSInstance().hasValidationErrors());
    }

    public FormComponent findFieldOneFormComponent() {
        return findFormComponentsByType(fieldOne).findFirst().get();
    }
}
