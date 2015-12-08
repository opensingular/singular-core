package br.net.mirante.singular.view.page.form.crud.services;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import br.net.mirante.singular.form.mform.document.ServiceRegistry;

/**
 * This class provides a {@link ServiceRegistry} that relays service lookup
 * to the spring context.
 * 
 * @author Fabricio Buzeto
 *
 */
@Component("springRegistry")
public class SpringServiceRegistry implements ServiceRegistry,
    ApplicationContextAware{
        
        private ApplicationContext applicationContext;
        
        @Override
        public Map<String, Pair> services() {
            return Maps.newHashMap();
        }

        @Override
        public <T> T lookupService(String name, Class<T> targetClass) {
            return applicationContext.getBean(name, targetClass);
        }
        
        @Override
        public <T> T lookupService(Class<T> targetClass) {
            return applicationContext.getBean(targetClass);
        }

        @Override
        public Object lookupService(String name) {
            return applicationContext.getBean(name);
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

    }