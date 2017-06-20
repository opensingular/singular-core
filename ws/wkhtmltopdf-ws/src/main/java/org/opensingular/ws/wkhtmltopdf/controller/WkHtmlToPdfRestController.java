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

package org.opensingular.ws.wkhtmltopdf.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.opensingular.lib.commons.dto.HtmlToPdfDTO;
import org.opensingular.lib.commons.pdf.PDFUtil;
import org.opensingular.lib.commons.util.Loggable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.opensingular.ws.wkhtmltopdf.constains.HtmlToPdfConstants.CONVERT_HTML_TO_PDF_PATH;

@RestController
public class WkHtmlToPdfRestController implements Loggable {

    @ResponseBody
    @RequestMapping(value = CONVERT_HTML_TO_PDF_PATH, method = RequestMethod.POST, produces = "application/pdf")
    public ResponseEntity<InputStreamResource> convertHtmlToPdf(@RequestBody HtmlToPdfDTO dto, HttpServletRequest request, HttpServletResponse response ) {
        try {
            File file = PDFUtil.getInstance().convertHTML2PDF(dto.getBody(), dto.getHeader(), dto.getFooter());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .contentLength(file.length())
                    .body(new InputStreamResource(new FileInputStream(file)));
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
        }
        return null;
    }
    
    @ResponseBody
    @RequestMapping(value = CONVERT_HTML_TO_PDF_PATH + "stream", method = RequestMethod.POST, produces = "application/pdf")
    public void convertHtmlToPdfStream(@RequestBody HtmlToPdfDTO dto, HttpServletResponse response) {
       
        try (InputStream in = new FileInputStream(
                PDFUtil.getInstance().convertHTML2PDF(dto.getBody(), dto.getHeader(), dto.getFooter()))) {
            IOUtils.copy(in, response.getOutputStream());
            response.setContentType("application/pdf");
            response.flushBuffer();
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
        }
    }

}