
package br.net.mirante.singular.ws.client;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "SingularWSService", targetNamespace = "http://ws.core.flow.singular.mirante.net.br/", wsdlLocation = "http://localhost:8080/sgas/singularWS?wsdl")
public class SingularWSService
    extends Service
{

    private final static URL SINGULARWSSERVICE_WSDL_LOCATION;
    private final static QName SINGULARWSSERVICE_QNAME = new QName("http://ws.core.flow.singular.mirante.net.br/", "SingularWSService");
    private final static Logger LOGGER = Logger.getLogger(SingularWSService.class.getName());

    static {
        URL url = null;
        try {
            url = SingularWSService.class.getResource("singularWS.wsdl");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        SINGULARWSSERVICE_WSDL_LOCATION = url;
    }

    public SingularWSService() {
        super(SINGULARWSSERVICE_WSDL_LOCATION, SINGULARWSSERVICE_QNAME);
    }

    public SingularWSService(WebServiceFeature... features) {
        super(SINGULARWSSERVICE_WSDL_LOCATION, SINGULARWSSERVICE_QNAME, features);
    }

    public SingularWSService(URL wsdlLocation) {
        super(wsdlLocation, SINGULARWSSERVICE_QNAME);
    }

    public SingularWSService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, SINGULARWSSERVICE_QNAME, features);
    }

    public SingularWSService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SingularWSService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns SingularWS
     */
    @WebEndpoint(name = "SingularWSPort")
    public SingularWS getSingularWSPort() {
        return super.getPort(new QName("http://ws.core.flow.singular.mirante.net.br/", "SingularWSPort"), SingularWS.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns SingularWS
     */
    @WebEndpoint(name = "SingularWSPort")
    public SingularWS getSingularWSPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://ws.core.flow.singular.mirante.net.br/", "SingularWSPort"), SingularWS.class, features);
    }
}