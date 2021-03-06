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

package org.opensingular.schedule;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builder for {@link IScheduleData}.
 *
 * @author lucas.lopes
 */
public class ScheduleDataBuilder {

    private static final String[] WEEK_DAYS_NAMES = new String[]{"domingo", "segunda", "terça", "quarta", "quinta", "sexta", "sábado"};

    private ScheduleDataBuilder() {
        /* CONSTRUTOR VAZIO */
    }

    public static IScheduleData buildMinutely(int minutes) {
        String description    = "Repetido a cada " + minutes + "m";
        String cronExpression = generateCronExpression("0", "0/" + Integer.toString(minutes), "*", "*", "*", "?", "");
        return new ScheduleDataImpl(cronExpression, description);
    }

    /**
     *
     * @param cron
     * @return
     * an {@link IScheduleData} built using the cron expression represented by the {@param cron}
     * The expression is not validated at this time
     */
    public static IScheduleData buildFromCron(String cron) {
        String description = "Expressão cron '" + cron + "'";
        return new ScheduleDataImpl(cron, description);
    }


    public static IScheduleData buildHourly(int hours) {
        String description    = "Repetido a cada " + hours + "h";
        String cronExpression = generateCronExpression("0", "0", "0/" + Integer.toString(hours), "*", "*", "?", "");

        return new ScheduleDataImpl(cronExpression, description);
    }

    /**
     * @param hours   mandatory = yes. allowed values = {@code 0-23}
     * @param minutes mandatory = yes. allowed values = {@code 0-59}
     * @return {@link IScheduleData}
     */
    public static IScheduleData buildDaily(int hours, int minutes) {
        String description = "Diário às " + hours + ':' + (minutes < 10 ? "0" : "") + minutes + "h";
        String cronExpression = generateCronExpression("0", Integer.toString(minutes),
                Integer.toString(hours), "*", "*", "?", "");

        return new ScheduleDataImpl(cronExpression, description);
    }

    /**
     * @param hours     mandatory = yes. allowed values = {@code 0-23}
     * @param minutes   mandatory = yes. allowed values = {@code 0-59}
     * @param dayOfWeek mandatory = yes. allowed values = {@code 0-6  (0=SUN)}
     * @return {@link IScheduleData}
     */
    public static IScheduleData buildWeekly(int hours, int minutes, Integer... dayOfWeek) {
        Preconditions.checkArgument(dayOfWeek.length > 0, "any dayOfWeek provided");

        try (Stream<Integer> dayOfWeekStream = Arrays.stream(dayOfWeek)) {

            String daysDescription = dayOfWeekStream.map((day) -> WEEK_DAYS_NAMES[day]).collect(Collectors.joining(","));
            String description = "Semanal: " + daysDescription
                    + " às " + hours + ':' + (minutes < 10 ? "0" : "") + minutes + "h";

            try (Stream<Integer> dayOfWeekStream2 = Arrays.stream(dayOfWeek)) {

                String cronExpression = generateCronExpression("0", Integer.toString(minutes),
                        Integer.toString(hours), "*", "*", dayOfWeekStream2.map(Object::toString)
                                .collect(Collectors.joining(",")), "");
                return new ScheduleDataImpl(cronExpression, description);
            }
        }
    }

    /**
     * @param dayOfMonth mandatory = yes. allowed values = {@code 1-31}
     * @param hours      mandatory = yes. allowed values = {@code 0-23}
     * @param minutes    mandatory = yes. allowed values = {@code 0-59}
     * @param months     mandatory = no. allowed values = {@code 1-12}
     * @return {@link IScheduleData}
     */
    public static IScheduleData buildMonthly(int dayOfMonth, int hours, int minutes, Integer... months) {
        if (months.length == 0) {
            String description = "Mensal: todo dia " + dayOfMonth
                    + " às " + hours + ':' + (minutes < 10 ? "0" : "") + minutes + "h";
            String cronExpression = generateCronExpression("0", Integer.toString(minutes), Integer.toString(hours),
                    Integer.toString(dayOfMonth), "*", "?", "");
            return new ScheduleDataImpl(cronExpression, description);
        } else {

            try (Stream<Integer> stream = Arrays.stream(months)) {

                String monthsDescription = stream.map(Object::toString).collect(Collectors.joining(","));
                String description = "Mensal: todo dia " + dayOfMonth
                        + " às " + hours + ':' + (minutes < 10 ? "0" : "") + minutes + "h"
                        + " nos meses: " + monthsDescription;

                String cronExpression = generateCronExpression("0", Integer.toString(minutes),
                        Integer.toString(hours), Integer.toString(dayOfMonth), monthsDescription, "?", "");

                return new ScheduleDataImpl(cronExpression, description);
            }
        }
    }

    /**
     * Generate a CRON expression is a string comprising 6 or 7 fields separated by white space.
     *
     * @param seconds    mandatory = yes. allowed values = {@code  0-59    * / , -}
     * @param minutes    mandatory = yes. allowed values = {@code  0-59    * / , -}
     * @param hours      mandatory = yes. allowed values = {@code 0-23   * / , -}
     * @param dayOfMonth mandatory = yes. allowed values = {@code 1-31  * / , - ? L W}
     * @param month      mandatory = yes. allowed values = {@code 1-12 or JAN-DEC    * / , -}
     * @param dayOfWeek  mandatory = yes. allowed values = {@code 0-6 or SUN-SAT * / , - ? L #}
     * @param year       mandatory = no. allowed values = {@code 19702099    * / , -}
     * @return a CRON Formatted String.
     */
    private static String generateCronExpression(final String seconds, final String minutes, final String hours,
                                                 final String dayOfMonth, final String month, final String dayOfWeek, final String year) {
        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s", seconds, minutes, hours, dayOfMonth,
                month, dayOfWeek, year).trim();
    }
}
