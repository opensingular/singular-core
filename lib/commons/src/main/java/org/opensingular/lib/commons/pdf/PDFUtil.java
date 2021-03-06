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

package org.opensingular.lib.commons.pdf;

import org.apache.commons.collections.CollectionUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.opensingular.internal.lib.commons.util.TempFileProvider;
import org.opensingular.lib.commons.base.SingularProperties;
import org.opensingular.lib.commons.util.Loggable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe utilitária para a manipulação de PDF's.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class PDFUtil implements Loggable {

    /**
     * A constante BEGIN_COMMAND.
     */
    private static final String BEGIN_COMMAND = "/";

    /**
     * A constante SET_FONT.
     */
    private static final String SET_FONT = "Tf\n";

    /**
     * A constante SPACE.
     */
    private static final int SPACE = 32;

    public static final String SINGULAR_WKHTML2PDF_HOME = "singular.wkhtml2pdf.home";

    /**
     * A constante "username".
     */
    protected static String username = null;

    /**
     * A constante "password".
     */
    protected static String password = null;

    /**
     * A constante "proxy".
     */
    protected static String proxy = null;


    /**
     * O caminho no sistema de arquivos para o local onde se encontram as bibliotecas nativas utilizadas
     * por este utilitário de manipulação de PDF's.
     */
    private static File wkhtml2pdfHome;

    /**
     * O tamanho da página. O valor padrão é {@link PageSize#PAGE_A4}.
     */
    private PageSize pageSize = null;

    /**
     * A orientação da página. O valor padrão é {@link PageOrientation#PAGE_PORTRAIT}.
     */
    private PageOrientation pageOrientation = null;

    /**
     * Indica quando necessário esperar por execução javascript na página.
     */
    private int javascriptDelay = 0;

    /**
     * Localiza a implementação correta para o Sistema operacional atual.
     */
    @Nonnull
    private static PDFUtil factory() {
        if (isWindows()) {
            return new PDFUtilWin();
        }
        return new PDFUtilUnix();
    }

    public final static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    final static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac os");
    }

    /**
     * Cria a versão correspondente ao sistema operacional atual.
     */
    @Nonnull
    public static PDFUtil getInstance() {
        return factory();
    }

    /**
     * Converte o código HTML em um arquivo PDF com o cabeçalho e rodapé especificados.
     *
     * @param html   o código HTML.
     * @param header o código HTML do cabeçalho.
     * @param footer o código HTML do rodapé.
     * @return O arquivo PDF retornado é temporário e deve ser apagado pelo solicitante para não deixa lixo.
     */
    @Nonnull
    public File convertHTML2PDF(@Nonnull String html, @Nullable String header, @Nullable String footer)
            throws SingularPDFException {
        return convertHTML2PDF(html, header, footer, null);
    }

    /**
     * Converte o código HTML em um arquivo PDF com o cabeçalho e rodapé especificados.
     *
     * @param rawHtml             o código HTML.
     * @param rawHeader           o código HTML do cabeçalho.
     * @param rawFooter           o código HTML do rodapé.
     * @param additionalConfig configurações adicionais.
     * @return O arquivo PDF retornado é temporário e deve ser apagado pelo solicitante para não deixa lixo.
     */
    @Nonnull
    public final File convertHTML2PDF(String rawHtml, @Nullable String rawHeader, @Nullable String rawFooter,
            @Nullable List<String> additionalConfig) throws SingularPDFException {
        try (TempFileProvider tmp = TempFileProvider.createForUseInTryClause(this)){

            File htmlFile = safeWrapHtmlToFile(rawHtml, tmp, "content.html");
            File headerFile = safeWrapHtmlToFile(rawHeader, tmp, "header.html");
            File footerFile = safeWrapHtmlToFile(rawFooter, tmp, "footer.html");
            File pdfFile  = tmp.createTempFileByDontPutOnDeleteList( "result.pdf");

            return convertHTML2PDF(htmlFile, headerFile, footerFile, additionalConfig, pdfFile);
        }
    }

    @Nonnull
    public final File convertHTML2PDF(@Nonnull File htmlFile, @Nonnull File pdfFile) throws SingularPDFException {
        return convertHTML2PDF(htmlFile, null, null, null, pdfFile);
    }

        /**
         * Converte o código HTML em um arquivo PDF com o cabeçalho e rodapé especificados.
         *
         * @param htmlFile             o código HTML.
         * @param headerFile           o código HTML do cabeçalho.
         * @param footerFile           o código HTML do rodapé.
         * @param additionalConfig configurações adicionais.
         * @return O arquivo PDF retornado é temporário e deve ser apagado pelo solicitante para não deixa lixo.
         */
    @Nonnull
    public final File convertHTML2PDF(@Nullable File htmlFile, @Nullable File headerFile, @Nullable File footerFile,
            @Nullable List<String> additionalConfig, @Nonnull File pdfFile) throws SingularPDFException {
        getWkhtml2pdfHome(); // Força verifica se o Home está configurado corretamente

        try (TempFileProvider tmp = TempFileProvider.createForUseInTryClause(this)) {

            List<String> commandAndArgs = new ArrayList<>(0);
            commandAndArgs.add(getHomeAbsolutePath("bin", fixExecutableName("wkhtmltopdf")));

            if (!CollectionUtils.isEmpty(additionalConfig)) {
                commandAndArgs.addAll(additionalConfig);
                addSmartBreakScript(commandAndArgs);
            } else {
                addDefaultPDFCommandArgs(commandAndArgs);
            }

            if (headerFile != null) {
                commandAndArgs.add("--header-html");
                commandAndArgs.add(fixPathArg(headerFile));
                addDefaultHeaderCommandArgs(commandAndArgs);
            }

            if (footerFile != null) {
                commandAndArgs.add("--footer-html");
                commandAndArgs.add(fixPathArg(footerFile));
                addDefaultFooterCommandArgs(commandAndArgs);
            }

            commandAndArgs.add(fixPathArg(htmlFile));
            commandAndArgs.add(pdfFile.getAbsolutePath());

            return runProcess(commandAndArgs, pdfFile);
        }
    }

    /**
     * Converte o código HTML em um arquivo PNG.
     *
     * @param html             o código HTML.
     * @param additionalConfig configurações adicionais.
     * @return O arquivo PDF retornado é temporário e deve ser apagado pelo solicitante para não deixa lixo.
     */
    @Nonnull
    public final File convertHTML2PNG(@Nonnull String html, @Nullable List<String> additionalConfig)
            throws SingularPDFException {
        getWkhtml2pdfHome(); // Força verifica se o Home está configurado corretamente

        try (TempFileProvider tmp = TempFileProvider.createForUseInTryClause(this)) {

            File htmlFile = tmp.createTempFile("content.html");
            writeToFile(htmlFile, html);

            List<String> commandAndArgs = new ArrayList<>();
            commandAndArgs.add(getHomeAbsolutePath("bin", fixExecutableName("wkhtmltoimage")));

            if (additionalConfig != null) {
                commandAndArgs.addAll(additionalConfig);
            } else {
                addDefaultPNGCommandArgs(commandAndArgs);
            }

            File pngFile = tmp.createTempFileByDontPutOnDeleteList("result.png");

            //File jarFile = tmp.createTempFile("cookie.txt", true);
            //commandAndArgs.add("--cookie-jar");
            //commandAndArgs.add(jarFile.getAbsolutePath());

            commandAndArgs.add(fixPathArg(htmlFile));
            commandAndArgs.add(pngFile.getAbsolutePath());

            return runProcess(commandAndArgs, pngFile);
        }
    }

    /**
     * Converte o código HTML em um arquivo PDF.
     *
     * @param html o código HTML.
     * @return O arquivo PDF retornado é temporário e deve ser apagado pelo solicitante para não deixa lixo.
     */
    @Nonnull
    public File convertHTML2PDF(@Nonnull String html) throws SingularPDFException {
        return convertHTML2PDF(html, null);
    }

    /**
     * Converte o código HTML em um arquivo PNG.
     *
     * @param html o código HTML.
     * @return O arquivo PNG retornado é temporário e deve ser apagado pelo solicitante para não deixa lixo.
     */
    @Nonnull
    public File convertHTML2PNG(@Nonnull String html) throws SingularPDFException {
        return convertHTML2PNG(html, null);
    }

    /**
     * Converte o código HTML em um arquivo PDF com o cabeçalho especificado.
     *
     * @param html   o código HTML.
     * @param header o código HTML do cabeçalho.
     * @return O arquivo PDF retornado é temporário e deve ser apagado pelo solicitante para não deixa lixo.
     */
    @Nonnull
    public File convertHTML2PDF(@Nonnull String html, @Nullable String header) throws SingularPDFException {
        return convertHTML2PDF(html, header, null);
    }

    /**
     * Adiciona os argumentos padrões para a geração do PDF.
     *
     * @param commandArgs o vetor com os argumentos.
     */
    private void addDefaultPDFCommandArgs(List<String> commandArgs) {
        commandArgs.add("--print-media-type");
        commandArgs.add("--load-error-handling");
        commandArgs.add("ignore");

        if (pageSize != null) {
            commandArgs.add("--page-size");
            commandArgs.add(pageSize.getValue());
        }
        if (pageOrientation != null) {
            commandArgs.add("--orientation");
            commandArgs.add(pageOrientation.getValue());
        }
        if (javascriptDelay > 0) {
            commandArgs.add("--javascript-delay");
            commandArgs.add(String.valueOf(javascriptDelay));
        }

        addSmartBreakScript(commandArgs);

    }

    /**
     * adiciona um script minificado de break de texto com mais de 1000 caracteres de forma automatica,
     * segue em comentario a versão original
     * <p>
     * (function () {
     * function preventBreakWrap(value) {
     * return '<span style=\'page-break-inside: avoid\'>' + value + '</span>';
     * }
     * function breakInBlocks(value, size) {
     * if (value.length > size) {
     * return preventBreakWrap(value.substr(0, size)) + breakInBlocks(value.substr(size, value.length), size);
     * }
     * return value;
     * }
     * function visitLeafs(root, visitor) {
     * if (root.children.length == 0) {
     * visitor(root);
     * } else {
     * for (var i = 0; i < root.children.length; i += 1) {
     * visitLeafs(root.children[i], visitor);
     * }
     * }
     * }
     * visitLeafs(document.getElementsByTagName('body')[0], function(e) {
     * e.innerHTML = breakInBlocks(e.innerHTML, 1000);
     * });
     * })();
     *
     * @param commandArgs os argumentos
     */
    private void addSmartBreakScript(List<String> commandArgs) {
        final String minificado = "\"!function(){function a(a){return'<span style=\\\'page-break-inside: avoid\\\'>'+" +
                "a+'</span>'}function b(c,d){return c.length>d?a(c.substr(0,d))+b(c.substr(d,c.length),d):c}function " +
                "c(a,b){if(0==a.children.length)b(a);else for(var d=0;d<a.children.length;d+=1)c(a.children[d],b)}c(d" +
                "ocument.getElementsByTagName('body')[0],function(a){a.innerHTML=b(a.innerHTML,600)})}();\"";
        commandArgs.add("--run-script");
        commandArgs.add(minificado);
    }

    /**
     * Adiciona os argumentos padrões para a geração do PNG.
     *
     * @param commandArgs o vetor com os argumentos.
     */
    private void addDefaultPNGCommandArgs(List<String> commandArgs) {
        commandArgs.add("--format");
        commandArgs.add("png");
        commandArgs.add("--load-error-handling");
        commandArgs.add("ignore");

        if (username != null) {
            commandArgs.add("--username");
            commandArgs.add(username);
        }
        if (password != null) {
            commandArgs.add("--password");
            commandArgs.add(password);
        }
        if (proxy != null) {
            commandArgs.add("--proxy");
            commandArgs.add(proxy);
        }
        if (javascriptDelay > 0) {
            commandArgs.add("--javascript-delay");
            commandArgs.add(String.valueOf(javascriptDelay));
        }
    }

    /**
     * Adiciona os argumentos padrões para a geração do cabeçalho.
     *
     * @param commandArgs o vetor com os argumentos.
     */
    private void addDefaultHeaderCommandArgs(List<String> commandArgs) {
        commandArgs.add("--header-spacing");
        commandArgs.add("5");
    }

    /**
     * Adiciona os argumentos padrões para a geração do rodapé.
     *
     * @param commandArgs o vetor com os argumentos.
     */
    private void addDefaultFooterCommandArgs(List<String> commandArgs) {
        commandArgs.add("--footer-spacing");
        commandArgs.add("5");
    }

    /**
     * Altera o valor do atributo {@link #pageSize}.
     *
     * @param pageSize o novo valor a ser utilizado para "page size".
     */
    public void setPageSize(PageSize pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Altera o valor do atributo {@link #pageOrientation}.
     *
     * @param pageOrientation o novo valor a ser utilizado para "page orientation".
     */
    public void setPageOrientation(PageOrientation pageOrientation) {
        this.pageOrientation = pageOrientation;
    }

    /**
     * Altera o valor do atributo {@link #javascriptDelay}.
     *
     * @param javascriptDelay o novo valor a ser utilizado para "javascript delay".
     */
    public void setJavascriptDelay(int javascriptDelay) {
        this.javascriptDelay = javascriptDelay;
    }

    /**
     * O enumerador para o tamanho da página.
     */
    public enum PageSize {
        PAGE_A3("A3"),
        PAGE_A4("A4"),
        PAGE_LETTER("Letter");

        private String value;

        PageSize(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * O enumerador para a orientação da página.
     */
    public enum PageOrientation {
        PAGE_PORTRAIT("Portrait"),
        PAGE_LANDSCAPE("Landscape");

        private String value;

        PageOrientation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Concatena os pdf em um único PDF.
     * @param pdfs
     * @return O arquivo retornado é temporário e deve ser apagado pelo solicitante para não deixa lixo.
     */
    @Nonnull
    public File merge(@Nonnull List<InputStream> pdfs) throws SingularPDFException {
        try(TempFileProvider tmp = TempFileProvider.createForUseInTryClause(this)) {
            PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
            pdfs.forEach(pdfMergerUtility::addSource);

            File tempMergedFile = tmp.createTempFileByDontPutOnDeleteList("merge.pdf");

            try (FileOutputStream output = new FileOutputStream(tempMergedFile)) {
                pdfMergerUtility.setDestinationStream(output);
                pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                return tempMergedFile;
            }
        } catch (Exception e) {
            throw new SingularPDFException("Erro realizando merge de arquivos PDF", e);
        } finally {
            for(InputStream in : pdfs) {
                try {
                    in.close();
                } catch (IOException e) {
                    getLogger().error("Erro fechando inputStrem", e);
                }
            }
        }
    }

    private File safeWrapHtmlToFile(String rawHtml, TempFileProvider tmp, String name) {
        if (rawHtml == null) {
            return null;
        }
        File file = tmp.createTempFile( name);
        writeToFile(file, safeWrapHtml(rawHtml));
        return file;
    }


    private String safeWrapHtml(String html) {
        if (html == null || html.startsWith(("<!DOCTYPE"))) {
            return html;
        }
        String  wraped   = html;
        boolean needHTML = !html.startsWith("<html>");
        boolean needBody = needHTML && !html.startsWith("<body>");
        if (needBody) {
            wraped = "<body>" + wraped + "<body>";
        }
        if (needHTML) {
            wraped = "<!DOCTYPE HTML><html>" + wraped + "</html>";
        }
        return wraped;
    }

    protected static final @Nonnull File getWkhtml2pdfHome() {
        if (wkhtml2pdfHome == null) {
            Optional<String> prop = SingularProperties.get().getPropertyOpt(SINGULAR_WKHTML2PDF_HOME);

            if (!prop.isPresent()) {
                throw new SingularPDFException("property 'singular.wkhtml2pdf.home' not set");
            }
            File file = new File(prop.get());
            if (! file.exists()) {
                throw new SingularPDFException(
                        "property '" + SINGULAR_WKHTML2PDF_HOME + "' configured for a directory that nos exists: " +
                                file.getAbsolutePath());
            }
            wkhtml2pdfHome = file;
        }
        return wkhtml2pdfHome;
    }

    final static void clearHome() {
        wkhtml2pdfHome = null;
    }

    @Nonnull
    private static final String getHomeAbsolutePath(@Nullable String subDir, @Nonnull String file)
            throws SingularPDFException {
        File arq = getWkhtml2pdfHome();
        if (subDir == null) {
            arq = new File(arq, file);
        } else {
            arq = new File(arq, subDir + File.separator + file);
        }
        if (!arq.exists()) {
            throw new SingularPDFException("Arquivo ou diretório '" + arq.getAbsolutePath() + "' não encontrado.");
        }
        return arq.getAbsolutePath();
    }

    // -------------------------------------------------------------------
    // Método para customização de acordo com o sistema operacional
    // -------------------------------------------------------------------

    /** Permite ajustar o nome do executável se necessário no sistema operacional em questão. */
    protected String fixExecutableName(String executable) {
        return executable;
    }


    /** Permite ajustar o path do arquivo se necessário no sistema operacional em questão. */
    protected @Nonnull String fixPathArg(@Nullable File arq) {
        if(arq != null) {
            return arq.getAbsolutePath();
        }
        throw new SingularPDFException("Nenhum diretorio informado");
    }

    /**
     * Executa o comando inforamdo e verifica se o arquivo esperado foi de fato gerado. Dispara exception se houver erro
     * na execução ou se o arquivo não for gerado.
     */
    protected abstract
    @Nonnull
    File runProcess(@Nonnull List<String> commandAndArgs, @Nonnull File expectedFile) throws SingularPDFException;

    /** Escreve o conteúdo informado no arquivo indicado. */
    protected abstract void writeToFile(File destination, String content) throws SingularPDFException;
}