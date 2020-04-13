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
import java.sql.Blob;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Clase utilizada para representar los ficheros.
 *
 * @author Yago Mouriño Mendaña
 */
@Entity
@Table(name = "files",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"parent_id", "name"}
        ))
public class File implements IFile, Serializable {

    private static final long serialVersionUID = -9127316768330902475L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "parent_id")
    private Directory parent;

    @NotNull
    private String name;

    @NotNull
    private long size;

    @NotNull
    private Blob content;

    public File() {
    }

    public File(String name, long size, Blob content) {
        this(null, name, size, content);
    }

    public File(Directory parent, String name, long size, Blob content) {
        this.name = name;
        this.size = size;
        this.content = content;
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
            this.parent.getFiles().put(this.getName(), this);
        }
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
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Blob getContent() {
        return content;
    }

    public void setContent(Blob content) {
        this.content = content;
    }

    /**
     * Se obtiene la ruta completa del fichero actual (sin su nombre).
     *
     * @return La ruta del fichero.
     */
    @Override
    public String getPath() {
        if (parent.getPathWithName().equals(java.io.File.separator)) {
            return parent.getPathWithName();
        } else {
            return parent.getPathWithName() + java.io.File.separator;
        }
    }

    /**
     * Se obtiene la ruta completa del fichero actual (con su nombre).
     *
     * @return La ruta del fichero (con su nombre).
     */
    @Override
    public String getPathWithName() {
        return getPath() + getName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
