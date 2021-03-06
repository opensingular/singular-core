/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.form.document;

import org.junit.Before;
import org.junit.Test;
import org.opensingular.form.SDictionary;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInfoPackage;
import org.opensingular.form.SInfoType;
import org.opensingular.form.SPackage;
import org.opensingular.form.SType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.TypeBuilder;
import org.opensingular.lib.commons.context.ServiceRegistryLocator;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class TestTypeLoaderInjectionBroken {

    @Before
    public void setUp(){
        ServiceRegistryLocator.setup(new MockServiceRegistry());
        ServiceRegistryLocator.locate().bindService(NadaBean.class, () -> new NadaBean());
    }

    @Test
    public void testAddToTypeLoaderBroken() throws Exception {
        MyTypeLoader myTypeLoader = new MyTypeLoader();
        myTypeLoader.add(NadaType.class);

    }

    @Test
    public void testOtherAddToTypeLoaderBroken() throws Exception {
        MyTypeLoader myTypeLoader = new MyTypeLoader();
        myTypeLoader.otherAdd(NadaType.class);
    }


    @Test
    public void testAnotherAddToTypeLoaderBroken() throws Exception {
        MyTypeLoader myTypeLoader = new MyTypeLoader();
        myTypeLoader.anotherAdd(NadaType.class);
    }


    @Test(expected = NoSuchElementException.class)
    public void testYetAnotherAddToTypeLoaderBroken() throws Exception {
        MyTypeLoader myTypeLoader = new MyTypeLoader();
        myTypeLoader.yetAnotherAdd(NadaType.class);
    }

    @Test(expected = NoSuchElementException.class)
    public void brokenAddToTypeLoaderBroken() throws Exception {
        MyTypeLoader myTypeLoader = new MyTypeLoader();
        myTypeLoader.brokenAdd(NadaType.class);
    }

    @SInfoPackage(name = "nada")
    public static class NadaPack extends SPackage {

    }

    @SInfoType(name = "nadaType", spackage = NadaPack.class)
    public static class NadaType extends STypeComposite<SIComposite> {

        @Inject
        private NadaBean nada;

        @Override
        protected void onLoadType(@Nonnull TypeBuilder tb) {
            nada.doNada();
        }
    }

    public static class NadaBean {

        public void doNada() {
            System.out.println("nada");
        }
    }


    public static class MyTypeLoader extends TypeLoader<Class<? extends SType>> {

        private Map<Class<? extends SType>, SType<?>> map = new HashMap<>();

        @Nonnull
        @Override
        protected Optional<RefType> loadRefTypeImpl(@Nonnull Class<? extends SType> typeId) {
            return Optional.of(RefType.of(typeId));
        }

        @Nonnull
        @Override
        protected Optional<SType<?>> loadTypeImpl(@Nonnull Class<? extends SType> typeId) {
            return Optional.ofNullable(map.get(typeId));
        }

        public void add(Class<NadaType> nadaTypeClass) {
            NadaType nada = SDictionary.create().getType(nadaTypeClass);
            map.put(nadaTypeClass, nada);
        }

        public void otherAdd(Class<NadaType> nadaTypeClass) {
            SIComposite composite = (SIComposite) SDocumentFactory.empty().createInstance(RefType.of(nadaTypeClass));
            map.put(nadaTypeClass, composite.getType());
        }

        public void anotherAdd(Class<NadaType> nadaTypeClass) {
            map.put(nadaTypeClass, loadRefTypeImpl(nadaTypeClass).get().get());
        }

        public void yetAnotherAdd(Class<NadaType> nadaTypeClass) {
            map.put(nadaTypeClass, loadTypeImpl(nadaTypeClass).get());
        }

        public void brokenAdd(Class<NadaType> nadaTypeClass) {
            NadaType ohYeah = (NadaType) this.loadType(nadaTypeClass).get();
            map.put(nadaTypeClass, ohYeah);
        }
    }
}
