/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.form.document;

import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.commons.lambda.ISupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * É um fábrica que formada pela composição de outra fábrica e acrescida de passos adicionais de setup do documento.
 *
 * @author Daniel C. Bordin
 */
final class SDocumentFactoryExtended extends SDocumentFactory {

    private final SDocumentFactory original;
    private final SDocumentConsumer setupSteps;
    private RefSDocumentFactoryExtended ref;

    SDocumentFactoryExtended(@Nonnull IConsumer<SDocument> setupStep) {
        this(null, SDocumentConsumer.of(setupStep));
    }


    private SDocumentFactoryExtended(@Nullable SDocumentFactory original, @Nullable SDocumentConsumer setupSteps) {
        this.original = original;
        this.setupSteps = setupSteps;
    }

    /**
     * Cria uma nova fábrica extendendo a original e acrescentando os passos adicionais de setup. Retorna a mesma
     * fábrica se o setup extra for null.
     */
    @Nonnull
    public static SDocumentFactory extend(@Nonnull SDocumentFactory original, @Nullable IConsumer<SDocument> extraSetupStep) {
        if (extraSetupStep == null) {
            return original;
        }
        if (original instanceof  SDocumentFactoryExtended) {
            SDocumentFactoryExtended original2 = (SDocumentFactoryExtended) original;
            SDocumentConsumer newSteps = original2.setupSteps == null ? SDocumentConsumer.of(extraSetupStep) :
                    original2.setupSteps.extendWith(extraSetupStep);
            return new SDocumentFactoryExtended(original2.original, newSteps);
        }
        return new SDocumentFactoryExtended(original, SDocumentConsumer.of(extraSetupStep));
    }

    // Por em quanto o método abaixo não possui uso
    //    @Nonnull
    //    public static SDocumentFactory extend(@Nonnull SDocumentFactory original,
    //            @Nullable ISupplier<ExternalServiceRegistry> registryProvider) {
    //        if (registryProvider == null) {
    //            return original;
    //        }
    //        if (original instanceof SDocumentFactoryExtended) {
    //            SDocumentFactoryExtended original2 = (SDocumentFactoryExtended) original;
    //            return new SDocumentFactoryExtended(original2.original, original2.setupSteps, registryProvider);
    //        }
    //        return new SDocumentFactoryExtended(original, null, registryProvider);
    //    }

    @Override
    protected RefSDocumentFactory createDocumentFactoryRef() {
        if (ref == null) {
            ref = new RefSDocumentFactoryExtended(this);
        }
        return ref;
    }

    @Override
    protected void setupDocument(SDocument document) {
        if (original != null) {
            original.setupDocument(document);
        }
        if (setupSteps != null) {
            setupSteps.accept(document);
        }
    }


    @Override
    public String toString() {
        if(original == null) {
            return super.toString();
        }
        return getClass().getSimpleName() + "( extend  factory " + original + ")";
    }

    /**
     * Referência serializável para a {@link RefSDocumentFactoryExtended}
     */
    private static final class RefSDocumentFactoryExtended extends RefSDocumentFactory {

        private final RefSDocumentFactory refOriginalFactory;
        private final SDocumentConsumer setupSteps;

        public RefSDocumentFactoryExtended(SDocumentFactoryExtended documentFactory) {
            super(documentFactory);
            if (documentFactory.original == null) {
                this.refOriginalFactory = null;
            } else {
                this.refOriginalFactory = documentFactory.original.getDocumentFactoryRef();
            }
            this.setupSteps = documentFactory.setupSteps;
        }

        @Nonnull
        @Override
        protected SDocumentFactory retrieve() {
            if (refOriginalFactory == null) {
                return new SDocumentFactoryExtended(null, setupSteps);
            }
            return new SDocumentFactoryExtended(refOriginalFactory.get(), setupSteps);
        }
    }
}
