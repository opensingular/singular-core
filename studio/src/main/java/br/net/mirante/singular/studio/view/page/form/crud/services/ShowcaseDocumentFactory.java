/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.studio.view.page.form.crud.services;

import static br.net.mirante.singular.form.RefService.of;
import static br.net.mirante.singular.form.type.core.attachment.handlers.FileSystemAttachmentHandler.newTemporaryHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import br.net.mirante.singular.form.document.SDocument;
import br.net.mirante.singular.form.spring.SpringSDocumentFactory;
import br.net.mirante.singular.form.spring.SpringServiceRegistry;
import br.net.mirante.singular.form.type.core.attachment.IAttachmentPersistenceHandler;
import br.net.mirante.singular.form.type.core.attachment.handlers.InMemoryAttachmentPersitenceHandler;
import br.net.mirante.singular.studio.spring.SingularStudioSpringConfiguration;

@Component("showcaseDocumentFactory")
public class ShowcaseDocumentFactory extends SpringSDocumentFactory {

    private final static SpringServiceRegistry registry;

    static {
        registry = new SpringServiceRegistry(new AnnotationConfigApplicationContext(SingularStudioSpringConfiguration.class));
    }

    @Override
    protected void setupDocument(SDocument document) {
        try {
            document.setAttachmentPersistenceTemporaryHandler(of(newTemporaryHandler()));
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,"Could not create temporary file folder, using memory instead",e);
            document.setAttachmentPersistenceTemporaryHandler(of(new InMemoryAttachmentPersitenceHandler()));
        }
        document.setAttachmentPersistencePermanentHandler(
                of(getServiceRegistry().lookupService(IAttachmentPersistenceHandler.class)));
        document.addServiceRegistry(registry);
    }


}
