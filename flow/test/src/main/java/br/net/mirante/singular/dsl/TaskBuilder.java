package br.net.mirante.singular.dsl;

import br.net.mirante.singular.flow.core.builder.ITaskDefinition;

public class TaskBuilder {

    public TaskBuilder(Builder builder) {
        /* CONSTRUTOR VAZIO */
    }

    public JavaBuilder1 java(String key) {
        return new JavaBuilder1(this);
    }

    public <T extends Enum & ITaskDefinition> JavaBuilder1 java(T val) {
        return new JavaBuilder1(this);
    }

    public PeopleBuilder1 people(String aprovar) {
        return new PeopleBuilder1(this);
    }

    public TransitionBuilder1 transition() {
        return new TransitionBuilder1(this);
    }

    public TransitionBuilder1 transition(String aprovado) {
        return null;
    }
}
