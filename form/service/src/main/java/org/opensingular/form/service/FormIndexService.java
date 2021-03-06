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

package org.opensingular.form.service;

import org.opensingular.form.SInfoType;
import org.opensingular.form.SInstance;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.form.io.SFormXMLUtil;
import org.opensingular.form.persistence.dao.FormCacheFieldDAO;
import org.opensingular.form.persistence.dao.FormCacheValueDAO;
import org.opensingular.form.persistence.dao.FormDAO;
import org.opensingular.form.persistence.dao.FormVersionDAO;
import org.opensingular.form.persistence.dto.InstanceFormDTO;
import org.opensingular.form.persistence.entity.FormEntity;
import org.opensingular.lib.commons.scan.SingularClassPathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Transactional
@Named
public class FormIndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormIndexService.class);

    @Inject
    private FormFieldService formFieldService;

    @Inject
    private FormDAO formDAO;

    @Inject
    private FormVersionDAO formVersionDAO;

    @Inject
    private FormCacheFieldDAO formFieldDAO;

    @Inject
    private FormCacheValueDAO formCacheValueDAO;

    /**
     * Recupera todos os formularios e a partir do seu tipo encontra a classe de seu SType no classpath.
     * Com a classe do SType carrega uma SInstance que é passada para o serviço FormFieldService para indexação
     */
    public void
    indexAllForms() {
        LOGGER.info("Iniciando a indexação total da base");

        long                  startNanos       = System.nanoTime();
        boolean               hasMoreItens     = true;
        List<InstanceFormDTO> instancesToIndex = new ArrayList<>();

        SingularClassPathScanner scanner = SingularClassPathScanner.get();
        Set<Class<?>>            classesFromClassloader = scanner.findClassesAnnotatedWith(SInfoType.class);
        classesFromClassloader.removeAll(classesFromClassloader.stream()
                .filter(c -> c.getName().contains("org.opensingular"))
                .collect(Collectors.toList()));

        while (hasMoreItens) {
            List<FormEntity> forms = formDAO.listUnIndexedForms();
            if (forms.isEmpty()) {
                hasMoreItens = false;
                continue;
            }

            for (FormEntity form : forms) {
                String formType = form.getFormType().getAbbreviation();
                String typeName = formType.substring(formType.lastIndexOf('.')+1, formType.length());
                Optional<Class<?>> clazz = classesFromClassloader.stream()
                        .filter(c -> c.getName().contains(typeName))
                        .findFirst();

                if (clazz.isPresent()) {
                    Class formClass = clazz.get();
                    RefType refType = RefType.of(formClass);
                    SDocumentFactory sDocumentFactory = SDocumentFactory.empty();
                    SInstance instance = SFormXMLUtil.fromXML(refType, form.getCurrentFormVersionEntity().getXml(), sDocumentFactory);
                    InstanceFormDTO dto = new InstanceFormDTO();
                    dto.setInstance(instance);
                    dto.setForm(form);
                    instancesToIndex.add(dto);
                } else {
                    LOGGER.info("Não foi possível indexar o form {}", formType);
                }
            }
            formFieldService.saveFields(instancesToIndex);
            instancesToIndex = new ArrayList<>();//NOSONAR
        }

        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        LOGGER.info("Indexação completa. Duração: {} millis", duration );
    }


    /**
     * Coloca todas as versões de formulario no estado de "Pendente de indexação" e apaga todos os dados já indexados
     */
    public void resetIndexedFlag() {
        formVersionDAO.resetIndexedFlag();
        formCacheValueDAO.deleteAllIndexedData();
        formFieldDAO.deleteAllIndexedFields();
    }
}



