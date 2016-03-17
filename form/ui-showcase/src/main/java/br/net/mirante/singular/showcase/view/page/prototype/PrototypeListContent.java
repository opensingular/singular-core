/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.showcase.view.page.prototype;

import static br.net.mirante.singular.util.wicket.util.WicketUtils.$m;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import br.net.mirante.singular.form.mform.SDictionary;
import br.net.mirante.singular.form.mform.SIComposite;
import br.net.mirante.singular.form.mform.SInstance;
import br.net.mirante.singular.form.mform.SType;
import br.net.mirante.singular.form.mform.context.SFormConfig;
import br.net.mirante.singular.form.mform.document.RefType;
import br.net.mirante.singular.form.mform.io.MformPersistenciaXML;
import br.net.mirante.singular.form.util.xml.MElement;
import br.net.mirante.singular.form.util.xml.MParser;
import br.net.mirante.singular.form.wicket.component.BFModalBorder;
import br.net.mirante.singular.form.wicket.feedback.SFeedbackPanel;
import br.net.mirante.singular.form.wicket.model.MInstanceRootModel;
import br.net.mirante.singular.showcase.dao.form.Prototype;
import br.net.mirante.singular.showcase.dao.form.PrototypeDAO;
import br.net.mirante.singular.showcase.view.SingularWicketContainer;
import br.net.mirante.singular.showcase.view.template.Content;
import br.net.mirante.singular.util.wicket.datatable.BSDataTable;
import br.net.mirante.singular.util.wicket.datatable.BSDataTableBuilder;
import br.net.mirante.singular.util.wicket.datatable.BaseDataProvider;
import br.net.mirante.singular.util.wicket.datatable.column.BSActionColumn;
import br.net.mirante.singular.util.wicket.modal.BSModalBorder;
import br.net.mirante.singular.util.wicket.output.BOutputPanel;
import br.net.mirante.singular.util.wicket.resource.Icone;
import br.net.mirante.singular.util.wicket.tab.BSTabPanel;

public class PrototypeListContent extends Content
        implements SingularWicketContainer<PrototypeListContent, Void> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PrototypeListContent.class);
    private static final SDictionary dictionary = SDictionary.create();

    private BSDataTable<Prototype, String> listTable;
    private List<Prototype> dataList = new LinkedList<>();

    private final BFModalBorder deleteModal  = new BFModalBorder("deleteModal");
    private final BFModalBorder viewXmlModal = new BFModalBorder("viewXmlModal");

    @Inject @Named("formConfigWithDatabase")
    private SFormConfig<String> singularFormConfig;

    @Inject
    private PrototypeDAO prototypeDAO;

    private Prototype selectedPrototype;

    static {
        dictionary.loadPackage(SPackagePrototype.class);
    }

    public PrototypeListContent(String id) {
        super(id, false, true);
    }

    @Override
    protected void onInitialize() {

        super.onInitialize();

        add(new Form<>("delete-form").add(deleteModal));
        add(setUpInsertButton());
        add(listTable = setupDataTable());
        add(viewXmlModal);
        add(new SFeedbackPanel("feedback", this));

        deleteModal.setTitleText(Model.of(getString("label.delete.message")));

        deleteModal.addButton(BSModalBorder.ButtonStyle.PRIMARY, Model.of(getString("label.button.ok")),
                new AjaxButton("delete-btn") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        prototypeDAO.remove(selectedPrototype);
                        selectedPrototype = null;
                        updateListTableFromModal(target);
                        deleteModal.hide(target);
                    }
                });

        deleteModal.addButton(BSModalBorder.ButtonStyle.DEFAULT, Model.of(getString("label.button.cancel")),
                new AjaxButton("cancel-btn") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        deleteModal.hide(target);
                    }
                });

        deleteModal.setSize(BSModalBorder.Size.SMALL);

    }

    private void updateDataList() {
        dataList = prototypeDAO.listAll();
    }

    private MarkupContainer setUpInsertButton() {
        return new Form<>("form").add(new AjaxButton("insert") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                setResponsePage(PrototypePage.class);
            }

        });
    }

    private BSDataTable<Prototype, String> setupDataTable() {
        updateDataList();
        BSDataTableBuilder<Prototype, String, IColumn<Prototype, String>> builder = new BSDataTableBuilder<>(createDataProvider());
        builder
                .appendPropertyColumn(getMessage("label.table.column.name"),
                        "name", Prototype::getName)
                .appendColumn(buildActionColumn())
                .setRowsPerPage(Long.MAX_VALUE); //TODO: proper pagination
        return builder.build("data-list");
    }

    public BSActionColumn<Prototype, String> buildActionColumn() {
        return new BSActionColumn<Prototype, String>($m.ofValue(""))
                .appendAction(getMessage("label.table.column.edit"),
                    Icone.PENCIL_SQUARE,
                    (target, model) -> {
                        setResponsePage(PrototypePage.class,
                                new PageParameters()
                                        .add(PrototypePage.ID, model.getObject().getId()));
                })
                .appendAction(getMessage("label.table.column.visualizar"),
                    Icone.EYE,
                    (target, model) -> {
                        setResponsePage(new PreviewPage(getMInstance(model.getObject()), PrototypeListContent.this.getPage()));
                    })
                .appendAction(getMessage("label.table.column.delete"),
                    Icone.MINUS, this::deleteSelected)
                .appendAction(getMessage("label.table.column.visualizar.xml"),
                    Icone.EYE, this::viewXml);
    }

    private MInstanceRootModel<SIComposite> getMInstance(Prototype prototype) {
        String xml = prototype.getXml();
        RefType refType = new RefType() {
            protected SType<?> retrieve() {
                return dictionary.getType(SPackagePrototype.META_FORM_COMPLETE);
            }
        };
        SIComposite instance = MformPersistenciaXML.fromXML(refType, xml, singularFormConfig.getDocumentFactory());
        return new MInstanceRootModel<>(instance);
    }

    private BaseDataProvider<Prototype, String> createDataProvider() {
        return new BaseDataProvider<Prototype, String>() {

            @Override
            public long size() {
                return dataList.size();
            }

            @Override
            public Iterator<? extends Prototype> iterator(int first, int count,
                                                               String sortProperty, boolean ascending) {
                return dataList.iterator();
            }
        };
    }

    private void deleteSelected(AjaxRequestTarget target, IModel<Prototype> model) {
        selectedPrototype = model.getObject();
        deleteModal.show(target);
    }

    private void viewXml(AjaxRequestTarget target, IModel<Prototype> model) {

        final String xmlPersistencia = model.getObject().getXml();
        final String xmlTabulado = getXmlTabulado(xmlPersistencia);

        final BSTabPanel xmlTabs = new BSTabPanel("xmlTabs");
        xmlTabs.addTab(getString("label.xml.tabulado"), new BOutputPanel(BSTabPanel.getTabPanelId(), $m.ofValue(xmlTabulado)));
        xmlTabs.addTab(getString("label.xml.persistencia"), new BOutputPanel(BSTabPanel.getTabPanelId(), $m.ofValue(xmlPersistencia)));

        viewXmlModal.addOrReplace(xmlTabs);
        viewXmlModal.show(target);
        viewXmlModal.setSize(BFModalBorder.Size.LARGE);
    }

    private String getXmlTabulado(String xmlString) {
        if (StringUtils.isNotEmpty(xmlString)) {
            try {
                final MElement xml = MParser.parse(xmlString);
                final StringWriter sw = new StringWriter();
                final PrintWriter writer = new PrintWriter(sw);
                xml.printTabulado(writer);
                return sw.toString();
            } catch (SAXException | IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return StringUtils.EMPTY;
    }

    private void updateListTableFromModal(AjaxRequestTarget target) {
        updateDataList();
        target.add(listTable);
    }

    @Override
    protected WebMarkupContainer getBreadcrumbLinks(String id) {
        return new Fragment(id, "breadcrumbForm", this);
    }


    @Override
    protected IModel<?> getContentTitleModel() {
        return new ResourceModel("label.content.title");
    }

    @Override
    protected IModel<?> getContentSubtitleModel() {
        return $m.ofValue();
    }

}
