//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;

public class JSONLanguageListResponse extends JSONResponse
{
    private final List<JSONLanguage> languages;

    public static JSONLanguageListResponse success(final List<JSONLanguage> l)
    {
        return new JSONLanguageListResponse(true, null, l);
    }

    public static JSONLanguageListResponse failure(final String error)
    {
        final List<JSONLanguage> empty = new LinkedList<JSONLanguage>();
        return new JSONLanguageListResponse(false, error, empty);
    }

    private JSONLanguageListResponse(final boolean success,
                                     final String error,
                                     final List<JSONLanguage> languages)
    {
        super(JSONLanguageListRequest.message_name_valid, success, error);
        this.languages = languages;
    }

}

