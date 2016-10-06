package org.opensingular.form.builder.selection;

import org.opensingular.form.SInstance;
import org.opensingular.form.provider.FreemarkerUtil;
import org.opensingular.singular.commons.lambda.IFunction;
import org.opensingular.form.SType;

import java.io.Serializable;


public class SelectionBuilder<TYPE extends Serializable, ROOT_TYPE extends SInstance, ELEMENT_TYPE extends SInstance> extends AbstractBuilder {

    public SelectionBuilder(SType<?> type) {
        super(type);
    }

    public ProviderBuilder<TYPE, ROOT_TYPE> selfIdAndDisplay(){
        return selfId().selfDisplay().simpleConverter();
    }

    public SelectionDisplayBuilder<TYPE, ROOT_TYPE, ELEMENT_TYPE> selfId() {
        type.asAtrProvider().idFunction((o) -> o);
        return next();
    }

    public SelectionDisplayBuilder<TYPE, ROOT_TYPE, ELEMENT_TYPE> id(String freemarkerTemplate) {
        type.asAtrProvider().idFunction((o) -> FreemarkerUtil.mergeWithFreemarker(freemarkerTemplate, o));
        return next();
    }

    public SelectionDisplayBuilder<TYPE, ROOT_TYPE, ELEMENT_TYPE> id(IFunction<TYPE, Object> valor) {
        type.asAtrProvider().idFunction(valor);
        return next();
    }

    private SelectionDisplayBuilder<TYPE, ROOT_TYPE, ELEMENT_TYPE> next() {
        return new SelectionDisplayBuilder<>(type);
    }


}