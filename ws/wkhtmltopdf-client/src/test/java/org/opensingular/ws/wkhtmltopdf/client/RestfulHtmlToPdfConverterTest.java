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

package org.opensingular.ws.wkhtmltopdf.client;

import org.junit.Assert;
import org.junit.Test;
import org.opensingular.lib.commons.base.SingularPropertiesImpl;
import org.opensingular.ws.wkhtmltopdf.constains.HtmlToPdfConstants;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

public class RestfulHtmlToPdfConverterTest {

    @Test
    public void testCreateUsingDefaultConfig(){
        SingularPropertiesImpl.Tester.runInSandbox(props -> {
            props.setProperty(HtmlToPdfConstants.ENDPOINT_WS_WKHTMLTOPDF, "http:\\someplace");
            Assert.assertNotNull(RestfulHtmlToPdfConverter.createUsingDefaultConfig());
        });
    }

    @Test
    public void testInstantiateByConstructor(){
        Assert.assertNotNull(new RestfulHtmlToPdfConverter("endpoint"));
    }

    @Test
    public void testConvertWithNullValue(){
        SingularPropertiesImpl.Tester.runInSandbox(props -> {
            props.setProperty(HtmlToPdfConstants.ENDPOINT_WS_WKHTMLTOPDF, "http:\\someplace");

            RestfulHtmlToPdfConverter usingDefaultConfig = RestfulHtmlToPdfConverter.createUsingDefaultConfig();
            Optional<File> convert = usingDefaultConfig.convert(null);
            Assert.assertFalse(convert.isPresent());
        });
    }
    
    @Test
    public void testConvertStreamWithNullValue(){
        SingularPropertiesImpl.Tester.runInSandbox(props -> {
            props.setProperty(HtmlToPdfConstants.ENDPOINT_WS_WKHTMLTOPDF, "http:\\someplace");

            RestfulHtmlToPdfConverter usingDefaultConfig = RestfulHtmlToPdfConverter.createUsingDefaultConfig();
            InputStream in = usingDefaultConfig.convertStream(null);
            Assert.assertNull(in);
        });
    }
}
