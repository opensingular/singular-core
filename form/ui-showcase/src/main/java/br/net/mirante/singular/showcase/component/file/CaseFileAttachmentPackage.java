package br.net.mirante.singular.showcase.component.file;

import br.net.mirante.singular.form.mform.PackageBuilder;
import br.net.mirante.singular.form.mform.SPackage;
import br.net.mirante.singular.form.mform.STypeComposite;
import br.net.mirante.singular.form.mform.basic.ui.AtrBasic;
import br.net.mirante.singular.form.mform.basic.ui.AtrBootstrap;
import br.net.mirante.singular.form.mform.core.AtrCore;
import br.net.mirante.singular.form.mform.core.attachment.STypeAttachment;

public class CaseFileAttachmentPackage extends SPackage {

    @Override
    protected void carregarDefinicoes(PackageBuilder pb) {
        STypeComposite<?> tipoMyForm = pb.createTipoComposto("testForm");

        STypeAttachment anexo = tipoMyForm.addCampo("anexo", STypeAttachment.class);
        anexo.as(AtrBasic.class).label("Anexo");
        anexo.as(AtrCore.class).obrigatorio(true);
        anexo.as(AtrBootstrap.class).colPreference(3);
    }
}
