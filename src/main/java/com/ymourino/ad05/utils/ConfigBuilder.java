/*
 * The MIT License
 *
 * Copyright 2020 Yago Mouriño Mendaña
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ymourino.ad05.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.ymourino.ad05.Main;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Esta clase deserializa la configuración del programa y permite acceder a ella
 * de forma sencilla (utilizando el patrón Singleton).
 *
 * @author Yago Mouriño Mendaña
 */
public class ConfigBuilder {

    private static ConfigBuilder config;
    private final Config.DbConnection dbConnection;
    private final Config.App app;

    public static ConfigBuilder getConfig() throws Exception {
        if (config == null) {
            config = new ConfigBuilder();
        }

        return config;
    }

    private ConfigBuilder() throws Exception {
        Gson gson = new Gson();

        // Se proporciona un fichero de configuración base en los recursos del
        // programa, pero si existe un fichero de configuración junto al JAR
        // del proyecto, será este último fichero el que será usado para cargar
        // la configuración.
        //
        // {
        //     "dbConnection": {
        //         "address": "192.168.56.102",
        //         "name": "minidrive",
        //         "user": "accesodatos",
        //         "password": "abc123."
        //     },
        //
        //     "app":{
        //         "directory": "/home/user/minidrive"
        //     }
        // }
        //
        JsonReader jr;

        if (Files.exists(Paths.get("config.json"))
                && !Files.isDirectory(Paths.get("config.json"))) {
            jr = new JsonReader(
                    new FileReader("config.json"));
        } else {
            jr = new JsonReader(
                    new InputStreamReader(
                            Main.class.getResourceAsStream("/config.json")));
        }

        Config configuration = gson.fromJson(jr, Config.class);
        dbConnection = configuration.getDbConnection();
        app = configuration.getApp();
    }

    public Config.DbConnection getDbConnection() {
        return dbConnection;
    }

    public Config.App getApp() {
        return app;
    }
}
