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

package org.opensingular.form.type.core.attachment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.io.HashUtil;
import org.opensingular.internal.lib.commons.util.SingularIOUtils;
import org.opensingular.lib.commons.junit.AbstractTestTempFileSupport;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opensingular.form.type.core.attachment.BaseAttachmentPersistenceFilesTest.getBytes;

public abstract class TestCasePersistenceHandlerBase extends AbstractTestTempFileSupport {

    // @formatter:off
    private final byte[][] conteudos = new byte[][]{"i".getBytes(), "np".getBytes(), "1234".getBytes()};
    private final String[] fileNames = new String[]{"i.txt", "np.txt", "1234.txt"};
    private final String[] hashs     = new String[]{"042dc4512fa3d391c5170cf3aa61e6a638f84342", "003fffd5649fc27c0fc0d15a402a4fe5b0444ce7", "7110eda4d09e062aa5e4a390b0a572ac0d2c0220"};
    private IAttachmentPersistenceHandler persistenHandler;
    // @formatter:on

    private static void assertConteudo(IAttachmentPersistenceHandler handler, IAttachmentRef ref, byte[] conteudoEsperado, String hashEsperado, int sizeEsperado) throws IOException {
        assertEquals(hashEsperado, ref.getHashSHA1());
        assertEquals(hashEsperado, ref.getId());
        assertEquals(sizeEsperado, handler.getAttachments().size());
        assertTrue(Arrays.equals(conteudoEsperado, getBytes(ref)));
    }

    protected final IAttachmentPersistenceHandler getHandler() {
        if (persistenHandler == null) {
            persistenHandler = setupHandler();
        }
        return persistenHandler;
    }

    /**
     * Se chamado mais de uma vez, deve retornar contextos diferente.
     */
    protected abstract IAttachmentPersistenceHandler setupHandler();

    @After
    public void limpeza() {
        persistenHandler = null;
    }

    @Test
    public void testSerializacao() throws IOException, ClassNotFoundException {
        IAttachmentRef[] refs = new IAttachmentRef[conteudos.length];
        for (int i = 0; i < conteudos.length; i++) {
            refs[i] = getHandler().addAttachment(getTempFileProvider().createTempFile(conteudos[i]), conteudos[i].length, fileNames[i], HashUtil.toSHA1Base16(conteudos[i]));
        }

        for (int i = 0; i < conteudos.length; i++) {
            IAttachmentRef ref = refs[i];
            IAttachmentRef ref2 = SingularIOUtils.serializeAndDeserialize(ref);
            IAttachmentRef ref3 = getHandler().getAttachment(ref.getId());

            assertThat(ref2).isNotNull();
            assertThat(getBytes(ref2)).isEqualTo(conteudos[i]);
            assertThat(ref2.getHashSHA1()).isEqualTo(hashs[i]);

            assertThat(ref3).isNotNull();
            assertThat(getBytes(ref3)).isEqualTo(conteudos[i]);
            assertThat(ref3.getHashSHA1()).isEqualTo(hashs[i]);
        }
    }

    @Test
    @Ignore("Review this test")
    public void testIndependenciaDeleteEntreContextosDiferentes() throws IOException {
        IAttachmentPersistenceHandler handler1 = getHandler();
        IAttachmentPersistenceHandler handler2 = setupHandler();
        assertNotEquals(handler1, handler2);

        IAttachmentRef ref11 = handler1.addAttachment(getTempFileProvider().createTempFile(conteudos[1]), conteudos[1].length, fileNames[1], HashUtil.toSHA1Base16(conteudos[1]));
        IAttachmentRef ref12 = handler1.addAttachment(getTempFileProvider().createTempFile(conteudos[2]), conteudos[2].length, fileNames[2], HashUtil.toSHA1Base16(conteudos[2]));
        IAttachmentRef ref13 = handler1.addAttachment(getTempFileProvider().createTempFile(conteudos[0]), conteudos[0].length, fileNames[0], HashUtil.toSHA1Base16(conteudos[0]));
        assertConteudo(handler1, ref13, conteudos[0], hashs[0], 3);

        IAttachmentRef ref21 = handler2.addAttachment(getTempFileProvider().createTempFile(conteudos[1]), conteudos[1].length, fileNames[1], HashUtil.toSHA1Base16(conteudos[1]));
        IAttachmentRef ref22 = handler2.addAttachment(getTempFileProvider().createTempFile(conteudos[2]), conteudos[2].length, fileNames[2], HashUtil.toSHA1Base16(conteudos[2]));
        assertConteudo(handler2, ref22, conteudos[2], hashs[2], 2);

        handler2.deleteAttachment(ref21.getHashSHA1(), null);
        assertNull(handler2.getAttachment(ref11.getId()));
        assertConteudo(handler2, ref22, conteudos[2], hashs[2], 1);
        assertConteudo(handler1, ref11, conteudos[1], hashs[1], 3);
        assertConteudo(handler1, handler1.getAttachment(ref11.getId()), conteudos[1], hashs[1], 3);

        handler1.deleteAttachment(ref12.getHashSHA1(), null);
        assertNull(handler1.getAttachment(ref12.getId()));
        assertConteudo(handler2, ref22, conteudos[2], hashs[2], 1);
        assertConteudo(handler2, handler2.getAttachment(ref22.getId()), conteudos[2], hashs[2], 1);
    }

    @Test
    @Ignore("Review this test")
    public void testCopiaEntreContextosDiferentesComDeletesDepois() throws IOException {
        IAttachmentPersistenceHandler handler1 = getHandler();
        IAttachmentPersistenceHandler handler2 = setupHandler();
        assertNotEquals(handler1, handler2);


        handler1.addAttachment(getTempFileProvider().createTempFile(conteudos[1]), conteudos[1].length, fileNames[1], HashUtil.toSHA1Base16(conteudos[1]));
        IAttachmentRef ref12o = handler1.addAttachment(getTempFileProvider().createTempFile(conteudos[2]), conteudos[2].length, fileNames[2], HashUtil.toSHA1Base16(conteudos[2]));
        IAttachmentRef ref21o = handler2.addAttachment(getTempFileProvider().createTempFile(conteudos[1]), conteudos[1].length, fileNames[1], HashUtil.toSHA1Base16(conteudos[1]));

//         Apagando na origem
        IAttachmentRef ref22c = handler2.copy(ref12o, null).getNewAttachmentRef();
        assertConteudo(handler2, ref22c, conteudos[2], hashs[2], 2);
        assertConteudo(handler2, handler2.getAttachment(hashs[2]), conteudos[2], hashs[2], 2);

        handler1.deleteAttachment(hashs[2], null);
        assertNull(handler1.getAttachment(hashs[2]));
        assertConteudo(handler2, ref22c, conteudos[2], hashs[2], 2);
        assertConteudo(handler2, handler2.getAttachment(hashs[2]), conteudos[2], hashs[2], 2);

        // apagando no destino
        IAttachmentRef ref11c = handler1.copy(ref21o, null).getNewAttachmentRef();
        assertConteudo(handler1, ref11c, conteudos[1], hashs[1], 1);
        assertConteudo(handler1, handler1.getAttachment(hashs[1]), conteudos[1], hashs[1], 1);

        handler1.deleteAttachment(hashs[1], null);
        assertNull(handler1.getAttachment(hashs[1]));
        assertConteudo(handler2, ref21o, conteudos[1], hashs[1], 2);
        assertConteudo(handler2, handler2.getAttachment(hashs[1]), conteudos[1], hashs[1], 2);
    }

    @Test
    @Ignore("To be implemented")
    public void testLeituraComHashViolado() {
        fail("implementar");
    }

    @Test
    @Ignore("To be implemented")
    public void testCompactacaoConteudoInterno() {
        fail("implementar");
    }

    @Test
    public void testExceptionNaEscritaDoConteudo() throws IOException {
        try {
            getHandler().addAttachment(new File(""), 1, "teste.txt", "");
            fail("Era esperada Exception");
        } catch (SingularFormException e) {
            Assert.assertTrue(e.getMessage().contains("Erro lendo origem de dados"));
        }
        assertEquals(0, getHandler().getAttachments().size());
    }

    @Test
    public void deletedFileIsNoLongerAvailable() throws IOException {
        byte[] b = {1, 2};
        IAttachmentRef ref = getHandler().addAttachment(getTempFileProvider().createTempFile(b), 2l, "testando.txt", HashUtil.toSHA1Base16(b));
        getHandler().deleteAttachment(ref.getId(), null);
        assertThat(getHandler().getAttachment(ref.getId())).isNull();
    }

    @Test
    public void doesNothingWhenYouTryToDeleteANullFile() {
        getHandler().deleteAttachment(null, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deleteOnlyTheDesiredFile() throws IOException {
        byte[] c1;
        byte[] c2;
        byte[] c3;
        getHandler().addAttachment(getTempFileProvider().createTempFile(c1 = new byte[]{1, 2, 3}), 3l, "testando1.txt", HashUtil.toSHA1Base16(c1));
        IAttachmentRef ref = getHandler().addAttachment(getTempFileProvider().createTempFile(c2 = new byte[]{1, 2}), 2l, "testando2.txt", HashUtil.toSHA1Base16(c2));
        getHandler().addAttachment(getTempFileProvider().createTempFile(c3 = new byte[]{1, 2, 4, 5}), 4l, "testando3.txt", HashUtil.toSHA1Base16(c3));

        getHandler().deleteAttachment(ref.getId(), null);

        assertThat((Collection<IAttachmentRef>) getHandler().getAttachments()).hasSize(2)
                .doesNotContain(ref);
    }


}
