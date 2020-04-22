package org.example;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 19.04.2020.
 *
 * @author Sergey Radchenko
 */
public class FilteredFileSystemTreeModel extends FileSystemTreeModel {

    private final Collection<String> fileSet;

    public FilteredFileSystemTreeModel(File rootFolder, Collection<String> fileSet) {
        super(rootFolder);
        this.fileSet = fileSet;
    }

    @Override
    public Object getRoot() {
        return super.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        File parentFile = (File) parent;
        List<File> currentDirFiles = Arrays.asList(Objects.requireNonNull(parentFile.listFiles()));

        currentDirFiles = currentDirFiles.stream()
                                         .filter(file -> fileSet.contains(file.getAbsolutePath())
                                                         || fileSet.stream()
                                                                   .anyMatch(filename ->
                                                                                     filename.contains(file.getAbsolutePath())))
                                         .collect(Collectors.toList());
        return new NameableTreeFile(parentFile, Objects.requireNonNull(currentDirFiles.get(index)).getName());
    }

    @Override
    public int getChildCount(Object parent) {
        File parentFile = (File) parent;
        List<File> currentDirFiles = Arrays.asList(Objects.requireNonNull(parentFile.listFiles()));

        currentDirFiles = currentDirFiles.stream()
                                         .filter(file -> fileSet.contains(file.getAbsolutePath())
                                                         || fileSet.stream()
                                                                   .anyMatch(filename ->
                                                                                     filename.contains(file.getAbsolutePath())))
                                         .collect(Collectors.toList());
        return currentDirFiles.size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return super.isLeaf(node);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        super.valueForPathChanged(path, newValue);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return super.getIndexOfChild(parent, child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        super.addTreeModelListener(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        super.removeTreeModelListener(l);
    }
}
