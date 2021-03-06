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

package org.opensingular.internal.lib.commons.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Recebe os eventos de erro durante o parse do XML e os guardar para serem
 * recebidos ao final da execução de parse. Desse modo, é possível a aplicação
 * listar todos os erros encontrados. <p/>Caso aparece um exceção com uma
 * exceção encadeada, então é disparada a exception de modo a não mascarar o
 * erro.
 *
 * @author Daniel C. Bordin
 */
final class ErrorHandlerMElement implements ErrorHandler {

    /**
     * Armazena a lista de erros encontrada. *
     */
    private StringBuilder buf_;

    /**
     * Adiciona o erro informado ao buffer para posterior recuperação. A única
     * exceção e se houver uma outra exceção encadeada, o que nesse caso provoca
     * um disparado da exceção recebida.
     *
     * @param type Nome do nível da exception.
     * @param ex Dados do erro.
     * @throws SAXParseException Redisparo de "ex" se essa conter outra
     * exception.
     */
    private void addToBuffer(String type, SAXParseException ex) throws SAXParseException {
        if (buf_ == null) {
            buf_ = new StringBuilder(1024);
            buf_.append("Erro(s) efetuando parse");
        }
        buf_.append('\n');
        buf_.append(type).append(':');
        if (ex.getLineNumber() > -1) {
            buf_.append(" Line=").append(ex.getLineNumber());
        }
        if (ex.getColumnNumber() > -1) {
            buf_.append(" Col=").append(ex.getColumnNumber());
        }
        if (ex.getSystemId() != null) {
            buf_.append(": uri=").append(ex.getSystemId());
        }
        if (ex.getPublicId() != null) {
            buf_.append(": id=").append(ex.getPublicId());
        }
        if (ex.getMessage() != null) {
            buf_.append(": ").append(ex.getMessage());
        }
        //Redisparo da exceção se existir outra encadeada (não mascara).
        if (ex.getException() != null) {
            throw ex;
        }
    }

    /**
     * Perti verificar se existem erros sem força a geração de String a partir
     * do buffer.
     *
     * @return true se houve pelo menos um erro registrado.
     */
    public boolean hasErros() {
        return buf_ != null;
    }

    /**
     * Retorna um descrição dos erros encontrados.
     *
     * @return Null se não ocorreu nenhum erro.
     */
    public String getErros() {
        if (buf_ == null) {
            return null;
        }
        return buf_.toString();
    }

    /**
     * Joga o error para o buffer de erros sem disparar exception.
     *
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {
        addToBuffer("Error", exception);
    }

    /**
     * Joga o error para o buffer de erros sem disparar exception.
     *
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        addToBuffer("FatalError", exception);
    }

    /**
     * Ignora esse erro sem jogar para o buffer e sem dispara uma exception.
     *
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception)  {
    }

}