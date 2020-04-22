package org.example;

import java.io.File;

/**
 * Created on 19.04.2020.
 *
 * @author Sergey Radchenko
 */
public class NameableTreeFile extends File {

    public NameableTreeFile(File parent, String child) {
        super(parent, child);
    }

    @Override
    public String toString() {
        return getName();
    }
}
