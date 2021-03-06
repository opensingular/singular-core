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

package org.opensingular.form.persistence;

import com.google.common.collect.Lists;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocumentFactory;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persitencia de instância baseada em mapa em memória.
 *
 * @author Daniel C. Bordin
 */
public class FormPersistenceInMemory<TYPE extends SType<INSTANCE>, INSTANCE extends SInstance>
        extends AbstractFormPersistence<TYPE, INSTANCE, FormKeyInt> {

    private final Map<FormKeyInt, INSTANCE> collection = new LinkedHashMap<>();

    private int id;

    public FormPersistenceInMemory() {
        super(FormKeyInt.class);
    }

    public FormPersistenceInMemory(SDocumentFactory documentFactory, RefType refType) {
        super(FormKeyInt.class);
    }

    @Nonnull
    @Override
    public SDocumentFactory getDocumentFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void updateInternal(@Nonnull FormKeyInt key, @Nonnull INSTANCE instance, Integer inclusionActor) {
        if (!collection.containsKey(key)) {
            throw addInfo(new SingularFormPersistenceException("Não existe uma isntância com a chave informada")).add(
                    "key", key);
        }
        collection.put(key, instance);
    }

    @Override
    protected void deleteInternal(@Nonnull FormKeyInt key) {
        collection.remove(key);
    }

    @Override
    @Nonnull
    protected FormKeyInt insertInternal(@Nonnull INSTANCE instance, Integer inclusionActor) {
        FormKeyInt key = new FormKeyInt(++id);
        collection.put(key, instance);
        return key;
    }

    @Override
    protected INSTANCE loadInternal(FormKeyInt key) {
        return collection.get(key);
    }

    @Override
    @Nonnull
    protected List<INSTANCE> loadAllInternal() {
        return Lists.newArrayList(collection.values());
    }

    @Override
    @Nonnull
    protected List<INSTANCE> loadAllInternal(long first, long max) {
        long end = Math.max(first, Math.min(first + max, countAll()));
        return loadAllInternal().subList((int) first, (int) end);
    }

    @Override
    public long countAll() {
        return collection.values().size();
    }

    @Override
    public FormKey newVersion(INSTANCE instance, Integer inclusionActor, boolean keepAnnotations) {
        throw new SingularFormException("Método não implementado");
    }

    @Override
    public INSTANCE createInstance() {
        throw new SingularFormException("Método não implementado");
    }
}
