package com.migu.utils.validator;

public interface Validator<T> {
    void validate(T element) throws ValidatorException;
}
