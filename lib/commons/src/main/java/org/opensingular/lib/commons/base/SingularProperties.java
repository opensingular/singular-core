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

package org.opensingular.lib.commons.base;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Carrega os arquivos de propriedades do singular e dá fácil acesso ao mesmos, mediante um singleton {@link
 * SingularProperties#get()}.
 *
 * <p>Doesn't allow to have the same property defined twice in the same scope, but allows them to be overridden
 * between scopes. A subsequent scope may override a property loaded by a previous scope in this order:</p>
 * <ol>
 *     <li>The first scope (singular internal) looks for all the singular-default.properties on the classpath.</li>
 *     <li>The second scope (application scope) looks for all the singular.properties on the classpath.</li>
 *     <li>The third scope (deploy scope) look for the 'singular.properties' on the configuration directory.</li>
 * </ol>
 *
 *
 *
 * @author Daniel C. Bordin
 * @author Vinicius Nunes
 */
public interface SingularProperties {

    String SYSTEM_PROPERTY_SINGULAR_SERVER_HOME = "singular.server.home";
    String HIBERNATE_GENERATOR = "flow.persistence.hibernate.generator";
    String HIBERNATE_SEQUENCE_PROPERTY_PATTERN = "flow.persistence.%s.sequence";
    String SINGULAR_EAGER_LOAD_FLOW_DEFINITIONS = "singular.flow.eager.load";
    String SINGULAR_SEND_EMAIL = "singular.send.email";
    String SINGULAR_EMAIL_TEST_RECPT = "singular.email.rcpt.test";

    String SINGULAR_DEV_MODE = "singular.development";

    // Limites globais são limites máximos, não configuráveis por arquivo.
    String FILEUPLOAD_GLOBAL_MAX_REQUEST_SIZE = "singular.fileupload.global_max_request_size";
    String FILEUPLOAD_GLOBAL_MAX_FILE_SIZE = "singular.fileupload.global_max_file_size";
    String FILEUPLOAD_GLOBAL_MAX_FILE_COUNT = "singular.fileupload.global_max_file_count";
    String FILEUPLOAD_GLOBAL_MAX_FILE_AGE = "singular.fileupload.global_max_file_age";

    // Limites default são limites configuráveis por arquivo. Não podem exceder os limites globais.
    String FILEUPLOAD_DEFAULT_MAX_REQUEST_SIZE = "singular.fileupload.default_max_request_size";
    String FILEUPLOAD_DEFAULT_MAX_FILE_SIZE = "singular.fileupload.default_max_file_size";

    // Identifica se o singular deve usar o banco em memória, ou se conectar a um banco externo.
    String USE_EMBEDDED_DATABASE = "singular.database.embedded";
    String JNDI_DATASOURCE = "singular.datasource.jndi.name";

    // Identifica o nome do schema que deve ser utilizado
    String CUSTOM_SCHEMA_NAME = "singular.custom.schema.name";

    String DEFAULT_CAS_ENABLED = "singular.cas.default.enabled";
    String DISABLE_AUTHORIZATION = "singular.auth.disable";
    String ANALYTICS_ENABLED = "singular.analytics.enabled";
    String FREEMARKER_IGNORE_ERROR = "singular.form.freemarker.ignore_error";

    String REST_ALLOWED_COMMON_NAME = "singular.rest.allowed.common.name";

    // Propriedades dos serviços soap
    String UPLOAD_DIR_WS = "singular.ws.upload";
    String TARGET_NAMESPACE = "singular.ws.targetnamespace";

    String RECREATE_DATABASE = "singular.database.recreate";

    //    ParmDef<String> ADDRESS = new ParmDef<>("Asasd.ad.asda " , String.class, "dadad")

    static SingularProperties get() {
        return SingularPropertiesImpl.get();
    }

    /**
     * Retorna o valor da propriedade solicitada. Retornar {@param defaultValue} se
     * a propriedade retornar null;
     */
    @Nonnull
    static String get(@Nonnull String key, @Nonnull String defaultValue) {
        return get().getProperty(key, defaultValue);
    }

    /**
     * Looks for the property with the giving key.
     * <p>Never return empty String (in this case they became null) and also trims the resulting String.</p>
     *
     * @throws SingularPropertyException If the search results in a null value.
     */
    @Nonnull
    static String get(@Nonnull String key) {
        return get().getProperty(key);
    }

    /**
     * Looks for the property with the giving key.
     * <p>Never return empty String (in this case they became null) and also trims the resulting String.</p>
     */
    @Nonnull
    static Optional<String> getOpt(@Nonnull String key) {
        return get().getPropertyOpt(key);
    }

    /**
     * Looks for the property with the giving key.
     * <p>Never return empty String (in this case they became null) and also trims the resulting String.</p>
     *
     * @throws SingularPropertyException If the search results in a null value.
     */
    @Nonnull
    String getProperty(@Nonnull String key);

    /**
     * Looks for the property with the giving key.
     * <p>Never return empty String (in this case they became null) and also trims the resulting String.</p>
     */
    @Nonnull
    Optional<String> getPropertyOpt(@Nonnull String key);

    /**
     * Retorna o valor da propriedade solicitada. Retornar {@param defaultValue} se
     * a propriedade retornar null;
     */
    @Nonnull
    default String getProperty(@Nonnull String key, @Nonnull String defaultValue) {
        Objects.requireNonNull(defaultValue);
        return getPropertyOpt(Objects.requireNonNull(key)).orElse(defaultValue);
    }


    boolean isTrue(String key);

    boolean isFalse(String key);

    default String getSingularServerHome() {
        return System.getProperty(SYSTEM_PROPERTY_SINGULAR_SERVER_HOME);
    }

    /** Prints the content of map of properties to the system output identifying the source of each property. */
    public void debugContent();

    /** Prints the content of map of properties to the specific output identifying the source of each property. */
    public void debugContent(@Nonnull Appendable out);
}