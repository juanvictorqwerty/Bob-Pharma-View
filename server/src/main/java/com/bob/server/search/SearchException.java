package com.bob.server.search;

public class SearchException extends RuntimeException {

    private final SearchValidation validation;

    public SearchException(SearchValidation validation) {
        super(validation.getMessage());
        this.validation = validation;
    }

    public SearchValidation getValidation() {
        return validation;
    }
}