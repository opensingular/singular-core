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

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Fornece métodos de conversão de string de Data/Hora no formato
 * ISO8601 para objeto Java (Date, Timestamp, Calendar, etc.). O
 * formato ISO8601 é utilizado para manter compatibildiade com a formatação
 * de tipos do XML Schema (ver http://www.w3.org).<p>
 * <p>
 * O formato ISO8601 é indenpendente de Locale. Um exemplo é<br>
 * 1999-05-31T13:20:00.000-05:00<p>
 * <p>
 * Para maiores informações sobre o formato veja
 * <a href="http://www.w3.org/TR/xmlschema-0/">http://www.w3.org/TR/xmlschema-0/
 * </a>.
 *
 * @author Daniel C. Bordin
 */
public final class ConversorDataISO8601 {

    /**
     * Separador entre a data e as informações de hora
     */
    private static final char SEPARADOR_DATA_HORA = 'T';

    private static final byte ANO     = 1;
    private static final byte MES     = 2;
    private static final byte DIA     = 3;
    private static final byte HORA    = 4;
    private static final byte MINUTO  = 5;
    private static final byte SEGUNDO = 6;
    private static final byte MILI    = 7;
    private static final byte NANO    = 8;

    private ConversorDataISO8601() {
    }

    public static String format(java.util.Date d) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);

        int  mili      = gc.get(Calendar.MILLISECOND);
        byte prescisao = SEGUNDO;
        if (mili != 0) {
            prescisao = MILI;
        }

        return format(
                gc.get(Calendar.YEAR),
                gc.get(Calendar.MONTH) + 1,
                gc.get(Calendar.DAY_OF_MONTH),
                gc.get(Calendar.HOUR_OF_DAY),
                gc.get(Calendar.MINUTE),
                gc.get(Calendar.SECOND),
                mili,
                0,
                prescisao);
    }

    public static java.util.Date getDate(String s) {
        return getCalendar(s).getTime();
    }

    public static String format(Calendar gc) {
        return format(
                gc.get(Calendar.YEAR),
                gc.get(Calendar.MONTH) + 1,
                gc.get(Calendar.DAY_OF_MONTH),
                gc.get(Calendar.HOUR_OF_DAY),
                gc.get(Calendar.MINUTE),
                gc.get(Calendar.SECOND),
                gc.get(Calendar.MILLISECOND),
                0,
                MILI);
    }

    public static GregorianCalendar getCalendar(String s) {
        int[] t = valueOf(s);
        GregorianCalendar gc =
                new GregorianCalendar(t[ANO], t[MES] - 1, t[DIA], t[HORA], t[MINUTO], t[SEGUNDO]);
        if (t[NANO] != 0) {
            gc.set(Calendar.MILLISECOND, t[NANO] / 1000000);
        }
        return gc;
    }

    /**
     * Classe de apoio para o parse da Data no formato ISO8601
     */
    private static class LeitorString {
        private final String texto_;
        private       int    pos_;

        public LeitorString(String texto) {
            texto_ = texto;
        }

        public boolean isNotEnd() {
            return pos_ != texto_.length();
        }

        public int lerNumero(int digitosMinimos, int digitosMaximos, boolean shiftMaximo) {
            int  p = 0;
            int  n = 0;
            char c;
            while (pos_ < texto_.length()) {
                c = texto_.charAt(pos_);
                if (Character.isDigit(c)) {
                    if (p == 0) {
                        n = (c - '0');
                    } else {
                        n = n * 10 + (c - '0');
                    }
                } else {
                    if (p == 0) {
                        throw erroFormato();
                    } else {
                        break;
                    }
                }
                pos_++;
                p++;
            }
            validar(digitosMinimos, digitosMaximos, p);
            n = letShiftMaximo(digitosMaximos, shiftMaximo, p, n);
            return n;
        }

        protected void validar(int digitosMinimos, int digitosMaximos, int p) {
            if ((p < digitosMinimos) || (p > digitosMaximos)) {
                throw erroFormato();
            }
        }

        protected int letShiftMaximo(int digitosMaximos, boolean shiftMaximo, int p, int n) {
            int _p = p;
            int _n = n;
            if (shiftMaximo) {
                for (; _p < digitosMaximos; _p++) {
                    _n *= 10;
                }
            }
            return _n;
        }

        public void lerSeparadorData() {
            char c = lerCaracter();
            if (!((c == '-') || (c == '.') || (c == '/'))) {
                throw erroFormato();
            }
        }

        public void lerSeparadorDataHora() {
            char c = lerCaracter();
            if (!((c == SEPARADOR_DATA_HORA) || (c == ' '))) {
                throw erroFormato();
            }
        }

        public boolean hasChar(char c) {
            if ((pos_ != texto_.length()) && (c == texto_.charAt(pos_))) {
                pos_++;
                return true;
            }
            return false;
        }

        public void lerCaracter(char c) {
            if (c != lerCaracter()) {
                throw erroFormato();
            }
        }

        public char lerCaracter() {
            if (pos_ == texto_.length()) {
                throw erroFormato();
            }
            return texto_.charAt(pos_++);
        }

        public RuntimeException erroFormato() {
            throw new IllegalArgumentException(
                    "A string '" + texto_ + "' deveria estar no formato yyyy-mm-dd hh:mm:ss.fffffffff");
        }

    }

    private static int[] valueOf(String s) {

        if (s == null) {
            throw new java.lang.IllegalArgumentException("string null");
        }
        LeitorString leitor = new LeitorString(s);

        int[] t = new int[NANO + 1];

        t[ANO] = leitor.lerNumero(4, 10, false);
        leitor.lerSeparadorData();
        t[MES] = leitor.lerNumero(1, 2, false);
        leitor.lerSeparadorData();
        t[DIA] = leitor.lerNumero(1, 2, false);

        // hora opcional
        if (leitor.isNotEnd()) {
            leitor.lerSeparadorDataHora();
            t[HORA] = leitor.lerNumero(1, 2, false);
            leitor.lerCaracter(':');
            t[MINUTO] = leitor.lerNumero(1, 2, false);
            //segundos opcionais
            if (leitor.isNotEnd()) {
                leitor.lerCaracter(':');
                t[SEGUNDO] = leitor.lerNumero(1, 2, false);
                // nanos/milis opcionais
                if (leitor.hasChar('.')) {
                    t[NANO] = leitor.lerNumero(1, 9, true);
                }
            }
            //indicador diferença GMT em miliseconds
            //if (leitor.hasChar('-')) {
            //   int hGMT = leitor.lerNumero(1, 2, false);
            //    leitor.lerCaracter(':');
            //    int mGMT = leitor.lerNumero(1, 2, false);
            //    gmtMili = (hGMT * 60 + mGMT) * 60 * 1000;
            //}
        }

        if (leitor.isNotEnd()) {
            throw leitor.erroFormato();
        }

        return t;
    }

    private static String format(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second, int milli,
            int nano,
            byte precision) {

        StringBuilder buffer = new StringBuilder(40);

        formatYearMonthDay(buffer, year, month, day);

        if ((precision == DIA) || isTimeZero(hour, minute, second, milli, nano)) {
            return buffer.toString();
        }

        buffer.append(SEPARADOR_DATA_HORA);
        format2(buffer, hour);
        buffer.append(':');
        format2(buffer, minute);
        buffer.append(':');
        format2(buffer, second);

        if (nano == 0) {
            formatMiliIfNecessary(buffer, milli, precision);
        } else if (milli != 0) {
            throw new IllegalArgumentException("Não se pode para mili e nanosegundos");
        } else {
            formatMiliAndNanoIfNecessary(buffer, nano, precision);
        }

        return buffer.toString();
    }

    private static boolean isTimeZero(int hour, int minute, int second, int milli, int nano) {
        if ((hour == 0) && (minute == 0) && (second == 0)) {
            return (milli == 0) && (nano == 0);
        }
        return false;
    }

    private static void formatYearMonthDay(StringBuilder buffer, int year, int month, int day) {
        if (year < 0) {
            throw new IllegalArgumentException("Ano Negativo");
        } else if (year < 10) {
            buffer.append("000");
        } else if (year < 100) {
            buffer.append("00");
        } else if (year < 1000) {
            buffer.append('0');
        }
        buffer.append(year);
        buffer.append('-');
        format2(buffer, month);
        buffer.append('-');
        format2(buffer, day);
    }

    private static void formatMiliAndNanoIfNecessary(StringBuilder buffer, int nano, byte prescisao) {
        int mili;
        if ((nano < 0) || (nano > 999999999)) {
            throw new IllegalArgumentException("Nanos <0 ou >999999999");
        }
        // Geralmente so tem precisão de mili segundos
        // Se forem apenas milisegundos fica .999
        // Se realm
        mili = nano / 1000000;
        formatMiliIfNecessary(buffer, mili, prescisao);
        if (prescisao == NANO) {
            int onlyNano = nano % 1000000;
            if (onlyNano != 0) {
                String nanoS = Integer.toString(onlyNano);
                for (int i = 6 - nanoS.length(); i != 0; i--) {
                    buffer.append('0');
                }
                //Trunca zeros restantes
                int ultimo = nanoS.length() - 1;
                while ((ultimo != -1) && (nanoS.charAt(ultimo) == '0')) {
                    ultimo--;
                }
                for (int i = 0; i <= ultimo; i++) {
                    buffer.append(nanoS.charAt(i));
                }
            }
        }
    }

    private static void format2(StringBuilder buffer, int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("valor negativo");
        } else if (valor < 10) {
            buffer.append('0');
        } else if (valor > 99) {
            throw new IllegalArgumentException("valor > 99");
        }
        buffer.append(valor);
    }

    private static void formatMiliIfNecessary(StringBuilder buffer, int mili, byte prescisao) {
        if (mili < 0) {
            throw new IllegalArgumentException("Milisegundos <0");
        } else if (mili > 999) {
            throw new IllegalArgumentException("Milisegundos >999");
        }
        if ((prescisao == MILI) || (prescisao == NANO)) {
            buffer.append('.');
            if (mili < 10) {
                buffer.append("00");
            } else if (mili < 100) {
                buffer.append('0');
            }
            buffer.append(mili);
        }
    }

    /**
     * Verifica se a string fornecida esta no formato ISO8601.
     *
     * @param valor a ser verificado
     * @return true se atender ao formato
     */
    public static boolean isISO8601(String valor) {
        //                01234567890123456789012345678
        //                1999-05-31T13:20:00.000-05:00
        String mascara = "????-??-??T??:??:??.???-??:??";
        if ((valor == null) || valor.length() < 10 || valor.length() > mascara.length()) {
            return false;
        }
        int tam = valor.length();
        for (int i = 0; i < tam; i++) {
            char m = mascara.charAt(i);
            if (m == '?') {
                if (!Character.isDigit(valor.charAt(i))) {
                    return false;
                }
            } else if (m != valor.charAt(i) && (i != 10 || valor.charAt(i) != ' ')) {
                return false;
            }
        }
        return true;
    }
}
