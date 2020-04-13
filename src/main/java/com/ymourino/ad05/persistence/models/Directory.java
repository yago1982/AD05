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
package com.ymourino.ad05.persistence.models;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Clase utilizada para representar los directorios.
 *
 * @author Yago Mouriño Mendaña
 */
@Entity
@Table(name = "directories",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"parent_id", "name"}
        ))
public class Directory implements IDirectoryEntry, Serializable {

    private static final long serialVersionUID = 1323880921025831147L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "parent_id")
    private Directory parent;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent")
    @MapKey(name = "name")
    @OrderBy("name")
    private Map<String, Directory> directories = new TreeMap<>();

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent")
    @MapKey(name = "name")
    @OrderBy("name")
    private Map<String, File> files = new TreeMap<>();

    @NotNull
    private String name;

    public Directory() {
    }

    public Directory(String name) {
        this(null, name);
    }

    public Directory(Directory parent, String name) {
        this.name = name;
        setParent(parent);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Directory getParent() {
        return parent;
    }

    @Override
    public void setParent(Directory parent) {
        this.parent = parent;

        if (this.parent != null) {
            this.parent.getDirectories().put(this.getName(), this);
        }
    }

    public Map<String, Directory> getDirectories() {
        return directories;
    }

    public void setDirectories(Map<String, Directory> directories) {
        this.directories = directories;
    }

    public Map<String, File> getFiles() {
        return files;
    }

    public void setFiles(Map<String, File> files) {
        this.files = files;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getSize() {
        return directories.size() + files.size();
    }

    /**
     * Se obtiene la ruta completa del directorio actual (sin su nombre).
     *
     * @return La ruta del directorio.
     */
    @Override
    public String getPath() {
        if (parent != null) {

            String parentPath = parent.getPathWithName();

            if (parentPath.equals(java.io.File.separator)) {
                return parentPath;
            } else {
                return parentPath + java.io.File.separator;
            }

        } else {
            return "";
        }
    }

    /**
     * Se obtiene la ruta completa del directorio actual (con su nombre).
     *
     * @return La ruta del directorio (con su nombre).
     */
    @Override
    public String getPathWithName() {
        return getPath() + getName();
    }

    /**
     * Dado un fichero, lo añade al directorio actual usando la ruta relativa
     * proporcionada.
     *
     * @param relativePath Ruta relativa al directorio actual.
     * @param file Fichero a añadir.
     */
    public void addFile(String relativePath, IFile file) {
        String[] foldersInPath = relativePath.split(java.io.File.separator);
        String firstFolder;
        String remainingPath;

        if (foldersInPath.length > 1) {
            firstFolder = foldersInPath[0];
            remainingPath = relativePath.substring(firstFolder.length() + 1);

            Directory directory;

            if (directories.containsKey(firstFolder)) {
                directory = directories.get(firstFolder);
            } else {
                directory = new Directory(this, firstFolder);
                directories.put(firstFolder, directory);
            }

            directory.addFile(remainingPath, file);
        } else {
            if (!files.containsKey(file.getName())) {
                file.setParent(this);
            }
        }
    }

    /**
     * Comprueba si existe un fichero en el directorio actual dada la ruta
     * relativa del fichero (con el nombre incluido).
     *
     * @param relativePath Ruta relativa del fichero con nombre incluido.
     * @return True si el fichero existe, false en caso contrario.
     */
    public boolean existsFile(String relativePath) {
        return existsFile(relativePath, this);
    }

    /**
     * Comprueba si existe un fichero en un directorio.
     *
     * @param relativePath Ruta relativa del fichero con nombre incluido.
     * @param directory Directorio donde se debe comprobar la existencia del
     * fichero.
     * @return True si el fichero existe, false en caso contrario.
     */
    private boolean existsFile(String relativePath, Directory directory) {
        String[] foldersInPath = relativePath.split(java.io.File.separator);

        if (foldersInPath.length > 1) {
            if (directory.getDirectories().containsKey(foldersInPath[0])) {
                return existsFile(
                        relativePath.substring(foldersInPath[0].length()
                                + java.io.File.separator.length()),
                        directory.getDirectories().get(foldersInPath[0]));
            } else {
                return false;
            }
        } else {
            return directory.getFiles().containsKey(foldersInPath[0]);
        }
    }

    /**
     * Dado un directorio, lo añade al directorio actual usando la ruta relativa
     * proporcionada.
     *
     * @param relativePath Ruta relativa al directorio actual.
     * @param directory Directorio a añadir.
     */
    public void addDirectory(String relativePath, IDirectoryEntry directory) {
        String[] foldersInPath = relativePath.split(java.io.File.separator);
        String firstFolder;
        String remainingPath;

        if (foldersInPath.length > 1) {
            firstFolder = foldersInPath[0];
            remainingPath = relativePath.substring(firstFolder.length() + 1);

            Directory newDirectory;

            if (directories.containsKey(firstFolder)) {
                newDirectory = directories.get(firstFolder);
            } else {
                newDirectory = new Directory(this, firstFolder);
                directories.put(firstFolder, newDirectory);
            }

            newDirectory.addDirectory(remainingPath, directory);
        } else {
            if (!directories.containsKey(directory.getName())) {
                directory.setParent(this);
            }
        }
    }

    /**
     * Comprueba si existe un directorio en el directorio actual dada la ruta
     * relativa del directorio a comprobar (con el nombre incluido).
     *
     * @param relativePath Ruta relativa del directorio con nombre incluido.
     * @return True si el directorio existe, false en caso contrario.
     */
    public boolean existsDirectory(String relativePath) {
        return existsDirectory(relativePath, this);
    }

    /**
     * Comprueba si existe un directorio en otro directorio.
     *
     * @param relativePath Ruta relativa del directorio con nombre incluido.
     * @param directory Directorio donde se debe comprobar la existencia del
     * directorio indicado en la ruta.
     * @return True si el directorio existe, false en caso contrario.
     */
    private boolean existsDirectory(String relativePath, Directory directory) {
        String[] foldersInPath = relativePath.split(java.io.File.separator);

        if (foldersInPath.length > 1) {
            if (directory.getDirectories().containsKey(foldersInPath[0])) {
                return existsDirectory(
                        relativePath.substring(foldersInPath[0].length()
                                + java.io.File.separator.length()),
                        directory.getDirectories().get(foldersInPath[0]));
            } else {
                return false;
            }
        } else {
            return directory.getFiles().containsKey(foldersInPath[0]);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
