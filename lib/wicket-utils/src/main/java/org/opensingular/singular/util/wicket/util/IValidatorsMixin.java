/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.singular.util.wicket.util;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

import org.opensingular.singular.commons.lambda.IPredicate;
import org.opensingular.singular.util.wicket.validator.BaseValidator;
import org.opensingular.singular.util.wicket.validator.NotFutureDateValidator;

@SuppressWarnings({ "serial" })
public interface IValidatorsMixin extends Serializable {

    default <T> IValidator<T> validator(IPredicate<T> isInvalidTest, IModel<String> errorMessage) {
        return new BaseValidator<T>() {
            @Override
            public void validate(IValidatable<T> validatable) {
                if (isInvalidTest.test(validatable.getValue())) {
                    validatable.error(validationError(errorMessage));
                }
            }
        };
    }

    default NotFutureDateValidator notFutureDate(IModel<String> errorMessage) {
        return new NotFutureDateValidator(errorMessage);
    }

    default IValidator<String> minLength(int minLength, IModel<String> errorMessage) {
        return validator(value -> value.length() < minLength, errorMessage);
    }
    default IValidator<String> maxLength(int maxLength, IModel<String> errorMessage) {
        return validator(value -> value.length() > maxLength, errorMessage);
    }
}