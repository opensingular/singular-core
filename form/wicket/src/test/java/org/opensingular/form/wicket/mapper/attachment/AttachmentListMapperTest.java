package org.opensingular.form.wicket.mapper.attachment;

import org.apache.wicket.util.tester.FormTester;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensingular.form.SIList;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.type.core.attachment.SIAttachment;
import org.opensingular.form.wicket.helpers.AssertionsWComponent;
import org.opensingular.form.wicket.helpers.SingularDummyFormPageTester;
import org.opensingular.internal.lib.commons.util.TempFileProvider;

import java.io.File;
import java.io.IOException;

public class AttachmentListMapperTest {

    protected TempFileProvider tmpProvider;

    @Before
    public void createTmpProvider() {
        tmpProvider = TempFileProvider.createForUseInTryClause(this);
    }

    @After
    public void cleanTmpProvider() {
        tmpProvider.deleteOrException();
    }


    @Test
    public void testAddNewFileAndRemove() throws IOException {
        SingularDummyFormPageTester ctx = new SingularDummyFormPageTester();
        ctx.getDummyPage().setTypeBuilder(AttachmentListMapperTest::createType);
        ctx.getDummyPage().setAsEditView();
        ctx.startDummyPage();

        ctx.getAssertionsPage().debugComponentTree(); //TODO apagar essa linha depois de pronto o teste
        SIList<SIAttachment> atts = (SIList<SIAttachment>) ctx.getAssertionsInstance().field("attachments").isList(0)
                .getTarget();

        ctx.getAssertionsPage().getSubCompomentForSInstance(atts).isNotNull().assertSInstance().isList(0);

        byte[] content = new byte[]{3, 4};
        File tmpFile = tmpProvider.createTempFile(content);

        FormTester formTester = ctx.newFormTester();
        //formTester.setFile(getFormRelativePath(multipleFileField), new org.apache.wicket.util.file.File(tempFile),
        // "text/plain");


        //TODO Passo a serem implementados no teste:
        // * Adicionar um arquivo
        // * Compara se o binario do arquivo adicionado bate com o armazenado na SInstance
        // * Fazer downalod do arquivo
        // * Verificar se a SIntance está consistente
    }

    private static void createType(STypeComposite<?> baseType) {
        baseType.addFieldListOfAttachment("attachments", "attachment").asAtr().label("Attachments");
    }

    @Test
    public void testRemove() throws IOException {
        byte[] content1 = new byte[]{1, 2};
        byte[] content2 = new byte[]{3, 4, 5};

        SingularDummyFormPageTester ctx = createTestPageWithTwoAttachments(content1, content2);
        ctx.getDummyPage().setAsEditView();
        ctx.startDummyPage();

        SIList<SIAttachment> atts = (SIList<SIAttachment>) ctx.getAssertionsInstance().field("attachments").isList(2)
                .getTarget();

        AssertionsWComponent assertAttachs = ctx.getAssertionsPage().getSubCompomentForSInstance(atts).isNotNull();

        FormTester formTester = ctx.newFormTester();
        AssertionsWComponent button = assertDelButton(assertAttachs.getSubCompomentForSInstance(atts.get(0)), true);
        formTester.submit(button.getTarget());

        assertAttachs = ctx.getAssertionsPage().getSubCompomentForSInstance(atts).isNotNull();
        SIAttachment att = assertAttachs.assertSInstance().isList(1).field("[0]").getTarget(SIAttachment.class);
        Assert.assertArrayEquals(content2, att.getContentAsByteArray().get());
    }

    @Test
    public void testReadOnly() throws IOException {
        byte[] content1 = new byte[]{1, 2};
        byte[] content2 = new byte[]{3, 4, 5};

        SingularDummyFormPageTester ctx = createTestPageWithTwoAttachments(content1, content2);
        ctx.getDummyPage().setAsVisualizationView();
        ctx.startDummyPage();

        ctx.getAssertionsPage().debugComponentTree(); //TODO apagar essa linha depois de pronto o teste
        SIList<SIAttachment> atts = (SIList<SIAttachment>) ctx.getAssertionsInstance().field("attachments").isList(2)
                .getTarget();

        AssertionsWComponent assertAttachs = ctx.getAssertionsPage().getSubCompomentForSInstance(atts).isNotNull();

        assertDelButton(assertAttachs.getSubCompomentForSInstance(atts.get(0)), false);
        assertDelButton(assertAttachs.getSubCompomentForSInstance(atts.get(1)), false);
    }

    private AssertionsWComponent assertDelButton(AssertionsWComponent componentAtt, boolean buttonRequired) {
        componentAtt.isNotNull();
        AssertionsWComponent remove = componentAtt.getSubCompomentWithId("remove_btn");
        if (buttonRequired) {
            remove.isNotNull();
            Assert.assertTrue(remove.getTarget().isEnabled() && remove.getTarget().isVisible());
        } else {
            Assert.assertTrue(
                    remove.getTarget() == null || !remove.getTarget().isEnabled() || !remove.getTarget().isVisible());
        }
        return remove;
    }

    @NotNull
    private SingularDummyFormPageTester createTestPageWithTwoAttachments(byte[] content1, byte[] content2) {
        File file1 = tmpProvider.createTempFile(content1);
        File file2 = tmpProvider.createTempFile(content2);

        SingularDummyFormPageTester ctx = new SingularDummyFormPageTester();
        ctx.getDummyPage().setTypeBuilder(AttachmentListMapperTest::createType);
        ctx.getDummyPage().addInstancePopulator(instance -> {
            SIList<SIAttachment> attachments = (SIList<SIAttachment>) instance.getField("attachments");
            attachments.addNew().setContent("teste1.txt", file1, file1.length());
            attachments.addNew().setContent("teste2.txt", file2, file2.length());
        });
        return ctx;
    }
}