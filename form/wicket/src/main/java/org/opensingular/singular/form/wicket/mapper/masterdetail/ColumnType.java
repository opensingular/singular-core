package org.opensingular.singular.form.wicket.mapper.masterdetail;

import org.opensingular.singular.commons.base.SingularException;
import org.opensingular.singular.commons.lambda.IFunction;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.type.basic.SPackageBasic;

class ColumnType {

    private final SType<?>                     type;
    private final String                       customLabel;
    private final IFunction<SInstance, String> displayFunction;

    ColumnType(SType<?> type, String customLabel, IFunction<SInstance, String> displayFunction) {
        if (type == null && displayFunction == null) {
            throw new SingularException("Não foi especificado o valor da coluna.");
        }
        this.type = type;
        this.customLabel = customLabel;
        this.displayFunction = displayFunction != null ? displayFunction : SInstance::toStringDisplay;
    }

    ColumnType(SType<?> type, String customLabel) {
        this(type, customLabel, null);
    }

    public SType<?> getType() {
        return type;
    }

    String getCustomLabel() {
        if (customLabel == null && type != null) {
            return type.getAttributeValue(SPackageBasic.ATR_LABEL);
        }
        return customLabel;
    }

    IFunction<SInstance, String> getDisplayFunction() {
        return displayFunction;
    }

}
