package br.net.mirante.singular.flow.core;

import br.net.mirante.singular.flow.core.view.WebRef;

@FunctionalInterface
public interface ITaskPageStrategy {

    public WebRef getPageFor(TaskInstance taskInstance, MUser user);

}