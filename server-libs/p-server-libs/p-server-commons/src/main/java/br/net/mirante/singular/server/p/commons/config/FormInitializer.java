package br.net.mirante.singular.server.p.commons.config;

import br.net.mirante.singular.form.spring.SpringSDocumentFactory;
import br.net.mirante.singular.form.spring.SpringTypeLoader;
import br.net.mirante.singular.persistence.util.HibernateSingularFlowConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;

public abstract class FormInitializer {

    public static final Logger logger = LoggerFactory.getLogger(FormInitializer.class);
    static final String SINGULAR_FORM = "[SINGULAR][FORM] %s";

    protected abstract Class<? extends SpringSDocumentFactory> documentFactory();

    protected abstract Class<? extends SpringTypeLoader> typeLoader();

    protected abstract Class<? extends HibernateSingularFlowConfigurationBean> flowConfigurationBean();

    public void init(ServletContext ctx, AnnotationConfigWebApplicationContext applicationContext) {
        Class<?> documentFactory = documentFactory();
        if (documentFactory != null) {
            applicationContext.register(documentFactory);
        } else {
            logger.info(String.format(SINGULAR_FORM, " Null Form Document Factory, skipping Form Document Factory configuration. "));
        }

        Class<?> typeLoader = typeLoader();
        if (typeLoader != null) {
            applicationContext.register(typeLoader);
        } else {
            logger.info(String.format(SINGULAR_FORM, " Null Form Type Loader, skipping Form Type Loader configuration. "));
        }

        Class<?> flowConfigurationBean = flowConfigurationBean();
        if (flowConfigurationBean != null) {
            applicationContext.register(flowConfigurationBean);
        } else {
            logger.info(String.format(SINGULAR_FORM, " Null Flow Configuration Bean, skipping Flow Configuration. "));
        }
    }


}