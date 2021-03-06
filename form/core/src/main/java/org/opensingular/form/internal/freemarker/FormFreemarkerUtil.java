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

package org.opensingular.form.internal.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.opensingular.form.SInstance;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.calculation.SimpleValueCalculation;
import org.opensingular.form.document.SDocument;
import org.opensingular.lib.commons.base.SingularProperties;
import org.opensingular.lib.commons.context.SingularContext;
import org.opensingular.lib.commons.context.SingularSingletonStrategy;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Integra o Singular Form com o Freemarker
 * <a href="http://freemarker.incubator.apache.org">http://freemarker.incubator.
 * apache.org</a> permitindo fazer o merge de template do freemaker com os dados
 * de uma instância do formulário.
 *
 * @author Daniel C. Bordin
 */
public final class FormFreemarkerUtil {

    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";

    private Configuration cfgIgnoreError;
    private Configuration cfgRethrowError;

    private Consumer<Configuration> configurationConsumer = null;

    private FormFreemarkerUtil() {
    }

    public static FormFreemarkerUtil get() {
        return ((SingularSingletonStrategy) SingularContext.get()).singletonize(FormFreemarkerUtil.class, FormFreemarkerUtil::new);
    }

    public static FormFreemarkerUtil get(Consumer<Configuration> configurationConsumer) {
        FormFreemarkerUtil formFreemarkerUtil = new FormFreemarkerUtil();
        formFreemarkerUtil.configurationConsumer = configurationConsumer;
        return formFreemarkerUtil;
    }

    public SimpleValueCalculation<String> createInstanceCalculation(String stringTemplate) {
        return context -> merge(context.instanceContext(), stringTemplate);
    }

    public SimpleValueCalculation<String> createInstanceCalculation(String stringTemplate, boolean escapeContentHtml, boolean ignoreError) {
        return context -> merge(context.instanceContext(), stringTemplate, escapeContentHtml, ignoreError);
    }

    /**
     * Gera uma string resultante do merge do template com os dados contídos no
     * documento informado. É o mesmo que merge(document.getRoot(),
     * templateString).
     */
    public String merge(SDocument document, String templateString) {
        return merge(document.getRoot(), templateString);
    }

    /**
     * Gera uma string resultante do merge do template com os dados contídos na
     * instancia informada.
     */
    public String merge(SInstance dataSource, String templateString) {
        return internalMerge(dataSource, templateString, false, false);
    }

    /**
     * Gera uma string resultante do merge do template com os dados contídos na
     * instancia informada podendo ignorar os erros.
     */
    public String merge(SInstance dataSource, String templateString, boolean escapeContentHtml, boolean ignoreError) {
        return internalMerge(dataSource, templateString, escapeContentHtml, ignoreError);
    }

    private String internalMerge(SInstance dataSource, String templateString, boolean escapeContentHtml, boolean ignoreError) {
        return merge(dataSource, escapeContentHtml, parseTemplate(templateString, ignoreError));
    }

    public String merge(SInstance dataSource, boolean escapeContentHtml, Template template) {
        StringWriter out = new StringWriter();
        try {
            template.process(dataSource, out, new FormObjectWrapper(escapeContentHtml));
        } catch (TemplateException | IOException e) {
            throw new SingularFormException("Erro mesclando dados da instancia com o template: " + template, e);
        }
        return out.toString();
    }

    private Template parseTemplate(String template, boolean ignoreError) {
        try {
            TemplateExceptionHandler exceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER;
            Optional<String> property = SingularProperties.getOpt(SingularProperties.FREEMARKER_IGNORE_ERROR);
            if (property.isPresent() && FALSE.equalsIgnoreCase(property.get())) {
                exceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER;
            } else if ((property.isPresent() && TRUE.equalsIgnoreCase(property.get())) || ignoreError) {
                exceptionHandler = TemplateExceptionHandler.IGNORE_HANDLER;
            }

            return new Template("templateStringParameter", template, getConfiguration(exceptionHandler));
        } catch (IOException e) {
            throw new SingularFormException("Erro fazendo parse do template: " + template, e);
        }
    }

    private Configuration getConfiguration(TemplateExceptionHandler exceptionHandler) {
        Configuration c = null;
        if (exceptionHandler != null) {
            if (exceptionHandler == TemplateExceptionHandler.IGNORE_HANDLER) {
                if (cfgIgnoreError == null) {
                    cfgIgnoreError = newConfiguration(exceptionHandler);
                }
                c = cfgIgnoreError;
            } else {
                if (cfgRethrowError == null) {
                    cfgRethrowError = newConfiguration(exceptionHandler);
                }
                c = cfgRethrowError;
            }
        }
        return c;
    }

    private Configuration newConfiguration(TemplateExceptionHandler exceptionHandler) {
        Configuration conf = new Configuration(Configuration.VERSION_2_3_22);
        conf.setDefaultEncoding(StandardCharsets.UTF_8.name());
        conf.setLocale(new Locale("pt", "BR"));
        conf.setTemplateExceptionHandler(exceptionHandler);
        if (configurationConsumer != null) {
            configurationConsumer.accept(conf);
        }
        return conf;
    }

}
