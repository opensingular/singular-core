package br.net.mirante.singular.showcase.view.page.form.examples.canabidiol.model;

import java.util.List;

public class CategoriaCID extends AbstractDadoCID {

    private List<SubCategoriaCID> subCategorias;

    public List<SubCategoriaCID> getSubCategorias() {
        return subCategorias;
    }

    public void setSubCategorias(List<SubCategoriaCID> subCategorias) {
        this.subCategorias = subCategorias;
    }
}
