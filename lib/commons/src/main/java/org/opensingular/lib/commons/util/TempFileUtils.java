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

package org.opensingular.lib.commons.util;

import org.opensingular.lib.commons.base.SingularException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TempFileUtils {

    private TempFileUtils() {
    }

    private static boolean exists(File f) {
        return (f != null) && f.exists();
    }

    /**
     * Tenta apagar o arquivo informado se o mesmo existir. Não conseguindo apagar ou ocorrendo uma exception ao
     * chamar {@link File#delete()}, faz log do erro mas não dispara exception.
     *
     * @param file      Arquivo a ser apagado
     * @param requester Classe junta a qual será gravado o log de erro do delete
     */
    public static void deleteAndFailQuietily(@Nonnull File file, @Nonnull Object requester) {
        delete(file, requester, true);
    }

    /**
     * Tenta apagar o arquivo informado se o mesmo existir. Não conseguindo apagar ou ocorrendo uma exception ao
     * chamar {@link File#delete()}, faz log do erro e dispara exception.
     *
     * @param file      Arquivo a ser apagado
     * @param requester Classe junta a qual será gravado o log de erro do delete
     */
    public static void deleteOrException(@Nonnull File file, @Nonnull Object requester) {
        delete(file, requester, false);
    }

    /**
     * Tenta apagar o arquivo informado se o mesmo existir. Sempre gera log de erro senão conseguri apagar.
     *
     * @param file         Arquivo a ser apagado
     * @param requester    Classe junta a qual será gravado o log de erro do delete
     * @param failQuietily Indica qual o comportamento se não conseguindo apagar ou ocorrendo uma exception ao
     *                     chamar {@link File#delete()}. Se true, engole a exception de erro. Se false, dispara
     *                     exception senão conseguir apagar ou se ocorre exception no processo.
     */
    private static void delete(@Nonnull File file, @Nonnull Object requester, boolean failQuietily) {
        Objects.requireNonNull(requester);
        if (file.exists()) {
            try {
                if (!file.delete()) {
                    dealWithDeleteErro(file, requester, failQuietily, null);
                }
            } catch (Exception e) {
                dealWithDeleteErro(file, requester, failQuietily, e);
            }
        }
    }

    /**
     * Faz log do erro do delete e dispara exception se necessário.
     */
    private static void dealWithDeleteErro(@Nonnull File file, @Nonnull Object requester, boolean failQuietily,
                                           @Nullable Exception e) {
        Class<?> req = requester instanceof Class ? (Class<?>) requester : requester.getClass();
        String msg = "Nao foi possível apagar o arquivo " + file;
        if (requester != null) {
            msg += " (solicitação da classe " + req.getName() + ")";
        }
        if (failQuietily) {
            Logger logger = Logger.getLogger(req.getName());
            logger.log(Level.SEVERE, msg, e);
        } else {
            throw SingularException.rethrow(msg, e);
        }
    }

}
