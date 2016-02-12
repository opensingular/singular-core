package br.net.mirante.singular.showcase.component.custom.comment;

import br.net.mirante.singular.showcase.component.CaseBase;
import br.net.mirante.singular.showcase.component.ResourceRef;
import br.net.mirante.singular.showcase.component.custom.MaterialDesignInputMapper;

import java.io.Serializable;
import java.util.Optional;

public class CaseAnnotation extends CaseBase implements Serializable {

    public CaseAnnotation() {
        super("Annotation");
        setDescriptionHtml("Anotações e comentários associados a elementos de um form");
        final Optional<ResourceRef> pageWithAnnotation = ResourceRef.forSource(
                PageWithAnnotation.class);
        if(pageWithAnnotation.isPresent()) {
            getAditionalSources().add(pageWithAnnotation.get());
        }
    }

    @Override
    public boolean annotationEnabled() {    return true;    }
}