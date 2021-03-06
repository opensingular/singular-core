/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.form.processor;

import org.junit.Test;
import org.opensingular.form.processor.TypeProcessorAttributeReadFromFile.AttributeEntry;
import org.opensingular.internal.lib.commons.xml.MElement;
import org.opensingular.internal.lib.commons.xml.MParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CoreAttributesXmlBasicTest {


    @Test
    public void basicLoadFromStringTest() throws Exception {
        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                      + "<attrs>"
                      + "<attr name=\"singular.form.basic.label\">Nome</attr>"
                      + "<attr name=\"singular.form.basic.subtitle\">Nome Subtitle</attr>"
                      + "<attr field=\"field1\" name=\"singular.form.basic.label\">SubNome1</attr>"
                      + "</attrs>";
        MElement xml = MParser.parse(xmlStr);
        NodeList root = xml.getMDocument().getElementsByTagName("attrs");
        NodeList elements = root.item(0).getChildNodes();

       
        Node n0 = elements.item(0);
        Node n1 = elements.item(1);
        Node n2 = elements.item(2);
        
        assertNull(n0.getAttributes().getNamedItem("field"));
        assertEquals("singular.form.basic.label", n0.getAttributes().getNamedItem("name").getNodeValue());
        assertEquals("Nome", n0.getTextContent());
        
        assertNull(n1.getAttributes().getNamedItem("field"));
        assertEquals("singular.form.basic.subtitle", n1.getAttributes().getNamedItem("name").getNodeValue());
        assertEquals("Nome Subtitle", n1.getTextContent());
        
        assertEquals("field1", n2.getAttributes().getNamedItem("field").getTextContent());
        assertEquals("singular.form.basic.label", n2.getAttributes().getNamedItem("name").getNodeValue());
        assertEquals("SubNome1", n2.getTextContent());
        
    }

    @Test
    public void basicLoadFromFileTest() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("basic.xml");
        
        //String xmlStr = IOUtils.toString(in, "UTF-8");
        MElement xml = MParser.parse(in);

        NodeList attrs = xml.getMDocument().getElementsByTagName("attrs");
        NodeList elements = attrs.item(0).getChildNodes();

        List<Node> n = new ArrayList<>();
        
        for (int i = 0; i < elements.getLength(); i++) {
            //Quando iterar sobre itens do XML, verificar de é do tipo Node.ELEMENT_NODE, pois aparecem nodes com textos em branco.
            if(elements.item(i).getNodeType() == Node.ELEMENT_NODE){
              n.add(elements.item(i));  
            }
        }

        assertNull(n.get(0).getAttributes().getNamedItem("field"));
        assertEquals("singular.form.basic.label", n.get(0).getAttributes().getNamedItem("name").getNodeValue());
        assertEquals("Nome", n.get(0).getTextContent());
        
        assertNull(n.get(1).getAttributes().getNamedItem("field"));
        assertEquals("singular.form.basic.subtitle", n.get(1).getAttributes().getNamedItem("name").getNodeValue());
        assertEquals("Nome Subtitle", n.get(1).getTextContent());
        
        assertEquals("field1", n.get(2).getAttributes().getNamedItem("field").getTextContent());
        assertEquals("singular.form.basic.label", n.get(2).getAttributes().getNamedItem("name").getNodeValue());
        assertEquals("SubNome1", n.get(2).getTextContent());
    }

    
    
    @Test
    public void basicReadDefinitions() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("basic.xml");
        MElement xml = MParser.parse(in);
        List<AttributeEntry> definitions = TypeProcessorAttributeReadFromFile.readDefinitionsFor(xml);
        
        assertEquals("singular.form.basic.label", definitions.get(0).attributeName);
        assertEquals("Nome", definitions.get(0).attributeValue);
        assertNull(definitions.get(0).subFieldPath);
        
        assertEquals("singular.form.basic.subtitle", definitions.get(1).attributeName);
        assertEquals("Nome Subtitle", definitions.get(1).attributeValue);
        assertNull(definitions.get(1).subFieldPath);

        assertEquals("singular.form.basic.label", definitions.get(2).attributeName);
        assertEquals("SubNome1", definitions.get(2).attributeValue);
        assertEquals("field1", definitions.get(2).subFieldPath);
    }
}

