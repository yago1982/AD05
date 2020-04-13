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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hibernate.Hibernate;
import org.hibernate.Session;

/**
 * Clase con métodos estáticos para guardar ficheros en la base de datos y
 * restaurarlos.
 *
 * @author Yago Mouriño Mendaña
 */
public class DBHelper {

    /**
     * Recorre un directorio y añade toda su información (directorios y
     * ficheros) a la base de datos.
     *
     * @param path Ruta del directorio a escanear.
     * @return Un objeto Directory con la información obtenida.
     * @throws Exception
     */
    public static Directory addDirectoryToDB(String path) throws Exception {
        IJPAUtil hibernateUtil = HibernateUtil.getHibernateUtil();

        // Se normaliza la ruta que se le ha indicado al método.
        Path normalizedRootPath = Paths.get(path).toAbsolutePath().normalize();

        // Se obtiene el directorio padre de la base de datos...
        Directory rootFromDB = hibernateUtil.getElement(
                "from Directory where parent_id is null",
                Directory.class);

        // ...si existe. En caso de no existir, se crea uno nuevo.
        Directory root = Objects.requireNonNullElseGet(
                rootFromDB,
                () -> new Directory(java.io.File.separator));

        // Con Apache Commons IO se consigue una colección de los ficheros y
        // directorios que "cuelgan" del directorio indicado en la configuración.
        Collection<File> files
                = FileUtils.listFilesAndDirs(
                        normalizedRootPath.toFile(),
                        TrueFileFilter.INSTANCE,
                        TrueFileFilter.INSTANCE);

        Iterator<File> filesIterator = files.iterator();

        while (filesIterator.hasNext()) {
            File currentElement = filesIterator.next();

            if (currentElement.isDirectory()) {
                if (!Paths.get(currentElement.toString()).toAbsolutePath().normalize().toString().equals(normalizedRootPath.toString())) {
                    // Obtenemos la ruta relativa del directorio actual (con el
                    // nombre incluido). Se obtiene previamente la ruta absoluta
                    // y normalizada para evitar problemas.
                    String currentRelativePathWithName
                            = Paths.get(currentElement.toString())
                                    .toAbsolutePath()
                                    .normalize()
                                    .toString()
                                    .substring(
                                            normalizedRootPath
                                                    .toString().length() + 1);

                    // Solo se añade el directorio si no existe en la base de datos.
                    if (!root.existsDirectory(currentRelativePathWithName)) {
                        Directory directory = new Directory(currentElement.getName());
                        root.addDirectory(currentRelativePathWithName, directory);
                    }
                }
            } else if (currentElement.isFile()) {
                // Obtenemos la ruta relativa del fichero actual (con el nombre
                // incluido). Se obtiene previamente la ruta absoluta y normalizada
                // para evitar problemas.
                String currentRelativePathWithName
                        = Paths.get(currentElement.toString())
                                .toAbsolutePath()
                                .normalize()
                                .toString()
                                .substring(
                                        normalizedRootPath
                                                .toString().length() + 1);

                // Solo se añade el fichero si no existe en la base de datos.
                // TODO: No se comprueba si hay diferencias entre el fichero de
                //  la base de datos y el fichero en disco.
                if (!root.existsFile(currentRelativePathWithName)) {
                    FileInputStream fis = new FileInputStream(currentElement);
                    Blob fileContent = Hibernate
                            .getLobCreator(hibernateUtil.getSession())
                            .createBlob(fis, currentElement.length());
                    com.ymourino.ad05.persistence.models.File file
                            = new com.ymourino.ad05.persistence.models.File(
                                    currentElement.getName(),
                                    currentElement.length(),
                                    fileContent);

                    root.addFile(currentRelativePathWithName, file);
                }
            } else {
                throw new Exception("El elemento '"
                        + currentElement.toString()
                        + "' no es un directorio ni tampoco un fichero.");
            }
        }

        hibernateUtil.saveElement(root);
        return root;
    }

    /**
     * Restaura directorios y ficheros que estén en la base de datos pero no en
     * la ruta indicada.
     *
     * @param path Ruta donde hacer la restauración.
     * @throws Exception
     */
    public static void restoreDirectoryFromDB(String path) throws Exception {
        IJPAUtil hibernateUtil = HibernateUtil.getHibernateUtil();

        if (hibernateUtil != null) {
            // Se normaliza la ruta que se le ha indicado al método.
            Path normalizedRootPath = Paths.get(path).toAbsolutePath().normalize();

            // Se obtiene el directorio raíz de la base de datos.
            Directory root = hibernateUtil.getElement(
                    "from Directory where parent_id is null",
                    Directory.class);

            if (root != null) {
                restoreDirectoryFromDB(root, normalizedRootPath);
            }
        }
    }

    /**
     * Restaura directorios y ficheros que estén en el objeto Directory indicado
     * pero no en la ruta indicada.
     *
     * @param directory
     * @param rootPath
     * @throws Exception
     */
    private static void restoreDirectoryFromDB(Directory directory, Path rootPath) throws Exception {
        IJPAUtil hibernateUtil = HibernateUtil.getHibernateUtil();

        if (!directory.getDirectories().isEmpty()) {
            directory.getDirectories().entrySet().forEach((d) -> {
                Directory currentDirectory = d.getValue();

                try {
                    File file = new File(rootPath.toString() + currentDirectory.getPathWithName());
                    file.mkdirs();

                    restoreDirectoryFromDB(d.getValue(), rootPath);
                } catch (Exception ex) {
                    Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

        if (!directory.getFiles().isEmpty()) {
            Session session = hibernateUtil.getSession();
            session.beginTransaction();

            directory.getFiles().entrySet().forEach((f) -> {
                restoreFileFromDB(f.getValue(), rootPath);
            });

            session.getTransaction().commit();
        }
    }

    /**
     * Restaura un fichero desde la base de datos hasta la ruta indicada.
     *
     * @param dbFile
     * @param rootPath
     */
    public static void restoreFileFromDB(com.ymourino.ad05.persistence.models.File dbFile, Path rootPath) {
        if (!Files.exists(Paths.get(rootPath.toString() + dbFile.getPathWithName()))) {
            try {
                File file = new File(rootPath.toString() + dbFile.getPath());
                file.mkdirs();

                Blob content = dbFile.getContent();
                byte[] blobData = content.getBytes(1, (int) dbFile.getSize());
                FileOutputStream fos = new FileOutputStream(rootPath.toString() + dbFile.getPathWithName());
                fos.write(blobData);
                fos.close();
            } catch (Exception ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
