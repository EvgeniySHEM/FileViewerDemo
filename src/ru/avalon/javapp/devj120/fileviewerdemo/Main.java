package ru.avalon.javapp.devj120.fileviewerdemo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Main extends JFrame {

    private final JList list;
    private final JTextArea content;

    private File[] children;

    public Main() {
        list = new JList();
        list.addListSelectionListener(e -> listSelectionChanged());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    int ndx = list.getSelectedIndex();
                    if(ndx >= 0 && children[ndx].isDirectory()) {
                        goToDir(children[ndx].getAbsolutePath());
                    }
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int ndx = list.getSelectedIndex();
                    if(ndx >= 0 && children[ndx].isDirectory()) {
                        goToDir(children[ndx].getAbsolutePath());
                    }
                }
            }
        });

        content = new JTextArea();
        content.setEnabled(false);
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(list),
                new JScrollPane(content));
        add(sp, BorderLayout.CENTER);

        setSize (600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        goToDir(System.getProperty("user.dir"));
    }

    private void goToDir(String path) {
        File dir = new File(path);
        File[] a = dir.listFiles();
        if(a == null) {
            content.setText("Error reading directory");
            return;
        }
        setTitle(dir.toString());
        children = a;
        Arrays.sort(children, Main::compareFiles);

        File parent = dir.getParentFile();
        if(parent != null) {
            File[] ch = new File[children.length + 1];
            ch[0] = parent;
            System.arraycopy(children, 0, ch, 1, children.length);
            children = ch;
        }
        String[] names = new String[children.length];
        for(int i = 0; i < names.length; i++) {
            names[i] = children[i].getName();
        }
        if(parent != null) {
            names[0] = "...";
        }
        list.setListData(names);
    }

    private void listSelectionChanged() {
        int ndx = list.getSelectedIndex();
        if(ndx == -1)
            return;
        if(children[ndx].isDirectory()) {
            content.setText("");
            return;
        }
        try(FileReader r = new FileReader(children[ndx])) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4_096];
            int n;
            while ((n = r.read(buf)) >= 0) {
                sb.append(buf, 0, n);
            }
            content.setText(sb.toString());
            content.setCaretPosition(0);
        } catch (IOException e) {
            content.setText("Error reading file" + e.getMessage() + ".");
        }
    }

    private static int compareFiles(File f1, File f2) {
        if(f1.isDirectory() && f2.isFile())
            return -1;
        if(f1.isFile() && f2.isDirectory())
            return 1;
        return f1.getName().compareTo(f2.getName());
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }
}
