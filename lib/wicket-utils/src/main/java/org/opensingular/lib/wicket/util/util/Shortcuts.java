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

package org.opensingular.lib.wicket.util.util;

import org.opensingular.lib.wicket.util.lambda.ILambdasMixin;

public class Shortcuts {

    public static final IModelsMixin     $m = Impl.INSTANCE;
    public static final IBehaviorsMixin  $b = Impl.INSTANCE;
    public static final IValidatorsMixin $v = Impl.INSTANCE;
    public static final ILambdasMixin    $L = Impl.INSTANCE;

    private Shortcuts() {}

    // é um enum para evitar problemas com a serialização
    private enum Impl implements IModelsMixin, IBehaviorsMixin, IValidatorsMixin, ILambdasMixin {
        INSTANCE;
    }
}
