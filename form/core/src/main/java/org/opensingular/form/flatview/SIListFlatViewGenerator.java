package org.opensingular.form.flatview;

import org.opensingular.form.SIList;
import org.opensingular.lib.commons.canvas.DocumentCanvas;

public class SIListFlatViewGenerator extends AbstractFlatViewGenerator {
    @Override
    protected void doWriteOnCanvas(DocumentCanvas canvas, FlatViewContext context) {
        SIList<?> instance = context.getInstanceAs(SIList.class);
        canvas.addTitle(context.getLabelOrName());
        DocumentCanvas subcanvas = canvas.newChild();
        instance.forEach(child -> {
            child.getAspect(ASPECT_FLAT_VIEW_GENERATOR)
                    .ifPresent(viewGenerator -> viewGenerator.writeOnCanvas(subcanvas, new FlatViewContext(child)));
            subcanvas.breakLine();
        });
    }
}