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

;(function ($) {
    "use strict";
    if (window.hasOwnProperty('BSTAB')) {
        return;
    }
    window.BSTAB = (function () {


        removeAllActiveTabs();
        var href = window.location.href;

        function changeUrl(page, url) {

            if (typeof (history.pushState) != "undefined") {
                var obj = {Page: page, Url: url};
                history.pushState(obj, obj.Page, href + obj.Url);
            } else {
                console.log("Browser does not support HTML5.");
            }
        }

        /**
         * This method remove the class of all active tab-pane active, because have to be just one tab-pane active.
         */
        function removeAllActiveTabs() {
            $('.singular-container-tab').removeClass('tab-pane active');
        }

        return {
            "changeUrl": changeUrl
        }
    }())
}(jQuery));