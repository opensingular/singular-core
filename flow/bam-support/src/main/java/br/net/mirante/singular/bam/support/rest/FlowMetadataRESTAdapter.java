package br.net.mirante.singular.bam.support.rest;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.net.mirante.singular.bam.support.persistence.dao.DefinitionDAO;
import br.net.mirante.singular.bam.support.persistence.dto.DefinitionDTO;
import br.net.mirante.singular.flow.core.Flow;
import br.net.mirante.singular.flow.core.MBPMUtil;
import br.net.mirante.singular.flow.core.ProcessDefinition;
import br.net.mirante.singular.flow.core.SingularFlowConfigurationBean;
import br.net.mirante.singular.flow.core.authorization.AccessLevel;
import br.net.mirante.singular.flow.core.renderer.FlowRendererFactory;
import br.net.mirante.singular.flow.core.service.IFlowMetadataREST;

@RestController
public abstract class FlowMetadataRESTAdapter implements IFlowMetadataREST {

    @Inject
    private DefinitionDAO definitionDAO;

    @Inject
    private SingularFlowConfigurationBean singularFlowConfigurationBean;

    @Transactional
    @RequestMapping(value = PATH_PROCESS_DEFINITION_WITH_ACCESS, method = RequestMethod.GET)
    @Override
    public Set<String> listProcessDefinitionsWithAccess(String groupToken, String userCod, AccessLevel accessLevel) {
        return definitionDAO.retrieveAll().stream().map(DefinitionDTO::getSigla).collect(Collectors.toSet());
    }

    @RequestMapping(value = PATH_PROCESS_DEFINITION_HAS_ACCESS, method = RequestMethod.GET)
    @Override
    public boolean hasAccessToProcessDefinition(@RequestParam String groupToken, @RequestParam String processDefinitionKey, @RequestParam String userCod, @RequestParam AccessLevel accessLevel) {
        return true;
    }

    @RequestMapping(value = PATH_PROCESS_INSTANCE_HAS_ACCESS, method = RequestMethod.GET)
    @Override
    public boolean hasAccessToProcessInstance(@RequestParam String groupToken, @RequestParam String processInstanceFullId, @RequestParam String userCod, @RequestParam AccessLevel accessLevel) {
        return true;
    }

    @RequestMapping(value = PATH_PROCESS_DEFINITION_DIAGRAM, method = RequestMethod.GET)
    @Override
    public byte[] processDefinitionDiagram(@RequestParam String groupToken, @RequestParam String processDefinitionKey) {
        ProcessDefinition<?> definicao = Flow.getProcessDefinitionWith(processDefinitionKey);
        if (definicao != null) {
            if (definicao instanceof ProcessDefinition) {
                return FlowRendererFactory.generateImageFor(definicao);
            } else {
                return MBPMUtil.getFlowImage(definicao);
            }
        }
        return null;
    }
}