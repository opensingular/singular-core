package br.net.mirante.singular.util.wicket.datatable;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.model.IModel;

public interface IBSAction<T> extends Serializable {

    void execute(AjaxRequestTarget target, IModel<T> model);

    default boolean isEnabled(IModel<T> model) {
        return true;
    }

    default boolean isVisible(IModel<T> model) {
        return true;
    }

    default void updateAjaxAttributes(AjaxRequestAttributes attributes) {
    }

    static <T> IBSAction<T> noop() {
        return (t, m) -> {
        };
    }
    static <T> IBSAction<T> noopIfNull(IBSAction<T> action) {
        return (action != null) ? action : noop();
    }
}