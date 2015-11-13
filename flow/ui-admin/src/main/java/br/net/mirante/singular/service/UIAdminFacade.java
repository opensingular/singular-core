package br.net.mirante.singular.service;

import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import br.net.mirante.singular.dto.DefinitionDTO;
import br.net.mirante.singular.dto.FeedDTO;
import br.net.mirante.singular.dto.InstanceDTO;
import br.net.mirante.singular.dto.MenuItemDTO;
import br.net.mirante.singular.dto.MetaDataDTO;
import br.net.mirante.singular.dto.StatusDTO;
import br.net.mirante.singular.flow.core.service.IUIAdminService;

@Service("uiAdminFacade")
public class UIAdminFacade implements IUIAdminService<DefinitionDTO, InstanceDTO, MetaDataDTO, StatusDTO,
        FeedDTO, MenuItemDTO> {

    @Inject
    private ProcessDefinitionService processDefinitionService;

    @Inject
    private PesquisaService pesquisaService;

    @Inject
    private FeedService feedService;

    @Inject
    private MenuService menuService;

    @Override
    public DefinitionDTO retrieveDefinitionById(Long id) {
        return processDefinitionService.retrieveById(id);
    }

    @Override
    public List<DefinitionDTO> retrieveAllDefinition(int first, int size, String orderByProperty, boolean asc) {
        return processDefinitionService.retrieveAll(first, size, orderByProperty, asc);
    }

    @Override
    public int countAllDefinition() {
        return processDefinitionService.countAll();
    }

    @Override
    public List<InstanceDTO> retrieveAllInstance(int first, int size, String orderByProperty, boolean asc, Long id) {
        return processDefinitionService.retrieveAll(first, size, orderByProperty, asc, id);
    }

    @Override
    public int countAllInstance(Long id) {
        return processDefinitionService.countAll(id);
    }

    @Override
    public byte[] retrieveProcessDiagram(String sigla) {
        return processDefinitionService.retrieveProcessDiagram(sigla);
    }

    @Override
    public List<MetaDataDTO> retrieveMetaData(Long id) {
        return processDefinitionService.retrieveMetaData(id);
    }

    @Override
    public List<Map<String, String>> retrieveMeanTimeByProcess(Period period, String processCode, Set<String> processCodeWithAccess) {
        return pesquisaService.retrieveMeanTimeByProcess(period, processCode, processCodeWithAccess);
    }

    @Override
    public List<Map<String, String>> retrieveNewInstancesQuantityLastYear(String processCode, Set<String> processCodeWithAccess) {
        return pesquisaService.retrieveNewInstancesQuantityLastYear(processCode, processCodeWithAccess);
    }

    @Override
    public List<Map<String, String>> retrieveEndStatusQuantityByPeriod(Period period, String processCode) {
        return pesquisaService.retrieveEndStatusQuantityByPeriod(period, processCode);
    }

    @Override
    public List<Map<String, String>> retrieveMeanTimeByTask(Period period, String processCode) {
        return pesquisaService.retrieveMeanTimeByTask(period, processCode);
    }

    @Override
    public List<Map<String, String>> retrieveStatsByActiveTask(String processDefinitionCode) {
        return pesquisaService.retrieveStatsByActiveTask(processDefinitionCode);
    }

    @Override
    public StatusDTO retrieveActiveInstanceStatus(String processCode, Set<String> processCodeWithAccess) {
        return pesquisaService.retrieveActiveInstanceStatus(processCode, processCodeWithAccess);
    }

    @Override
    public List<Map<String, String>> retrieveMeanTimeActiveInstances(String processCode, Set<String> processCodeWithAccess) {
        return pesquisaService.retrieveMeanTimeActiveInstances(processCode, processCodeWithAccess);
    }

    @Override
    public List<Map<String, String>> retrieveAverageTimesActiveInstances(String processCode, Set<String> processCodeWithAccess) {
        return pesquisaService.retrieveAverageTimesActiveInstances(processCode, processCodeWithAccess);
    }

    @Override
    public List<Map<String, String>> retrieveMeanTimeFinishedInstances(String processCode, Set<String> processCodeWithAccess) {
        return pesquisaService.retrieveMeanTimeFinishedInstances(processCode, processCodeWithAccess);
    }

    @Override
    public List<Map<String, String>> retrieveCounterActiveInstances(String processCode, Set<String> processCodeWithAccess) {
        return pesquisaService.retrieveCounterActiveInstances(processCode, processCodeWithAccess);
    }

    @Override
    public String retrieveProcessDefinitionName(String processCode) {
        return pesquisaService.retrieveProcessDefinitionName(processCode);
    }

    @Override
    public String retrieveProcessDefinitionId(String processDefinitionCode) {
        return pesquisaService.retrieveProcessDefinitionId(processDefinitionCode);
    }

    @Override
    public List<FeedDTO> retrieveAllFeed(String processCode, Set<String> processCodeWithAccess) {
        return feedService.retrieveFeed(processCode, processCodeWithAccess);
    }

    @Override
    public List<MenuItemDTO> retrieveAllCategories() {
        return menuService.retrieveAllCategories();
    }

    @Override
    public List<MenuItemDTO> retrieveAllCategoriesWithAcces(String userId) {
        return menuService.retrieveAllCategoriesWithAcces(userId);
    }
    
    @Override
    public Pair<Long, Long> retrieveCategoryDefinitionIdsByCode(String code) {
        return menuService.retrieveCategoryDefinitionIdsByCode(code);
    }
}
