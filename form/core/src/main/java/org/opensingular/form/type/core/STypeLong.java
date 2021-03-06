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

package org.opensingular.form.type.core;

import org.apache.commons.lang3.StringUtils;
import org.opensingular.form.SInfoType;
import org.opensingular.form.STypeSimple;

import java.math.BigInteger;

@SInfoType(name = "Long", spackage = SPackageCore.class)
public class STypeLong extends STypeSimple<SILong, Long> {

    public STypeLong() {
        super(SILong.class, Long.class);
    }

    protected STypeLong(Class<? extends SILong> instanceClass) {
        super(instanceClass, Long.class);
    }

    @Override
    protected Long convertNotNativeNotString(Object value) {
        if (value instanceof Number) {
            BigInteger bigIntegerValue = new BigInteger(String.valueOf(value));
            if (bigIntegerValue.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) > 0) {
                throw createConversionError(value, Long.class, " Valor muito grande.", null);
            }
            if (bigIntegerValue.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) < 0) {
                throw createConversionError(value, Long.class, " Valor muito pequeno.", null);
            }
            return bigIntegerValue.longValue();
        }
        throw createConversionError(value);
    }

    @Override
    public Long fromString(String value) {
        String v = StringUtils.trimToNull(value);
        if (v == null) {
            return null;
        }
        try {
            return Long.valueOf(v);
        } catch (Exception e) {
            throw createConversionError(value, Long.class, null, e);
        }
    }
}
