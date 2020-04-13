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

import java.io.Serializable;

/**
 * Se utiliza esta clase para deserializar la configuración del programa.
 *
 * @author Yago Mouriño Mendaña
 */
public class Config implements Serializable {

    private static final long serialVersionUID = -2532903982561985934L;

    private DbConnection dbConnection;
    private App app;

    public DbConnection getDbConnection() {
        return dbConnection;
    }

    public App getApp() {
        return app;
    }

    public static class DbConnection implements Serializable {

        private static final long serialVersionUID = 7838625292183744130L;

        private String address;
        private String name;
        private String user;
        private String password;

        // A dialect se le asigna un valor por defecto por si no se encontrase
        // en el fichero JSON. El valor por defecto se debe a que en el
        // enunciado de la práctica se pide usar PostgreSQL.
        private String dialect = "org.hibernate.dialect.PostgreSQL10Dialect";

        public DbConnection() {
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDialect() {
            return dialect;
        }

        public void setDialect(String dialect) {
            this.dialect = dialect;
        }
    }

    public static class App implements Serializable {

        private static final long serialVersionUID = 1907062053068318501L;

        private String directory;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }
}
