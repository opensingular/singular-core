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

package org.opensingular.form.wicket;

import org.junit.Test;
import org.opensingular.form.SInstance;
import org.opensingular.form.wicket.feedback.FeedbackFence;
import org.opensingular.form.wicket.helpers.SingularDummyFormPageTester;

import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

public class SValidationFeedbackHandlerTest {

    @Test
    public void testBasic() {
        SingularDummyFormPageTester tester = new SingularDummyFormPageTester();
        tester.getDummyPage().setTypeBuilder(tb->tb.addFieldString("string"));
        tester.getDummyPage().setAsEditView();
        tester.startDummyPage();

        Set<? extends SInstance> lowerBound = SValidationFeedbackHandler.collectLowerBoundInstances(
                new FeedbackFence(tester.getDummyPage().getSingularFormPanel().getParent()));

        assertThat(lowerBound.isEmpty()).isFalse();
    }
}
