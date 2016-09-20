package br.net.mirante.singular.server.commons.flow;

import br.net.mirante.singular.commons.util.Loggable;
import br.net.mirante.singular.flow.core.Flow;
import br.net.mirante.singular.flow.core.renderer.IFlowRenderer;
import br.net.mirante.singular.flow.schedule.IScheduleService;
import br.net.mirante.singular.persistence.util.HibernateSingularFlowConfigurationBean;
import br.net.mirante.singular.server.commons.config.ConfigProperties;
import br.net.mirante.singular.server.commons.config.SingularServerConfiguration;
import br.net.mirante.singular.server.commons.flow.renderer.remote.YFilesFlowRemoteRenderer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class SingularServerFlowConfigurationBean extends HibernateSingularFlowConfigurationBean implements Loggable {

    @Inject
    private SingularServerConfiguration singularServerConfiguration;

    @Inject
    private SessionFactory sessionFactory;

    @Inject
    private PlatformTransactionManager transactionManager;

    @PostConstruct
    protected void postConstruct() {
        this.setProcessGroupCod(singularServerConfiguration.getProcessGroupCod());
        this.setDefinitionsPackages(singularServerConfiguration.getDefinitionsPackages());
        Flow.setConf(this);
        initializeFlowDefinitionsDatabase();
    }

    @Override
    public IFlowRenderer getFlowRenderer() {
//        return JGraphFlowRenderer.INSTANCE;
        return new YFilesFlowRemoteRenderer(null);
    }

    public void initializeFlowDefinitionsDatabase() {
        if ("true".equals(ConfigProperties.get(ConfigProperties.SINGULAR_EAGER_LOAD_FLOW_DEFINITIONS))) {
            TransactionSynchronizationManager.bindResource(this.sessionFactory, sessionFactory.openSession());
            getLogger().info("INITIALIZING FLOW DEFINITIONS");
            singularServerConfiguration.getProcessDefinitionFormNameMap().keySet().stream().forEach(pdclass -> {
                try {
                    getLogger().info("INITIALIZING " + pdclass.getName() + "....");
                    pdclass.newInstance().getEntityProcessVersion();
                } catch (InstantiationException | IllegalAccessException e) {
                    getLogger().error(e.getMessage(), e);
                }
            });
            Session session = (Session) TransactionSynchronizationManager.unbindResource(this.sessionFactory);
            if (session.isOpen()) {
                session.flush();
                session.close();
            }
        }
    }

    @Override
    protected IScheduleService getScheduleService() {
        return new TransactionalQuartzScheduledService(transactionManager);
    }

}