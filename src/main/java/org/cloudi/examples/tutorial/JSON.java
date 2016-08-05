//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import com.google.gson.Gson;

public class JSON
{
    private static Gson instance;

    public static Gson object()
    {
        if (JSON.instance == null)
        {
            JSON.instance = new Gson();
        }
        return JSON.instance;
    }

}

