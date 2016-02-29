package br.net.mirante.singular.view.page.processo;

import static br.net.mirante.singular.util.wicket.util.WicketUtils.$m;

import java.util.Iterator;

import javax.inject.Inject;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import br.net.mirante.singular.flow.core.authorization.AccessLevel;
import br.net.mirante.singular.flow.core.dto.IDefinitionDTO;
import br.net.mirante.singular.flow.core.dto.IInstanceDTO;
import br.net.mirante.singular.service.UIAdminFacade;
import br.net.mirante.singular.util.wicket.datatable.BSDataTableBuilder;
import br.net.mirante.singular.util.wicket.datatable.BaseDataProvider;
import br.net.mirante.singular.view.SingularWicketContainer;
import br.net.mirante.singular.view.page.dashboard.DashboardPage;
import br.net.mirante.singular.view.template.Content;

public class InstanciasContent extends Content implements SingularWicketContainer<InstanciasContent, Void> {

    @Inject
    private UIAdminFacade uiAdminFacade;

    private IDefinitionDTO processDefinition;

    public InstanciasContent(String id, boolean withSideBar, String processDefinitionCode) {
        super(id, false, withSideBar, false, true);
        processDefinition = uiAdminFacade.retrieveDefinitionByKey(processDefinitionCode);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        if(flowMetadataFacade.hasAccessToProcessDefinition(processDefinition.getSigla(), getUserId(), AccessLevel.LIST)){
            BaseDataProvider<IInstanceDTO, String> dataProvider = new BaseDataProvider<IInstanceDTO, String>() {
                @Override
                public Iterator<? extends IInstanceDTO> iterator(int first, int count,
                    String sortProperty, boolean ascending) {
                    return uiAdminFacade.retrieveAllInstance(first, count, sortProperty, ascending, processDefinition.getCod()).iterator();
                }
                
                @Override
                public long size() {
                    return uiAdminFacade.countAllInstance(processDefinition.getCod());
                }
            };
            
            queue(new BSDataTableBuilder<>(dataProvider)
                .appendPropertyColumn(getMessage("label.table.column.description"), "description", IInstanceDTO::getDescricao)
                .appendPropertyColumn(getMessage("label.table.column.time"), "delta", IInstanceDTO::getDeltaString)
                .appendPropertyColumn(getMessage("label.table.column.date"), "date", IInstanceDTO::getDataInicialString)
                .appendPropertyColumn(getMessage("label.table.column.delta"), "deltas", IInstanceDTO::getDeltaAtividadeString)
                .appendPropertyColumn(getMessage("label.table.column.dates"), "dates", IInstanceDTO::getDataAtividadeString)
                .appendPropertyColumn(getMessage("label.table.column.user"), "user", IInstanceDTO::getUsuarioAlocado)
                .build("processos"));
        } else {
            queue(new WebMarkupContainer("processos").setVisible(false));
            error(getString("error.user.without.access.to.process"));
        }
        
    }

    @Override
    protected WebMarkupContainer getBreadcrumbLinks(String id) {
        RepeatingView breadCrumb = new RepeatingView(id);
        
        PageParameters pageParameters = new PageParameters().set(Content.PROCESS_DEFINITION_COD_PARAM, processDefinition.getSigla());
        
        breadCrumb.add(createBreadCrumbLink(breadCrumb.newChildId(), 
            urlFor(DashboardPage.class, pageParameters).toString(),
            getString("breadcrumb.dashboard")));
        breadCrumb.add(createActiveBreadCrumbLink(breadCrumb.newChildId(), 
            urlFor(ProcessosPage.class, pageParameters).toString(),
            getString("breadcrumb.instances")));
        breadCrumb.add(createBreadCrumbLink(breadCrumb.newChildId(), 
            urlFor(MetadadosPage.class, pageParameters).toString(),
            getString("breadcrumb.metadata")));
        return breadCrumb;
    }
    
    @Override
    protected IModel<?> getContentTitlelModel() {
        return $m.ofValue(processDefinition.getNome());
    }

    @Override
    protected IModel<?> getContentSubtitlelModel() {
        return $m.ofValue();
    }
}