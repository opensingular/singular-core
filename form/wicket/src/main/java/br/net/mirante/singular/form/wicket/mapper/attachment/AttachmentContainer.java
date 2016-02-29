package br.net.mirante.singular.form.wicket.mapper.attachment;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import br.net.mirante.singular.form.mform.SInstance;
import br.net.mirante.singular.form.mform.core.attachment.IAttachmentPersistenceHandler;
import br.net.mirante.singular.form.mform.core.attachment.SIAttachment;
import br.net.mirante.singular.form.mform.document.SDocument;
import br.net.mirante.singular.form.wicket.model.IMInstanciaAwareModel;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSContainer;
import br.net.mirante.singular.util.wicket.bootstrap.layout.TemplatePanel;

/**
 * AttachmentContainer is the class responsible for rendering a upload field
 * using the jquery-file-upload javascript plugin. Even though it creates a file
 * input it is not used by the singular-form to submit the file information to
 * the {@link SInstance} representing it. Instead, it populates the instance
 * with a composite type containing the file descriptor. The workings of this
 * component is as follows:
 *
 * 1 - Whem a file is uploaded it uses the UploadBehaviour to call the
 * {@link IAttachmentPersistenceHandler} registered in the
 * {@link SDocument#getAttachmentPersistenceTemporaryHandler()}. It has a
 * default handler, but you can personalize as desired by using the
 * {@link SDocument#setAttachmentPersistenceTemporaryHandler(br.net.mirante.singular.form.mform.ServiceRef)}
 * register method. 2 - The information returne by the persistence handler is
 * stored in the file field as its descriptor. Using the handler is possible to
 * retrieve the proper information about the file.
 *
 * Since only the descriptor is stored in the Instance, it's advised to use
 * different handlers for the upload (default) and submission (persistent) of
 * the form.
 *
 * OBS: Remember that each {@link SInstance} has its own {@link SDocument}
 * making each handler configuration unique for its own instance.
 *
 * @author Fabricio Buzeto
 *
 */
@SuppressWarnings({"serial", "rawtypes"})
class AttachmentContainer extends BSContainer {
    public static String PARAM_NAME = "FILE-UPLOAD";
    private UploadBehavior uploader;
    private DownloadBehaviour downloader;
    private FormComponent fileField, nameField, hashField, sizeField, idField;

    public AttachmentContainer(IModel<? extends SIAttachment> model) {
        super("_attachment_" + model.getObject().getNome());
        setupFields(model);
        this.add(this.uploader = new UploadBehavior(model.getObject()));
        this.add(this.downloader = new DownloadBehaviour(model.getObject()));
        setup(field(), model);
    }

    @SuppressWarnings("unchecked")
    protected FormComponent setupFields(IModel<? extends SInstance> model) {
        String name = model.getObject().getNome();
        fileField = new FileUploadField(name, new IMInstanciaAwareModel() {
            @Override
            public Object getObject() {return null;}

            @Override
            public void setObject(Object object) {}

            @Override
            public void detach() {}

            @Override
            public SInstance getMInstancia() {
                return model.getObject();
            }
        });
        nameField = new HiddenField("file_name_"+name,
                        new PropertyModel<>(model, "fileName"));
        hashField = new HiddenField("file_hash_"+name,
                            new PropertyModel<>(model, "fileHashSHA1"));
        sizeField = new HiddenField("file_size_"+name,
                            new PropertyModel<>(model, "fileSize"));
        idField = new HiddenField("file_id_"+name,
                            new PropertyModel<>(model, "fileId"));
        return field();
    }

    protected FormComponent field(){
        return fileField;
    }

    public void setup(FormComponent field, IModel<? extends SInstance> model) {
        String fieldId = field.getMarkupId();

        appendTag("span", true, "class='btn btn-success fileinput-button'",
                appendInputButton(field));
        appendTag("div", true, "class='progress' id='progress_" + fieldId + "'",
                createProgressBar(field));

        appendTag("div", true, "class='files' id='files_" + fieldId + "'",
                createDownloadLink(model));

        appendTag("input", true, "type='hidden' id='" + nameField.getMarkupId() + "'",
                nameField);
        appendTag("input", true, "type='hidden' id='" + hashField.getMarkupId() + "'",
                hashField);
        appendTag("input", true, "type='hidden' id='" + idField.getMarkupId() + "'",
                idField);
        appendTag("input", true, "type='hidden' id='" + sizeField.getMarkupId() + "'",
                sizeField);
    }

    private BSContainer appendInputButton(FormComponent field) {
        BSContainer buttonContainer = new BSContainer<>("_bt_" + field.getId())
                .appendTag("span", new Label("_", Model.of("Selecionar ...")))
                .appendTag("input", true,
                "type='file' id='" + fileField.getMarkupId() + "'",fileField);

        appendScriptContainer(field.getMarkupId(), buttonContainer);
        return buttonContainer;
    }

    @SuppressWarnings({"unchecked"})
    private void appendScriptContainer(String fieldId, BSContainer buttonContainer) {
        TemplatePanel scriptContainer = (TemplatePanel)
                buttonContainer.newComponent(id -> new TemplatePanel(id,
                    () -> "<script > "
                            + "$(function () {"
                            + "  $('#" + fileField.getMarkupId()
                            + "').fileupload({  "
                            + "    url: '"+uploader.getUrl()+"',  "
                            + "    paramName: '"+PARAM_NAME+"',  "
                            + "    singleFileUploads: true,  "
                            + "    dataType: 'json',  "
                            + "    start: function (e, data) {  "
                            + "        console.log($('#files_"+ fieldId+"'));"
                            + "        $('#files_"+ fieldId+"').html('');"
                            + "        $('#progress_"+ fieldId+" .progress-bar').css('width','0%')"
                            + "    },"
                            + "    done: function (e, data) {  "
//                            + "        console.log(e,data);    "
                            + "        $.each(data.result.files, function (index, file) {  "
                            + "            $('#files_"+ fieldId+"').append("
                            + "                 $('<p/>').append("
                            + "                         $('<a />')"
                            + "                             .attr('href','"+downloader.getUrl()+"&fileId='+file.fileId+'&fileName='+file.name)"
                            + "                             .text(file.name)"
                            +"                  )"
                            +"             ); "
                            + "            $('#" + nameField.getMarkupId()+ "').val(file.name);"
                            + "            $('#" + idField.getMarkupId()+ "').val(file.fileId);"
                            + "            $('#" + hashField.getMarkupId()+ "').val(file.hashSHA1);"
                            + "            $('#" + sizeField.getMarkupId()+ "').val(file.size);"
                            + "        });  "
                            + "    },  "
                            + "    progressall: function (e, data) {  "
                            + "        var progress = parseInt(data.loaded / data.total * 100, 10); "
                            + "        $('#progress_"+ fieldId+" .progress-bar').css( 'width', "
                            + "                        progress + '%' ); "
                            + "    }  "
                            + "  }).prop('disabled', !$.support.fileInput)  "
                            + "    .parent().addClass($.support.fileInput ? undefined : 'disabled');  "
                            + "});"
                            + " </script>\n"));
        scriptContainer.setRenderBodyOnly(true);
    }

    private BSContainer createProgressBar(FormComponent field) {
        BSContainer progressContainer = new BSContainer<>("_progress_" + field.getId());
        progressContainer.appendTag("div", true, "class='progress-bar progress-bar-success'", emptyLabel());
        return progressContainer;
    }

    private final class DownloadLink extends Link<Object> {
        private DownloadLink(String id, IModel<Object> model) {
            super(id, model);
            setBody(model);
        }

        @Override
        protected CharSequence getURL() {
            return downloader.getUrl();
        }

        @Override
        public void onClick() {}
    }

    @SuppressWarnings("unchecked")
    private WebMarkupContainer createDownloadLink(IModel<? extends SInstance> model) {
        Link<Object> link = new DownloadLink("_", new PropertyModel(model, "fileName"));

        BSContainer wrapper = new BSContainer<>("_");
        wrapper.appendTag("a", true, "", link);
        return wrapper;
    }

    private Label emptyLabel() {
        return new Label("_", Model.of(""));
    }
}