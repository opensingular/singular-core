package br.net.mirante.singular.form.mform;

import java.util.List;
import java.util.Objects;

import junit.framework.TestCase;

public abstract class TestCaseForm extends TestCase {

    protected static void testCaminho(SInstance registro, String path, String caminhoCompletoEsperado) {
        SInstance esperada = (path == null) ? registro : ((ICompositeInstance) registro).getField(path);
        assertNotNull(esperada);
        String caminho = esperada.getPathFromRoot();
        assertEquals(caminhoCompletoEsperado, caminho);

        String esperadoFull;
        SInstance raiz = registro.getDocument().getRoot();
        if (caminho == null) {
            esperadoFull = raiz.getName();
        } else if (raiz instanceof SIList) {
            esperadoFull = raiz.getName() + caminho;
        } else {
            esperadoFull = raiz.getName() + "." + caminho;
        }
        assertEquals(esperadoFull, esperada.getPathFull());

        if (caminho != null) {
            assertEquals(esperada, ((ICompositeInstance) registro.getDocument().getRoot()).getField(caminho));
        }
    }

    protected static <R extends SInstance & ICompositeInstance> void testAtribuicao(R registro, String path, Object valor,
                                                                                    int qtdFilhosEsperados) {
        testAtribuicao(registro, path, valor);
        assertFilhos(registro, qtdFilhosEsperados);
    }

    protected static <R extends SInstance & ICompositeInstance> void testAtribuicao(R registro, String path, Object valor) {
        registro.setValue(path, valor);
        assertEquals(valor, registro.getValue(path));
    }

    protected static void assertEqualsList(Object valor, Object... valoresEsperados) {
        if (!(valor instanceof List)) {
            throw new RuntimeException("Não é uma lista");
        }
        List<?> valores = (List<?>) valor;
        assertEquals(valores.size(), valoresEsperados.length);
        for (int i = 0; i < valoresEsperados.length; i++) {
            if (!Objects.equals(valoresEsperados[i], valores.get(i))) {
                throw new RuntimeException(
                        "Valores diferentes na posição " + i + ": era esparado " + valoresEsperados[i] + " e veio " + valores.get(i));
            }
        }
    }

    /**
     * Faz alguns verifições quanto a integridade dos filhos;
     */
    protected static void assertFilhos(SInstance pai, int qtdFilhosEsperados) {
        int[] counter = new int[1];
        assertNotNull(pai.getDocument());
        assertFilhos(pai, pai, counter);
        assertEquals(qtdFilhosEsperados, counter[0]);

        SInstance atual = pai;
        while (atual != null) {
            assertEquals(pai.getDocument(), atual.getDocument());
            if (atual.getParent() == null) {
                assertEquals(atual, pai.getDocument().getRoot());
            }
            atual = atual.getParent();
        }

    }

    private static void assertFilhos(SInstance raiz, SInstance pai, int[] counter) {
        if (pai instanceof ICompositeInstance) {
            for (SInstance filho : ((ICompositeInstance) pai).getChildren()) {
                assertEquals(raiz.getDocument(), filho.getDocument());
                assertEquals(pai, filho.getParent());
                counter[0]++;
                assertFilhos(raiz, filho, counter);
            }
        }
    }

    public static void assertException(Runnable acao, String trechoMsgEsperada) {
        assertException(acao, RuntimeException.class, trechoMsgEsperada, null);
    }

    public static void assertException(Runnable acao, String trechoMsgEsperada, String msgFailException) {
        assertException(acao, RuntimeException.class, trechoMsgEsperada, msgFailException);
    }

    public static void assertException(Runnable acao, Class<? extends Exception> exceptionEsperada) {
        assertException(acao, exceptionEsperada, null, null);
    }

    public static void assertException(Runnable acao, Class<? extends Exception> exceptionEsperada, String trechoMsgEsperada,
            String msgFailException) {
        try {
            acao.run();
            String msg = "Não ocorreu nenhuma Exception. Era esperado " + exceptionEsperada.getSimpleName() + "'";
            if (trechoMsgEsperada != null) {
                msg += " com mensagem contendo '" + trechoMsgEsperada + "'";
            }
            if (msgFailException != null) {
                msg += ", pois " + msgFailException;
            }
            fail(msg);
        } catch (Exception e) {
            if (exceptionEsperada.isInstance(e)) {
                if (trechoMsgEsperada == null || (e.getMessage() != null && e.getMessage().contains(trechoMsgEsperada))) {
                    return;
                }
            }
            throw e;
        }

    }
}