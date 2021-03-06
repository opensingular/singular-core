/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.opensingular.form.wicket.mapper.composite;

import org.junit.Test;
import org.opensingular.form.SIComposite;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.type.core.STypeInteger;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.view.SViewByBlock;
import org.opensingular.form.wicket.enums.AnnotationMode;
import org.opensingular.form.wicket.helpers.SingularFormDummyPageTester;

import java.util.function.Consumer;

/**
 * Teste apenas renderização sem exception, devem ser criados outros cenários de teste
 * @author Vinicius Nunes on 10/04/2017.
 *
 */
public class BlocksCompositeMapperTest {

    private static void createSimpleForm(STypeComposite<SIComposite> testForm) {

        STypeString  nome  = testForm.addFieldString("nome");
        STypeInteger idade = testForm.addFieldInteger("idade");

        STypeComposite<SIComposite> experiencia = testForm.addFieldComposite("experiencia");
        STypeString                 empresa     = experiencia.addFieldString("empresa", true);
        STypeString                 cargo       = experiencia.addFieldString("cargo", true);
        experiencia.asAtrAnnotation().setAnnotated();




        testForm.withView(new SViewByBlock(), vbb -> {
            vbb.newBlock("Experiencia ")
                    .add(experiencia)
                    .newBlock("Dados Cargo")
                    .add(nome)
                    .add(idade);
        });
    }

    @Test
    public void test_Edit() {
        testBlocksCompositeMapper(ctx -> ctx.getDummyPage().setAsEditView());
    }

    @Test
    public void test_Edit_Annotation() {
        testBlocksCompositeMapper(ctx -> {
            ctx.getDummyPage().setAsEditView();
            ctx.getDummyPage().getSingularFormPanel().setAnnotationMode(AnnotationMode.EDIT);
        });
        testBlocksCompositeMapper(ctx -> ctx.getDummyPage().setAsEditView());
    }

    @Test
    public void test_ReadOnly() {
        testBlocksCompositeMapper(ctx -> ctx.getDummyPage().setAsVisualizationView());
    }

    @Test
    public void test_ReadOnly_Annotation() {
        testBlocksCompositeMapper(ctx -> {
            ctx.getDummyPage().setAsVisualizationView();
            ctx.getDummyPage().getSingularFormPanel().setAnnotationMode(AnnotationMode.EDIT);
        });
    }

    public void testBlocksCompositeMapper(Consumer<SingularFormDummyPageTester> config) {
        SingularFormDummyPageTester ctx = new SingularFormDummyPageTester();
        ctx.getDummyPage().setTypeBuilder(BlocksCompositeMapperTest::createSimpleForm);
        config.accept(ctx);
        ctx.startDummyPage();
    }
}
