/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.opensingular.form.flatview;

import org.opensingular.form.STypeComposite;
import org.opensingular.form.STypeList;
import org.opensingular.form.STypeSimple;
import org.opensingular.form.aspect.AspectRef;
import org.opensingular.form.aspect.SingleAspectRegistry;
import org.opensingular.form.type.core.attachment.STypeAttachment;
import org.opensingular.form.type.country.brazil.STypeUF;

import javax.annotation.Nonnull;

/**
 * Register of the default implementations of the aspect {@link FlatViewGenerator#ASPECT_FLAT_VIEW_GENERATOR}.
 *
 * @author Daniel C. Bordin on 12/08/2017.
 */
public class FlatViewGeneratorRegistry extends SingleAspectRegistry<FlatViewGenerator, Object> {

    public FlatViewGeneratorRegistry(@Nonnull AspectRef<FlatViewGenerator> aspectRef) {
        super(aspectRef);
        add(STypeSimple.class, SISimpleFlatViewGenerator::new);
        add(STypeList.class, SIListFlatViewGenerator::new);
        add(STypeAttachment.class, SIAttachmentFlatViewGenerator::new);
        add(STypeUF.class, UFFlatViewGenerator::new);
        add(STypeComposite.class, SICompositeFlatViewGenerator::new);
    }
}
