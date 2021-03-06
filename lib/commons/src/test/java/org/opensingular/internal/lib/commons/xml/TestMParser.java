/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.opensingular.internal.lib.commons.xml;

import junit.framework.TestCase;
import org.junit.Test;
import org.opensingular.lib.commons.base.SingularException;

/**
 * @author Daniel C. Bordin
 */
public class TestMParser extends TestCase {

    public TestMParser(String name) {
        super(name);
    }

    public void testParseXMLDentroDeXML() throws Exception {
        MElement original = MElement.newInstance("X");
        original.addElement("TTT");

        MElement externo = MElement.newInstance("Y");
        externo.addElement("conteudo", original.toStringExato());

        System.out.println(externo.toStringExato());
        MElement newExternal = MParser.parse(externo.toStringExato());

        assertEquals(externo.toStringExato(), newExternal.toStringExato());

        assertEquals(newExternal.getValue("conteudo"), original.toStringExato());
    }

    @Test(expected = SingularException.class)
    public void testAddInputSourceException(){
        MParser parser = new MParser();
        try {
            parser.addInputSource("id", String.class, "invalidValue");
        } catch (Exception e) {
            SingularException.rethrow(e);
        }
    }

    @Test(expected = SingularException.class)
    public void testParseComResolver() {
        MDocument document = MDocument.newInstance();
        MElement root = document.createRoot("raiz");
        root.addElement("filho", "filhoVal");

        MParser parser = new MParser();
        try {
            parser.parseComResolver(root.toString());
        } catch (Exception e) {
            SingularException.rethrow(e);
        }
    }
}
