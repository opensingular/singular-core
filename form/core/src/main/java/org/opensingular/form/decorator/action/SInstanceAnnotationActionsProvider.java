package org.opensingular.form.decorator.action;

import static org.apache.commons.lang3.BooleanUtils.*;

import java.util.Arrays;
import java.util.Collections;

import org.opensingular.form.PackageBuilder;
import org.opensingular.form.SDictionary;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.decorator.action.SInstanceAction.ActionHandler;
import org.opensingular.form.decorator.action.SInstanceAction.ActionType;
import org.opensingular.form.decorator.action.SInstanceAction.Delegate;
import org.opensingular.form.decorator.action.SInstanceAction.FormDelegate;
import org.opensingular.form.decorator.action.SInstanceAction.Preview;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.form.type.core.STypeBoolean;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.type.core.annotation.SIAnnotation;
import org.opensingular.form.view.SViewTextArea;
import org.opensingular.lib.commons.lambda.IPredicate;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.opensingular.lib.commons.ref.Out;

/**
 * Provider para a ação de exibição do Help do campo.
 */
public class SInstanceAnnotationActionsProvider implements ISInstanceActionsProvider {

    private final IPredicate<SInstance> annotationsVisible;
    private final IPredicate<SInstance> annotationsEditable;
    public SInstanceAnnotationActionsProvider(IPredicate<SInstance> annotationsVisible, IPredicate<SInstance> annotationsEditable) {
        this.annotationsVisible = annotationsVisible;
        this.annotationsEditable = annotationsEditable;
    }

    @Override
    public Iterable<SInstanceAction> getActions(ISInstanceActionCapable target, SInstance instance) {
        if (!annotationsVisible.test(instance))
            return Collections.emptyList();

        final boolean annotatable = instance.asAtrAnnotation().isAnnotated();
        final boolean editable = !isEmpty(instance) && annotationsEditable.test(instance);

        if (!annotatable)
            return Collections.emptyList();

        final SIcon icon = resolveIcon(instance);
        final Preview preview = resolvePreview(instance);

        SInstanceAction editAction = new SInstanceAction(SInstanceAction.ActionType.NORMAL)
            .setIcon(icon)
            .setText(getEditActionTitle(instance))
            .setPosition(Integer.MAX_VALUE)
            .setPreview(preview)
            .setActionHandler(new EditAnnotationHandler());

        return Arrays.asList(editAction);
    }

    private static String getEditActionTitle(SInstance instance) {
        return "Comentários sobre " + instance.asAtr().getLabel();
    }

    private static Preview resolvePreview(SInstance instance) {
        if (isEmpty(instance))
            return null;

        return new Preview()
            .setTitle("Comentário")
            .setMessage("")
            .setFormat("")
            .setActions(Arrays.asList(
                new SInstanceAction(ActionType.LINK)
                    .setText("Editar")
                    .setIcon(SIcon.resolve(SingularFormAnnotationsIconProvider.ANNOTATION_EDIT))
                    .setActionHandler(new EditAnnotationHandler()), //

                new SInstanceAction(ActionType.LINK)
                    .setText("Remover")
                    .setIcon(SIcon.resolve(SingularFormAnnotationsIconProvider.ANNOTATION_REMOVE))
                    .setActionHandler(new RemoveAnnotationHandler()) //
        ));
    }

    private static SIcon resolveIcon(SInstance instance) {
        if (isApproved(instance))
            return SIcon.resolve(SingularFormAnnotationsIconProvider.ANNOTATION_APPROVED).setColors("#7f7", "#fff");
        else if (isRejected(instance))
            return SIcon.resolve(SingularFormAnnotationsIconProvider.ANNOTATION_REJECTED).setColors("#f77", "#fff");
        else
            return SIcon.resolve(SingularFormAnnotationsIconProvider.ANNOTATION_EMPTY).setColors("#aaa", "#fff");
    }

    private static boolean isEmpty(SInstance instance) {
        return !isApproved(instance) || !isRejected(instance);
    }

    private static boolean isRejected(SInstance instance) {
        return isFalse(instance.asAtrAnnotation().approved());
    }

    private static boolean isApproved(SInstance instance) {
        return isTrue(instance.asAtrAnnotation().approved());
    }

    private static final class FormAnotacaoRefType extends RefType {
        static final String JUSTIFICATIVA = "justificativa";
        static final String APROVADO      = "aprovado";

        @Override
        protected SType<?> retrieve() {
            final SDictionary dict = SDictionary.create();
            final PackageBuilder pkg = dict.createNewPackage("anotacoes");
            final STypeComposite<SIComposite> anotacao = pkg.createCompositeType("anotacao");
            final STypeBoolean aprovado = anotacao.addField(APROVADO, STypeBoolean.class);
            final STypeString justificativa = anotacao.addField(JUSTIFICATIVA, STypeString.class);

            aprovado.asAtr().label("Aprovado?");

            justificativa.setView(SViewTextArea::new);
            justificativa.asAtr().label("Justificativa");

            return anotacao;
        }
    }

    private static final class EditAnnotationHandler implements ActionHandler {
        @Override
        public void onAction(SInstanceAction action, ISupplier<SInstance> fieldInstance, Delegate delegate) {
            ISupplier<SInstance> formSupplier = () -> {
                SInstance ins = SDocumentFactory.empty().createInstance(new FormAnotacaoRefType());
                ins.getField(FormAnotacaoRefType.APROVADO)
                    .setValue(fieldInstance.get().asAtrAnnotation().approved());
                ins.getField(FormAnotacaoRefType.JUSTIFICATIVA)
                    .setValue(fieldInstance.get().asAtrAnnotation().text());
                return ins;
            };
            Out<SInstanceAction.FormDelegate> formDelegate = new Out<>();
            delegate.openForm(formDelegate,
                getEditActionTitle(fieldInstance.get()),
                null,
                formSupplier,
                fd -> Arrays.asList(
                    new SInstanceAction(SInstanceAction.ActionType.NORMAL)
                        .setText("Cancelar")
                        .setActionHandler(new CloseFormHandler(fd)),
                    new SInstanceAction(SInstanceAction.ActionType.PRIMARY)
                        .setText("Confirmar")
                        .setActionHandler(new ConfirmarEdicaoHandler(fd)) //
                ));
        }
    }

    private static final class RemoveAnnotationHandler implements ActionHandler {
        @Override
        public void onAction(SInstanceAction action, ISupplier<SInstance> fieldInstance, Delegate delegate) {
            Out<FormDelegate> formDelegate = new Out<>();
            delegate.openForm(formDelegate,
                "Você está prestes a remover este comentário",
                "Deseja realmente prosseguir e apagá-lo?",
                () -> null,
                fd -> Arrays.asList(
                    new SInstanceAction(ActionType.PRIMARY)
                        .setText("Apagar")
                        .setActionHandler((a, i, d) -> {
                            i.get().asAtrAnnotation().clear();
                            d.refreshFieldForInstance(i.get());
                            fd.close();
                        }),
                    new SInstanceAction(ActionType.PRIMARY)
                        .setText("Cancelar")
                        .setActionHandler((a, i, d) -> fd.close())//
                ));
        }
    }

    private static final class ConfirmarEdicaoHandler implements ActionHandler {
        private final FormDelegate formDelegate;
        public ConfirmarEdicaoHandler(FormDelegate formDelegate) {
            this.formDelegate = formDelegate;
        }
        @Override
        public void onAction(SInstanceAction action, ISupplier<SInstance> actionInstanceSupplier, Delegate delegate) {
            final SInstance formInstance = formDelegate.getFormInstance();
            final SInstance fieldInstance = delegate.getInstanceRef().get();
            final SIAnnotation annotationInstance = fieldInstance.asAtrAnnotation().annotation();

            annotationInstance.setApproved(formInstance.getValue(FormAnotacaoRefType.APROVADO));
            annotationInstance.setText(formInstance.getValue(FormAnotacaoRefType.JUSTIFICATIVA));

            delegate.refreshFieldForInstance(fieldInstance);
            formDelegate.close();
        }
    }

    private static final class CloseFormHandler implements ActionHandler {
        private final FormDelegate formDelegate;
        public CloseFormHandler(FormDelegate formDelegate) {
            this.formDelegate = formDelegate;
        }
        @Override
        public void onAction(SInstanceAction action, ISupplier<SInstance> actionInstanceSupplier, Delegate delegate) {
            formDelegate.close();
        }
    }
}
