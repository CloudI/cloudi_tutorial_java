//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;

public class JSONSubjectListResponse extends JSONResponse
{
    private final List<JSONSubject> subjects;

    public static JSONSubjectListResponse success(final List<JSONSubject> l)
    {
        return new JSONSubjectListResponse(true, null, l);
    }

    public static JSONSubjectListResponse failure(final String error)
    {
        final List<JSONSubject> empty = new LinkedList<JSONSubject>();
        return new JSONSubjectListResponse(false, error, empty);
    }

    private JSONSubjectListResponse(final boolean success,
                                    final String error,
                                    final List<JSONSubject> subjects)
    {
        super(JSONSubjectListRequest.message_name_valid, success, error);
        this.subjects = subjects;
    }

}

