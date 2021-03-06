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

package org.opensingular.lib.commons.net;

import org.junit.Assert;
import org.junit.Test;
import org.opensingular.lib.commons.base.SingularException;

public class LnkTest {

    @Test
    public void constructorsTest(){
        Lnk url1 = new Lnk("url", false);
        Assert.assertEquals("url", url1.getUrl());
        Assert.assertFalse(url1.isUrlAppMissing());

        Assert.assertNotNull(Lnk.of("path://url"));

        Assert.assertNotNull(Lnk.of("urlApp", "path"));

        Assert.assertNotNull(Lnk.of("urlAppNew", url1));

        Assert.assertEquals(url1, url1.addUrlApp(null));

        Lnk url2 = Lnk.of(null, "url");
        Assert.assertTrue(url2.isUrlAppMissing());
    }

    @Test
    public void someMethodsTest(){
        Lnk url1 = new Lnk("url", false);
        Assert.assertEquals("<a href=\"url\">(ver)</a>", url1.getHref());
        Assert.assertEquals("<a href=\""+url1.getUrl()+"\">"+url1.getUrl()+"</a>", url1.getHrefUrl());
        Assert.assertEquals("url/path", Lnk.concat("url", "path"));
        Assert.assertEquals("url", url1.getUrl("app"));

        Assert.assertNotEquals(url1, url1.appendPath("path"));

        Assert.assertEquals(url1, url1.addParamSeNaoPresente("integerValue", (Integer) null));
        Assert.assertEquals(url1, url1.addParamSeNaoPresente("integerValue", null));
        Assert.assertNotEquals(url1, url1.addParamSeNaoPresente("integerValue", 123));

        Assert.assertEquals("<img src=\"url\"/>",url1.getImg());
        Assert.assertEquals("<img src=\"url\" title=\"title\"/> ", url1.getImg("title"));

        Assert.assertEquals("<a href=\"url\" title=\"titleHref\">textLink</a>", url1.getHref("textLink", "titleHref"));
        Assert.assertEquals("<a target=\"_blank\" href=\"url\" title=\"titleHref\">textLink</a>",
                url1.getHrefBlank("textLink", "titleHref"));

        Assert.assertNotNull(url1.toString());

        Assert.assertEquals("urlApp/url", Lnk.of(null, "url").getUrl("urlApp"));
    }

    @Test(expected = SingularException.class)
    public void getUrlException(){
        Lnk url1 = new Lnk("url", true);
        url1.getUrl();
    }

    @Test
    public void andTest(){
        Lnk url1 = Lnk.of("url");
        Assert.assertNotEquals(url1, url1.and("parameter", 01));

        Assert.assertEquals(url1, url1.and("parameter2", (Integer) null));

        Assert.assertNotEquals(url1, url1.and("parameter3", "1234"));

        Assert.assertEquals(url1, url1.and("parameter3", (String) null));

        Lnk url2 = Lnk.of(null, "url");
        Assert.assertNotEquals(url2, url2.addUrlApp("url2"));
    }
}
