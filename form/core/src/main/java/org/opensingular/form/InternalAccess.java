package org.opensingular.form;

import org.opensingular.form.io.SFormXMLUtil;
import org.opensingular.form.processor.TypeProcessorAttributeReadFromFile;
import org.opensingular.internal.lib.commons.xml.MElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * PARA USO INTERNO DA API APENAS. Dá acesso a estrutura internas do form. Os métodos aqui disponibilizados não deve ser
 * utilizados fora do core do form, pois poderão ser removidos ou ter seu comportamento no futuro.
 *
 * @author Daniel C. Bordin
 */
public final class InternalAccess {

    private static final InternalAccess INTERNAL_ACCESS = new InternalAccess();

    /** Provoca o repasse do objeto de acesso interno para as classes autorizadas pela API. */
    public static void load() {
        TypeProcessorAttributeReadFromFile.setInternalAccess(INTERNAL_ACCESS);
        SFormXMLUtil.setInternalAccess(INTERNAL_ACCESS);
    }

    private InternalAccess() {}

    /**
     * @see {@link SInstance#addUnreadInfo(MElement)}
     */
    public void addUnreadInfo(SInstance instance, MElement xmlInfo) {
        instance.addUnreadInfo(xmlInfo);
    }

    /**
     * @see {@link SInstance#getUnreadInfo()}
     */
    public List<MElement> getUnreadInfo(SInstance instance) {
        return instance.getUnreadInfo();
    }

    /**
     * @see {@link SType#setAttributeValueSavingForLatter(String, String)}
     */
    public void setAttributeValueSavingForLatter(@Nonnull SType<?> target, @Nonnull String attributeName,
            @Nullable String value) {
        target.setAttributeValueSavingForLatter(attributeName, value);
    }

    /**
     * @see {@link SInstance#setAttributeValueSavingForLatter(String, String)}
     */
    public void setAttributeValueSavingForLatter(@Nonnull SInstance target, @Nonnull String attributeName,
            @Nullable String value) {
        target.setAttributeValueSavingForLatter(attributeName, value);
    }
}
