package br.net.mirante.singular.server.p.commons.config;

import br.net.mirante.singular.server.commons.config.ConfigProperties;
import br.net.mirante.singular.server.commons.config.IServerContext;
import br.net.mirante.singular.server.commons.exception.SingularServerException;
import org.apache.wicket.request.Request;

import javax.servlet.http.HttpServletRequest;

/**
 * Utilitário para prover a configuração de contexto atual e os métodos utilitários
 * relacionados.
 */
public enum PServerContext implements IServerContext {

    PETITION("/peticionamento/*", br.net.mirante.singular.server.commons.config.ConfigProperties.PETICIONAMENTO_CONTEXT_KEY),
    ANALYSIS("/worklist/*", br.net.mirante.singular.server.commons.config.ConfigProperties.ANALISE_CONTEXT_KEY);

    private final String contextPath;

    PServerContext(String deaultPath, String key) {
        String path = ConfigProperties.get(key);
        if (path == null || path.length() <= 0) {
            this.contextPath = deaultPath;
        } else {
            this.contextPath = path;
        }
    }

    public static PServerContext getContextFromRequest(Request request) {
        return getContextFromRequest((HttpServletRequest) request.getContainerRequest());
    }

    public static PServerContext getContextFromRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String context = request.getPathInfo().replaceFirst(contextPath, "");
        for (PServerContext ctx : PServerContext.values()){
            if (context.startsWith(ctx.getUrlPath())) {
                return ctx;
            }
        }
        throw new SingularServerException("Não foi possível determinar o contexto do servidor do singular: peticionamento ou analise");
    }

    /**
     * O contexto no formato aceito por servlets e filtros
     *
     * @return
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Conversao do formato aceito por servlets e filtros (contextPath) para java regex
     *
     * @return
     */
    public String getPathRegex() {
        return getContextPath().replaceAll("\\*", ".*");
    }

    /**
     * Conversao do formato aceito por servlets e filtros (contextPath) para um formato de url
     * sem a / ao final.
     *
     * @return
     */
    public String getUrlPath() {
        String path = getContextPath().replace("*", "").replace(".", "").trim();
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

}