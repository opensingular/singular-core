package org.opensingular.singular.server.commons.config;


import org.opensingular.flow.core.ProcessDefinition;
import org.opensingular.form.SType;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.*;

/**
 * Spring Bean para guardar parametros de configuração reutilizáveis
 * para a solução do singular
 */
public class SingularServerConfiguration implements ServletContextAware {


    private IServerContext[] contexts;
    private String           springMVCServletMapping;
    private Map<String, Object> attrs = new HashMap<>();
    private List<Class<? extends SType<?>>> formTypes;
    private String                          processGroupCod;
    private String[]                        definitionsPackages;
    private Map<Class<? extends ProcessDefinition>, String> processDefinitionFormNameMap = new HashMap<>(0);
    private String[] defaultPublicUrls;

    public String[] getDefaultPublicUrls() {
        return defaultPublicUrls;
    }

    public IServerContext[] getContexts() {
        return contexts;
    }

    public String getSpringMVCServletMapping() {
        return springMVCServletMapping;
    }

    public Object setAttribute(String name, Object value) {
        return attrs.put(name, value);
    }

    public Object getAttribute(String name) {
        return attrs.get(name);
    }

    public List<Class<? extends SType<?>>> getFormTypes() {
        return Collections.unmodifiableList(formTypes);
    }

    public String getProcessGroupCod() {
        return processGroupCod;
    }

    public String[] getDefinitionsPackages() {
        return definitionsPackages;
    }

    public Map<Class<? extends
            ProcessDefinition>, String> getProcessDefinitionFormNameMap() {
        return Collections.unmodifiableMap(processDefinitionFormNameMap);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        WebInitializer             webInitializer             = (WebInitializer) servletContext.getAttribute(SingularInitializer.SERVLET_ATTRIBUTE_WEB_CONFIGURATION);
        SpringHibernateInitializer springHibernateInitializer = (SpringHibernateInitializer) servletContext.getAttribute(SingularInitializer.SERVLET_ATTRIBUTE_SPRING_HIBERNATE_CONFIGURATION);
        FormInitializer            formInitializer            = (FormInitializer) servletContext.getAttribute(SingularInitializer.SERVLET_ATTRIBUTE_FORM_CONFIGURATION_CONFIGURATION);
        FlowInitializer            flowInitializer            = (FlowInitializer) servletContext.getAttribute(SingularInitializer.SERVLET_ATTRIBUTE_FLOW_CONFIGURATION_CONFIGURATION);
        this.contexts = webInitializer.serverContexts();
        this.defaultPublicUrls = webInitializer.getDefaultPublicUrls();
        this.springMVCServletMapping = springHibernateInitializer.springMVCServletMapping();
        Optional.ofNullable(formInitializer)
                .ifPresent(fi -> this.formTypes = fi.getTypes());
        Optional.ofNullable(flowInitializer)
                .ifPresent(fi -> this.processGroupCod = fi.processGroupCod());
        Optional.ofNullable(flowInitializer)
                .ifPresent(fi -> this.processDefinitionFormNameMap = fi.processDefinitionFormNameMap());
        Optional.ofNullable(flowInitializer)
                .ifPresent(fi -> this.definitionsPackages = fi.definitionsBasePackage());
    }
}