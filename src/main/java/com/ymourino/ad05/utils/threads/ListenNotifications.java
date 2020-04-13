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
package com.ymourino.ad05.utils.threads;

import com.ymourino.ad05.persistence.DBHelper;
import com.ymourino.ad05.persistence.HibernateUtil;
import com.ymourino.ad05.persistence.IJPAUtil;
import com.ymourino.ad05.persistence.models.File;
import com.ymourino.ad05.utils.Config;
import com.ymourino.ad05.utils.ConfigBuilder;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

/**
 *
 * @author Yago Mouriño Mendaña
 */
public class ListenNotifications extends Thread {

    private IJPAUtil hibernateUtil;
    private PGConnection pgconn;
    private String rootPath;

    public ListenNotifications(String rootPath) throws Exception {
        this.rootPath = rootPath;
        hibernateUtil = HibernateUtil.getHibernateUtil();

        ConfigBuilder config = ConfigBuilder.getConfig();
        Config.DbConnection dbConnection = config.getDbConnection();

        Class.forName("org.postgresql.Driver");
        String pgUrl = "jdbc:postgresql://"
                + dbConnection.getAddress()
                + "/" + dbConnection.getName();

        Connection conn = DriverManager.getConnection(
                pgUrl,
                dbConnection.getUser(),
                dbConnection.getPassword());

        pgconn = conn.unwrap(org.postgresql.PGConnection.class);
        Statement stmt = conn.createStatement();
        stmt.execute("LISTEN nuevofichero");
        stmt.close();
    }

    public void run() {
        try {
            while (true) {
                PGNotification notifications[] = pgconn.getNotifications();

                if (notifications != null) {
                    Session session = hibernateUtil.getSession();
                    session.beginTransaction();

                    for (int i = 0; i < notifications.length; i++) {
                        File newFile = hibernateUtil.getElement(
                                Long.parseLong(notifications[i].getParameter()),
                                File.class);
                        DBHelper.restoreFileFromDB(newFile, Paths.get(rootPath));
                    }

                    session.getTransaction().commit();
                }

                Thread.sleep(500);
            }
        } catch (Exception ex) {
            Logger.getLogger(ListenNotifications.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
