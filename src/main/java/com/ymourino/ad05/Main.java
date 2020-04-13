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
package com.ymourino.ad05;

import com.ymourino.ad05.persistence.DBHelper;
import com.ymourino.ad05.persistence.HibernateUtil;
import com.ymourino.ad05.persistence.IJPAUtil;
import com.ymourino.ad05.persistence.models.Directory;
import com.ymourino.ad05.utils.Config;
import com.ymourino.ad05.utils.ConfigBuilder;
import com.ymourino.ad05.utils.threads.ListenNotifications;
import com.ymourino.ad05.utils.threads.WatchChanges;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Yago Mouriño Mendaña
 */
public class Main extends javax.swing.JFrame {

    private static final long serialVersionUID = -1224279262403702125L;

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();

        // Actualizamos el directorio local con la información de la base de
        // datos, y la base de datos con la información del directorio local.
        try {
            ConfigBuilder config = ConfigBuilder.getConfig();
            DBHelper.restoreDirectoryFromDB(config.getApp().getDirectory());
            Directory root = DBHelper.addDirectoryToDB(config.getApp().getDirectory());

            // TODO: la información de directorios y ficheros en la interfaz no
            //  se actualiza adecuadamente cuando los hilos añaden o recuperan
            //  información desde la base de datos.
            WatchChanges watcher = new WatchChanges(config.getApp().getDirectory());
            watcher.start();

            ListenNotifications listener = new ListenNotifications(config.getApp().getDirectory());
            listener.start();

            DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("root");
            populateTree(root, treeRoot);
            treeFolders.setModel(new DefaultTreeModel(treeRoot));

            DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) treeFolders.getCellRenderer();
            renderer.setLeafIcon(renderer.getClosedIcon());

            treeFolders.addTreeSelectionListener((TreeSelectionEvent tse) -> {
                populateTable();
            });

            if (treeFolders.getRowCount() > 0) {
                treeFolders.expandRow(0);
            }
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

            try {
                IJPAUtil hibernateUtil = HibernateUtil.getHibernateUtil();
                hibernateUtil.close();
            } catch (Exception ex2) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
    }

    /**
     * Dado un objeto Directory extrae su información para crear el árbol donde
     * se mostrarán las carpetas existentes en la base de datos.
     *
     * @param directory
     * @param node
     */
    private void populateTree(Directory directory, DefaultMutableTreeNode node) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(directory);
        node.add(newNode);

        if (!directory.getDirectories().isEmpty()) {
            for (Map.Entry<String, Directory> d : directory.getDirectories().entrySet()) {
                populateTree(d.getValue(), newNode);
            }
        }
    }

    /**
     * Se obtiene la carpeta seleccionada en el árbol y se muestran en la tabla
     * los ficheros que contiene.
     */
    private void populateTable() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeFolders.getLastSelectedPathComponent();

        if (node != null) {
            Object nodeContent = node.getUserObject();

            if (nodeContent instanceof Directory) {
                Map<String, com.ymourino.ad05.persistence.models.File> files = ((Directory) nodeContent).getFiles();

                DefaultTableModel tableModel = (DefaultTableModel) tableFiles.getModel();
                tableModel.setRowCount(0);

                if (!files.isEmpty()) {
                    files.entrySet().forEach((file) -> {
                        tableModel.addRow(new Object[]{file.getValue().getName(), file.getValue().getSize()});
                    });
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitMain = new javax.swing.JSplitPane();
        scrollFolders = new javax.swing.JScrollPane();
        treeFolders = new javax.swing.JTree();
        scrollFiles = new javax.swing.JScrollPane();
        tableFiles = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MiniDrive");
        setMinimumSize(new java.awt.Dimension(900, 600));

        splitMain.setDividerLocation(300);

        treeFolders.setRootVisible(false);
        scrollFolders.setViewportView(treeFolders);

        splitMain.setLeftComponent(scrollFolders);

        tableFiles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Fichero", "Tamaño en bytes"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollFiles.setViewportView(tableFiles);

        splitMain.setRightComponent(scrollFiles);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitMain, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitMain, javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        try {
            // Generar una marca de tiempo para el fichero donde se registrarán los
            // errores y mensajes de Hibernate.
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter timestampFormatter
                    = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String timestampString = timestampFormatter.format(now);

            // Se redirigen System.err y System.out hacia un fichero. De esta
            // forma evitamos perder los mensajes de Hibernate y los posibles
            // errores que se puedan producir.
            File file = new File("log_" + timestampString + ".log");
            FileOutputStream fos = new FileOutputStream(file, true);
            PrintStream ps = new PrintStream(fos);
            System.setErr(ps);
            System.setOut(ps);

            // Se comprueba si el directorio indicado en la configuración del
            // programa existe, y en caso negativo se genera una excepción para
            // informar al usuario y finalizar el programa.
            ConfigBuilder config = ConfigBuilder.getConfig();

            if (Files.exists(Paths.get(config.getApp().getDirectory()))) {
                createTriggerAndFunction();
            } else {
                throw new FileNotFoundException("El directorio '"
                        + config.getApp().getDirectory()
                        + "' no existe.");
            }

            /* Create and display the form */
            java.awt.EventQueue.invokeLater(() -> {
                Main main = new Main();
                main.setLocationRelativeTo(null);
                main.setVisible(true);
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);

            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

            // Se cierra la conexión con la base de datos por si estaba abierta.
            IJPAUtil hibernateUtil = HibernateUtil.getHibernateUtil();
            hibernateUtil.close();
        }
    }

    /**
     * Se crea en la base de datos el trigger y la función utilizados para
     * notificar la creación de nuevos ficheros.
     *
     * @throws Exception
     */
    private static void createTriggerAndFunction() throws Exception {
        // Obteniendo una instancia de HibernateUtil se fuerza la creación
        // de la base de datos si no existiese.
        IJPAUtil hibernateUtil = HibernateUtil.getHibernateUtil();

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

        CallableStatement createFunction = conn.prepareCall(
                "CREATE OR REPLACE FUNCTION notificar_fichero() "
                + "RETURNS trigger AS $$ "
                + "BEGIN "
                + "PERFORM pg_notify('nuevofichero',NEW.id::text); "
                + "RETURN NEW; "
                + "END; "
                + "$$ LANGUAGE plpgsql;");
        createFunction.execute();
        createFunction.close();

        CallableStatement createTrigger = conn.prepareCall(
                "DROP TRIGGER IF EXISTS notif_nuevo_fichero ON files; "
                + "CREATE TRIGGER notif_nuevo_fichero "
                + "AFTER INSERT "
                + "ON files "
                + "FOR EACH ROW "
                + "EXECUTE PROCEDURE notificar_fichero(); ");
        createTrigger.execute();
        createTrigger.close();

        conn.close(); // ¿?
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollFiles;
    private javax.swing.JScrollPane scrollFolders;
    private javax.swing.JSplitPane splitMain;
    private javax.swing.JTable tableFiles;
    private javax.swing.JTree treeFolders;
    // End of variables declaration//GEN-END:variables
}
