package org.example;


import javax.swing.*;

/**
 * Created on 19.04.2020.
 *
 * @author Sergey Radchenko
 */
public class Application {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException{
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtilities.invokeLater(MainForm::new);
    }
}
