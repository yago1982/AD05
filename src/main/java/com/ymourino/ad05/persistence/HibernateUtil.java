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
package com.ymourino.ad05.persistence;

import com.ymourino.ad05.persistence.models.Directory;
import com.ymourino.ad05.persistence.models.File;
import com.ymourino.ad05.utils.Config;
import com.ymourino.ad05.utils.ConfigBuilder;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;
import org.hibernate.Session;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * Clase que implementa el patrón Singleton para acceder a la base de datos.
 *
 * @author Yago Mouriño Mendaña
 */
public class HibernateUtil implements IJPAUtil {

    private static HibernateUtil hibernateUtil;
    private final EntityManagerFactory entityManagerFactory;
    private final Session session;

    public static HibernateUtil getHibernateUtil() throws Exception {
        if (hibernateUtil == null) {
            hibernateUtil = new HibernateUtil();
        }

        return hibernateUtil;
    }

    private HibernateUtil() throws Exception {
        Configuration configuration = new Configuration();
        Properties properties = new Properties();

        ConfigBuilder config = ConfigBuilder.getConfig();
        Config.DbConnection dbConnection = config.getDbConnection();

        properties.put(Environment.URL, "jdbc:postgresql://"
                + dbConnection.getAddress()
                + "/" + dbConnection.getName());
        properties.put(Environment.USER, dbConnection.getUser());
        properties.put(Environment.PASS, dbConnection.getPassword());
        properties.put(Environment.DIALECT, dbConnection.getDialect());

        // Con update se crea (o actualiza) la base de datos, pero si ya
        // está creada y no se requieren cambios, los datos y estructura
        // se conservan.
        properties.put(Environment.HBM2DDL_AUTO, "update");

        configuration.setProperties(properties);

        configuration.addAnnotatedClass(Directory.class);
        configuration.addAnnotatedClass(File.class);

        final StandardServiceRegistry registry
                = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

        entityManagerFactory = configuration.buildSessionFactory(registry);
        session = entityManagerFactory.createEntityManager().unwrap(Session.class);
    }

    @Override
    public void close() {
        if (session != null && session.isOpen()) {
            session.close();
        }

        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Override
    public boolean saveElement(Object element) {
        if (element != null) {
            try {
                session.beginTransaction();
                session.save(element);
                session.getTransaction().commit();
                return true;
            } catch (Exception ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
                session.getTransaction().rollback();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteElement(Object element) {
        if (element != null) {
            try {
                session.beginTransaction();
                session.delete(element);
                session.getTransaction().commit();
                return true;
            } catch (Exception ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
                session.getTransaction().rollback();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean updateElement(Object element) {
        if (element != null) {
            try {
                session.beginTransaction();
                session.update(element);
                session.getTransaction().commit();
                return true;
            } catch (Exception ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
                session.getTransaction().rollback();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public <T, U> T getElement(U id, Class<T> type) {
        if (id != null) {
            return session.find(type, id);
        } else {
            return null;
        }
    }

    @Override
    public <T> T getElement(String query, Class<T> type) {
        List<T> element = getElements(query, type);

        if (!element.isEmpty()) {
            return element.get(0);
        } else {
            return null;
        }
    }

    @Override
    public <T> List<T> getElements(String query, Class<T> type) {
        return session.createQuery(query, type).getResultList();
    }

    @Override
    public Session getSession() {
        return this.session;
    }
}
