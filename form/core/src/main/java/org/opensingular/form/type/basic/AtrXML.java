package org.opensingular.form.type.basic;

import org.opensingular.form.SAttributeEnabled;
import org.opensingular.form.SInstance;
import org.opensingular.form.STranslatorForAttribute;
import org.opensingular.form.type.core.SPackagePersistence;

import java.util.function.Function;
import java.util.function.Predicate;

public class AtrXML extends STranslatorForAttribute {

    public AtrXML() {
    }

    public AtrXML(SAttributeEnabled target) {
        super(target);
    }

    public static <A extends SAttributeEnabled> Function<A, AtrXML> factory() {
        return AtrXML::new;
    }

    public AtrXML keepEmptyNode() {
        setAttributeValue(SPackagePersistence.ATR_XML, si -> true);
        return this;
    }

    public AtrXML keepEmptyNode(boolean keep) {
        setAttributeValue(SPackagePersistence.ATR_XML, si -> keep);
        return this;
    }

    public AtrXML keepEmptyNode(Predicate<SInstance> value) {
        setAttributeValue(SPackagePersistence.ATR_XML, value);
        return this;
    }

    public Predicate<SInstance> getKeepNodePredicate() {
        return isKeepNodePredicateConfigured() ? getAttributeValue(SPackagePersistence.ATR_XML) : si -> Boolean.FALSE ;
    }

    public boolean isKeepNodePredicateConfigured() {
        return getAttributeValue(SPackagePersistence.ATR_XML) != null;
    }

}
