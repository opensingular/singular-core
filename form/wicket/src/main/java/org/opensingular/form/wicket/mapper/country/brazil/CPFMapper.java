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

package org.opensingular.form.wicket.mapper.country.brazil;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.mapper.StringMapper;

public class CPFMapper extends StringMapper {

    @Override
    public String getReadOnlyFormattedText(WicketBuildContext ctx, IModel<? extends SInstance> model) {
        return formatCPF(super.getReadOnlyFormattedText(ctx, model));
    }

    private static String formatCPF(String cpf) {
        if (cpf != null) {
            final String safeCpf = cpf.replaceAll("[^\\d]", "");
            if (safeCpf.length() == 11) {
                return String.format("%s.%s.%s-%s", safeCpf.substring(0, 3), safeCpf.substring(3, 6),
                        safeCpf.substring(6, 9), safeCpf.substring(9, 11));
            } else {
                return cpf;
            }
        } else {
            return StringUtils.EMPTY;
        }
    }

}