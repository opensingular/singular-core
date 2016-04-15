package br.net.mirante.singular.form.wicket.mapper.selection;

import br.net.mirante.singular.form.mform.SIList;
import br.net.mirante.singular.form.mform.STypeComposite;
import br.net.mirante.singular.form.mform.basic.view.SViewAutoComplete;
import br.net.mirante.singular.form.mform.core.STypeString;
import br.net.mirante.singular.form.mform.options.SOptionsProvider;
import br.net.mirante.singular.form.wicket.helpers.SingularFormBaseTest;
import org.apache.wicket.request.Url;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class BloodhoundDataBehaviorTest extends SingularFormBaseTest {

    STypeString string;

    private void executeBloodhoundDataBehavior() {
        executeBloodhoundDataBehavior(null);
    }

    private void executeBloodhoundDataBehavior(String query) {
        final TypeaheadComponent           typeaheadComponent      = (TypeaheadComponent) findFirstFormComponentsByType(page.getForm(), string);
        final List<BloodhoundDataBehavior> bloodhoundDataBehaviors = typeaheadComponent.getBehaviors(BloodhoundDataBehavior.class);
        Assert.assertThat("O componente possui mais de um BloodhoundDataBehavior", bloodhoundDataBehaviors, Matchers.hasSize(1));
        String url = String.valueOf(bloodhoundDataBehaviors.get(0).getCallbackUrl());
        if (query != null) {
            url += "&filter=" + query;
        }
        tester.executeAjaxUrl(Url.parse(url));
    }

    @Test
    public void setsEncoding() {
        executeBloodhoundDataBehavior();
        assertThat(tester.getLastResponse().getContentType()).contains("application/json");
        assertThat(tester.getLastResponse().getContentType()).contains("charset=utf-8");
    }

    @Test
    public void returnOptions() {
        executeBloodhoundDataBehavior();
        JSONArray expected = new JSONArray();
        expected.put(createValue("1", "@gmail.com"));
        expected.put(createValue("2", "@hotmail.com"));
        expected.put(createValue("3", "@yahoo.com"));
        JSONAssert.assertEquals(expected, new JSONArray(tester.getLastResponseAsString()), false);
    }

    @Test
    public void applyFilterToOptions() {
        executeBloodhoundDataBehavior("bruce");
        JSONArray expected = new JSONArray();
        expected.put(createValue("1", "bruce@gmail.com"));
        expected.put(createValue("2", "bruce@hotmail.com"));
        expected.put(createValue("3", "bruce@yahoo.com"));
        JSONAssert.assertEquals(expected, new JSONArray(tester.getLastResponseAsString()), false);
    }

    private JSONObject createValue(String key, String v) {
        JSONObject value = new JSONObject();
        value.accumulate("key", key);
        value.accumulate("value", v);
        return value;
    }

    @Override
    protected void buildBaseType(STypeComposite<?> baseType) {
        string = baseType.addFieldString("string");
        string.withSelectionFromProvider(createProvider());
        string.withView(new SViewAutoComplete(SViewAutoComplete.Mode.DYNAMIC));
    }

    private SOptionsProvider createProvider() {
        return (SOptionsProvider) (instance, filter) -> {
            if (filter == null) filter = "";
            SIList<?> r = instance.getType().newList();
            r.addNew().setValue(filter + "@gmail.com");
            r.addNew().setValue(filter + "@hotmail.com");
            r.addNew().setValue(filter + "@yahoo.com");
            return r;
        };
    }
}