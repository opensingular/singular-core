package org.opensingular.form.service;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opensingular.form.PackageBuilder;
import org.opensingular.form.RefService;
import org.opensingular.form.SDictionary;
import org.opensingular.form.SInstance;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocument;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.form.type.core.STypeInteger;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.type.core.attachment.IAttachmentPersistenceHandler;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Rollback(value = false)
public abstract class FormServiceTest {

    @Inject
    protected SessionFactory sessionFactory;

    @Inject
    protected FormService       formService;
    protected SInstance         instancia;
    protected STypeComposite<?> tipoPessoa;
    protected STypeInteger      idade;
    protected STypeString       nome;
    protected SDocumentFactory  documentFactory;
    protected RefType tipoPessoaRef = RefType.of(() -> tipoPessoa);
    private SDocument                     document;

    @Before
    public void setUp() {
        PackageBuilder pb = createTestDictionary().createNewPackage("pessoaTeste");
        tipoPessoa = pb.createType("pessoa", STypeComposite.class);
        idade = tipoPessoa.addFieldInteger("idade");
        nome = tipoPessoa.addFieldString("nome");
        tipoPessoa.asAtrAnnotation().setAnnotated();

        documentFactory = SDocumentFactory.of(doc -> {
            IAttachmentPersistenceHandler<?> tempHandler = mock(IAttachmentPersistenceHandler.class);
            IAttachmentPersistenceHandler<?> persistentHandler = mock(IAttachmentPersistenceHandler.class);
            doc.setAttachmentPersistenceTemporaryHandler(RefService.ofToBeDescartedIfSerialized(tempHandler));
            doc.setAttachmentPersistencePermanentHandler(RefService.ofToBeDescartedIfSerialized(persistentHandler));
        });
        TransactionSynchronizationManager.bindResource(this.sessionFactory, new SessionHolder(sessionFactory.openSession()));
    }

    @After
    public void dispose() {
        TransactionSynchronizationManager.unbindResource(this.sessionFactory);
    }

    protected final SDictionary createTestDictionary() {
        return SDictionary.create();
    }


}
