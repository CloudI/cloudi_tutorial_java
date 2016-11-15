//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:
package org.cloudi.examples.tutorial;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSON
{
    private static Gson instance;

    public synchronized static Gson object()
    {
        if (JSON.instance == null)
        {
            JSON.instance = JSON.configuration(new GsonBuilder());
        }
        return JSON.instance;
    }

    private static Gson configuration(final GsonBuilder builder)
    {
        return builder.registerTypeAdapter(JSONItem.class,
                                           new JSONItem.Serializer())
                      .registerTypeAdapter(JSONLanguage.class,
                                           new JSONLanguage.Serializer())
                      .registerTypeAdapter(JSONSubject.class,
                                           new JSONSubject.Serializer())
                      .registerTypeAdapter(JSONRecommendation.class,
                                           new JSONRecommendation.Serializer())
                      .serializeNulls()
                      .create();
    }

}

