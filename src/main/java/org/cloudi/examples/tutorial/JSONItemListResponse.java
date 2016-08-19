//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;

public class JSONItemListResponse extends JSONResponse
{
    private final Long user_id;
    private String language;
    private String subject;
    private final List<JSONItem> items;

    public static JSONItemListResponse success(final long user_id,
                                               final String language,
                                               final String subject,
                                               final List<JSONItem> items)
    {
        return new JSONItemListResponse(true, null, user_id,
                                        language, subject, items);
    }

    public static JSONItemListResponse failure(final String error,
                                               final long user_id,
                                               final String language,
                                               final String subject)
    {
        final List<JSONItem> empty = new LinkedList<JSONItem>();
        return new JSONItemListResponse(false, error, user_id,
                                        language, subject, empty);
    }

    public static JSONItemListResponse failure(final String error)
    {
        final List<JSONItem> empty = new LinkedList<JSONItem>();
        return new JSONItemListResponse(false, error, null, null, null, empty);
    }

    private JSONItemListResponse(final boolean success,
                                 final String error,
                                 final Long user_id,
                                 final String language,
                                 final String subject,
                                 final List<JSONItem> items)
    {
        super(JSONItemListRequest.message_name_valid,
              success, error);
        this.user_id = user_id;
        this.language = language;
        this.subject = subject;
        this.items = items;
    }

}

