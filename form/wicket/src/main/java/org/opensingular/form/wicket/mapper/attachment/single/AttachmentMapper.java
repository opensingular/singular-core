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

package org.opensingular.form.wicket.mapper.attachment.single;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.type.core.attachment.SIAttachment;
import org.opensingular.form.view.FileEventListener;
import org.opensingular.form.view.SViewAttachment;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.enums.ViewMode;
import org.opensingular.form.wicket.mapper.AbstractControlsFieldComponentMapper;
import org.opensingular.form.wicket.mapper.attachment.DownloadLink;
import org.opensingular.form.wicket.mapper.attachment.DownloadSupportedBehavior;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSWellBorder;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;

public class AttachmentMapper extends AbstractControlsFieldComponentMapper {

    @Override
    @SuppressWarnings("unchecked")
    public Component appendInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        final IModel<? extends SInstance> model = ctx.getModel();
        final FileUploadPanel container = new FileUploadPanel("container", (IModel<SIAttachment>) model, ViewMode.EDIT);
        registerListeners(ctx, container);
        formGroup.appendDiv(container);
        return container.getUploadField();
    }

    private void registerListeners(WicketBuildContext ctx, FileUploadPanel container) {
        if (ctx.getView() instanceof SViewAttachment) {
            SViewAttachment viewAttachment = (SViewAttachment) ctx.getView();
            for (FileEventListener uploadListener : viewAttachment.getFileUploadedListeners()) {
                container.registerFileUploadedListener(uploadListener);
            }
            for (FileEventListener removedListener : viewAttachment.getFileRemovedListeners()) {
                container.registerFileRemovedListener(removedListener);
            }
        }
    }

    @Override
    public String getReadOnlyFormattedText(WicketBuildContext ctx, IModel<? extends SInstance> model) {
        throw new SingularFormException("Este metodo não deve ser acessado, para download utilizar appendReadOnlyInput");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Component appendReadOnlyInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        final IModel<? extends SInstance> model = ctx.getModel();

        String markup = ""
            + "\n<div wicket:id='well'>"
            + "\n  <div stype='min-height: 20px; white-space: pre-wrap; word-wrap: break-word;'>"
            + "\n    <i class='fa fa-file'></i>"
            + "\n    <a wicket:id='downloadLink' target='_blank'></a>"
            + "\n  </div>"
            + "\n</div>"
            + "\n";

        final BSWellBorder well = BSWellBorder.small("well");
        final TemplatePanel panel = new TemplatePanel("_readOnlyAttachment", markup);
        final DownloadSupportedBehavior downloadSupportedBehavior = new DownloadSupportedBehavior(model);
        final DownloadLink downloadLink = new DownloadLink("downloadLink", (IModel<SIAttachment>) model, downloadSupportedBehavior);
        panel.add(downloadSupportedBehavior);
        well.add(downloadLink);
        panel.add(well);
        formGroup.appendTag("div", panel);
        return panel;
    }

}
