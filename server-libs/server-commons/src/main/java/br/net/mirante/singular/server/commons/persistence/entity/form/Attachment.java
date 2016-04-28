package br.net.mirante.singular.server.commons.persistence.entity.form;

import br.net.mirante.singular.form.mform.core.attachment.IAttachmentRef;
import br.net.mirante.singular.persistence.util.Constants;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = Constants.SCHEMA, name = "TB_ARQUIVO_PETICAO")
public class Attachment extends AbstractAttachmentEntity implements IAttachmentRef {


}
