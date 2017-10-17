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

package org.opensingular.form.io;


import org.opensingular.form.document.RefSDocumentFactory;
import org.opensingular.form.document.RefType;
import org.opensingular.form.validation.ValidationError;
import org.opensingular.internal.lib.commons.xml.MElement;
import org.opensingular.lib.commons.context.ServiceRegistry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Objeto transitório para guardar uma versão serializável de MInstance ou
 * MDocument.
 *
 * @author Daniel C. Bordin
 */
public final class FormSerialized implements Serializable {

    private final RefSDocumentFactory         sDocumentFactoryRef;
    private final RefType  refRootType;
    private final String   rootTypeName;
    private final MElement xml, annotations;
    private String                            focusFieldPath;
    private Map<String, ServiceRegistry.ServiceEntry> services;
    private List<ValidationError>                    validationErrors;

    public FormSerialized(RefType refRootType, String rootTypeName, MElement xml, MElement annotations,
                          RefSDocumentFactory sDocumentFactoryRef) {
        this.refRootType = refRootType;
        this.rootTypeName = rootTypeName;
        this.sDocumentFactoryRef = sDocumentFactoryRef;
        this.xml = xml;
        this.annotations = annotations;
    }

    public String getRootTypeName() {
        return rootTypeName;
    }

    public RefType getRefRootType() {
        return refRootType;
    }

    public String getFocusFieldPath() {
        return focusFieldPath;
    }

    public MElement getAnnotations() {
        return annotations;
    }

    public MElement getXml() {
        return xml;
    }

    public void setFocusFieldPath(String focusFieldPath) {
        this.focusFieldPath = focusFieldPath;
    }

    public Map<String, ServiceRegistry.ServiceEntry> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceRegistry.ServiceEntry> services) {
        this.services = services;
    }

    public RefSDocumentFactory getSDocumentFactoryRef() {
        return sDocumentFactoryRef;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Collection<ValidationError> validationErrors) {
        this.validationErrors = (validationErrors == null) ? null : new ArrayList<>(validationErrors);
    }
}
