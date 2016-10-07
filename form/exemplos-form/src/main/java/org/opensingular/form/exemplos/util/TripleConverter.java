package org.opensingular.form.exemplos.util;

import org.opensingular.form.SIComposite;
import org.opensingular.form.SType;
import org.opensingular.form.converter.SInstanceConverter;
import org.opensingular.form.util.transformer.Value;
import org.apache.commons.lang3.tuple.Triple;


public class TripleConverter implements SInstanceConverter<Triple, SIComposite> {

    private final String left;
    private final String middle;
    private final String right;

    public TripleConverter(SType left, SType middle, SType right) {
        this.left = left.getNameSimple();
        this.middle = middle.getNameSimple();
        this.right = right.getNameSimple();
    }

    @Override
    public void fillInstance(SIComposite ins, Triple obj) {
        ins.setValue(left, obj.getLeft());
        ins.setValue(middle, obj.getMiddle());
        ins.setValue(right, obj.getRight());
    }

    @Override
    public Triple toObject(SIComposite ins) {
        return Triple.of(
                Value.of(ins, left),
                Value.of(ins, middle),
                Value.of(ins, right)
        );
    }

}