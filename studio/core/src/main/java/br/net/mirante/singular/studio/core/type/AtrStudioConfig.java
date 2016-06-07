package br.net.mirante.singular.studio.core.type;

import br.net.mirante.singular.form.SAttributeEnabled;
import br.net.mirante.singular.form.STranslatorForAttribute;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * Created by Daniel on 19/05/2016.
 */
public class AtrStudioConfig extends STranslatorForAttribute {


        public AtrStudioConfig() {
        }

        public AtrStudioConfig(SAttributeEnabled alvo) {
            super(alvo);
        }

        public AtrStudioConfig defaultSearchCriteria(Boolean value) {
            setAttributeValue(SPackageCollectionEditorConfig.ATR_DEFAULT_SEARCH_CRITERIA, value);
            return this;
        }
}