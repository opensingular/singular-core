package org.opensingular.singular.showcase.component.studio;

import org.opensingular.singular.showcase.component.CaseItem;
import org.opensingular.singular.showcase.component.Group;
import org.opensingular.singular.studio.core.CollectionDefinition;
import org.opensingular.singular.studio.core.CollectionEditorConfigBuilder;
import org.opensingular.singular.studio.core.CollectionInfoBuilder;

@CaseItem(componentName = "Order", group = Group.STUDIO_SAMPLES)
public class OrderCollectionDefinition implements CollectionDefinition<STypeOrder> {


    /*
     * Configurações que serão utilizadas para montar a casca do sinuglar studio
     * tais como configurações de menu e de permissoes globais do studio-form
     * @param builder
     */
    @Override
    public void collectionInfo(CollectionInfoBuilder<STypeOrder> builder) {
        builder
                .form(STypeOrder.class)
                .title("Compras");
    }

    /*
     * configuracao do renderizador do studio-form:
      * Listagem: listagem, filtros, paginacao, exclusao, acoes
      * Form: editar, novo, visualizar
     *
     */
    @Override
    public void configEditor(CollectionEditorConfigBuilder cfg, STypeOrder type) {
        cfg
                .list()
                .column(type.id)
                .column(type.descricao)
                .rowsPerPage(10)
                .disableQuickFilter()
                .sortBy(type.id)
                .delete("Tem certeza que deseja excluir a Compra número ${0}", type.id)
                        //Expadir para o delete, insert, view e edit:
                        // perfil que pode realizar cada operação
                        // gancho para regra de negócio anterior a deleção: sendo possível inclusive não fazer a deleção e apresentar msg
                        // gancho para regra de negócio após a deleção: sendo possível inclusive não fazer a deleção e apresentar msg
                .disableDelete()
                .disableInsert()
                .disableView()
                .disableEdit()
                .form()
                        //desabilita validacao
                .validateBeforeSave(false)
                        //configura botao de salvar
                .disableSave()
                        //configura botão de validar
                .disableValidate()
                        //configura botão de fechar
                .disableClose();
    }


}