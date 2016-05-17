/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.studio.component.custom;

import java.util.Optional;

import br.net.mirante.singular.studio.component.CaseBase;
import br.net.mirante.singular.studio.component.ResourceRef;

public class CaseCustomStringMapper extends CaseBase {

    public CaseCustomStringMapper() {
        super("Custom Mapper", "Material Desing Input");
        final Optional<ResourceRef> customStringMapper = ResourceRef.forSource(MaterialDesignInputMapper.class);
        if(customStringMapper.isPresent()) {
            getAditionalSources().add(customStringMapper.get());
        }
    }
}
