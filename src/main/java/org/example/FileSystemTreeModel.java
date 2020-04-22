package org.example;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created on 19.04.2020.
 *
 *
 *
 * @author Sergey Radchenko
 */
public class FileSystemTreeModel implements TreeModel {

    private final File rootFolder;

    public FileSystemTreeModel(File rootFolder) {
        this.rootFolder = rootFolder;
    }

    public Object getRoot() {
        return rootFolder;
    }

    public Object getChild(Object parent, int index) {
        if (parent == null) {
            return null;
        } else {
            File directory = (File) parent;
            return new NameableTreeFile(directory, Objects.requireNonNull(directory.list())[index]);
        }
    }

    public int getChildCount(Object parent) {
        if (parent == null) {
            return 0;
        } else {
            File parentFile = (File) parent;
            return parentFile.listFiles() != null ? parentFile.listFiles().length : 0;
        }
    }

    public boolean isLeaf(Object node) {
        return ((File) node).isFile();
    }

    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    public int getIndexOfChild(Object parent, Object child) {
        List<File> fileList = Arrays.asList(((File) parent).listFiles());
        return fileList.indexOf(child);
    }

    public void addTreeModelListener(TreeModelListener l) {

    }

    public void removeTreeModelListener(TreeModelListener l) {

    }
}
