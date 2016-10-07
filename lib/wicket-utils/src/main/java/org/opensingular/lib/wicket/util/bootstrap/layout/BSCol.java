/*
 * Copyright (c) 2016, Singular and/or its affiliates. All rights reserved.
 * Singular PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.lib.wicket.util.bootstrap.layout;

import org.apache.wicket.behavior.Behavior;

public class BSCol extends BSContainer<BSCol> implements IBSGridCol<BSCol> {

    public BSCol(String id) {
        super(id);
        add(newBSGridColBehavior());
    }

    @Override
    public BSCol add(Behavior... behaviors) {
        return (BSCol) super.add(behaviors);
    }
}