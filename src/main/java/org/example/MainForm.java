package org.example;

import org.apache.lucene.document.Document;
import org.example.lucene.LuceneConstants;
import org.example.lucene.LuceneIndexer;
import org.example.lucene.LuceneSearcher;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 19.04.2020.
 *
 * @author Sergey Radchenko
 */
public class MainForm extends JFrame {

    private JPanel rootPanel;
    private JButton browseButton;
    private JTree fileTree;
    private JPanel filesystemPanel;
    private JTabbedPane tabbedPane;
    private JTextField queryField;
    private JTextField fileMaskField;
    private JButton nextButton;
    private JButton previousButton;
    private JButton selectAllButton;
    private JPanel navigationPanel;
    private JPanel searchPanel;
    private JScrollPane fileTreeScrollPane;
    private JLabel statusLabel;
    private JPanel mainPanel;
    private JPanel panelTab;
    private JLabel queryLabel;
    private JLabel fileMaskLabel;
    private JSplitPane splitPane;

    private final LuceneIndexer luceneIndexer;
    private final LuceneSearcher luceneSearcher;
    private File currentRoot;

    private HashSet<File> filesInTabSet;
    private JTextComponent activeTextComponent;

    public final String WINDOW_TITLE = "Graystone log observer";

    public MainForm() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Dimension windowPreferredSize = new Dimension(
                (int) (screenSize.getWidth() / 1.5),
                (int) (screenSize.getHeight() / 1.5));

        Dimension windowMinimumSize = new Dimension(
                (int) screenSize.getWidth() / 3,
                (int) screenSize.getHeight() / 3);

        this.add(rootPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setSize(windowPreferredSize);
        this.setMinimumSize(windowMinimumSize);
        this.setLocationRelativeTo(null);

        this.setTitle(WINDOW_TITLE);

        sealFilePanel();
        loadEmptyTree();
        tabbedPane.removeAll();

        setStatus("Browse directory to start indexing");

        loadListeners();

        luceneIndexer = new LuceneIndexer();
        luceneSearcher = new LuceneSearcher();
        filesInTabSet = new HashSet<>();

    }

    private void sealFilePanel() {
        queryField.setEnabled(false);
        fileMaskField.setEnabled(false);
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        selectAllButton.setEnabled(false);
    }

    private void unsealFilePanel() {
        queryField.setEnabled(true);
        fileMaskField.setEnabled(true);
        nextButton.setEnabled(true);
        previousButton.setEnabled(true);
        selectAllButton.setEnabled(true);
    }

    private void loadEmptyTree() {
        fileTree.setVisible(false);
    }

    private void loadTree(TreeModel treeModel) {
        fileTree.setModel(treeModel);
        fileTree.setVisible(true);
    }

    private void setStatus(String text) {
        setStatus(text, null);
    }

    private void setStatus(String text, ImageIcon icon) {
        statusLabel.setText(text);
        statusLabel.setIcon(icon);
    }

    private void indexInBackground(File directory) {
        browseButton.setEnabled(false);
        setStatus("Indexing...", new ImageIcon("src/images/spin16x16.gif"));
        sealFilePanel();

        IndexDirectoryWorker indexDirectoryWorker = new IndexDirectoryWorker(directory);

        indexDirectoryWorker.execute();
    }

    private void updateFileTree() {
        final String queryInput = queryField.getText();
        final String fileMaskInput = fileMaskField.getText();

        if (queryInput.isEmpty() && fileMaskInput.isEmpty()) {
            fileTree.setModel(new FileSystemTreeModel(currentRoot));
        } else {
            SwingWorker<Void, Void> filterTreeWorker = new FilterTreeWorker(queryInput, fileMaskInput);
            filterTreeWorker.execute();
        }
    }

    private List<String> toPathList(Collection<Document> docs) {
        return docs.stream()
                   .map(doc -> doc.getField(LuceneConstants.FILE_PATH).stringValue())
                   .collect(Collectors.toList());
    }

    private void highlightEntries(String s, JTextComponent textComponent, Color color) {
        if(s.isEmpty()) return;

        Highlighter highlighter = textComponent.getHighlighter();
        highlighter.removeAllHighlights();

        s = s.toLowerCase();
        String componentText = textComponent.getText().toLowerCase();
        int componentTextLength = componentText.length();

        for(int i=0; i<componentTextLength-s.length(); i++) {
            String checkString = componentText.substring(i, i+s.length());
            if (s.equals(checkString)) {
                try {
                    highlighter.addHighlight(i, i + s.length(), new DefaultHighlighter.DefaultHighlightPainter(color));
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class IndexDirectoryWorker extends SwingWorker<Void, Void> {

        private final File directory;

        public IndexDirectoryWorker(File directory) {
            this.directory = directory;
        }

        @Override
        protected Void doInBackground() {
            luceneIndexer.createIndex(directory.getAbsolutePath());
            return null;
        }

        @Override
        protected void done() {
            browseButton.setEnabled(true);
            fileTree.setEnabled(true);
            setStatus("Documents indexed: " + luceneSearcher.getNumDocs());
            unsealFilePanel();
        }
    }

    private class FilterTreeWorker extends SwingWorker<Void, Void> {

        private final String searchContent;
        private final String fileMask;
        private List<Document> docsFoundContent;

        public FilterTreeWorker(String searchContent, String fileMask) {
            this.searchContent = searchContent;
            this.fileMask = fileMask;
            docsFoundContent = new ArrayList<>();
        }

        @Override
        protected Void doInBackground() {
            docsFoundContent = luceneSearcher.search(searchContent, fileMask);
            return null;
        }

        @Override
        protected void done() {
            TreeModel treeModel = new FilteredFileSystemTreeModel((File) fileTree.getModel().getRoot(),
                                                                  toPathList(docsFoundContent));
            fileTree.setModel(treeModel);
        }
    }


    /*
        Listeners
    */
    private void loadListeners() {
        browseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!browseButton.isEnabled()) return;

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int option = fileChooser.showOpenDialog(null);
                if (option == JFileChooser.APPROVE_OPTION) {
                    currentRoot = fileChooser.getSelectedFile();
                    loadTree(new FileSystemTreeModel(currentRoot));

                    fileTree.setEnabled(false);
                    indexInBackground(currentRoot);
                }
            }
        });
        queryField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateFileTree();
                // re-highlight text in active tab
                if(tabbedPane.getTabCount()>0) {
                    highlightEntries(queryField.getText(), activeTextComponent, Color.YELLOW);
                }
            }
        });
        fileMaskField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateFileTree();
            }
        });
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                String pathToFile = "";
                for(Object o : event.getNewLeadSelectionPath().getPath()) {
                    pathToFile = pathToFile.concat(File.separator).concat(o.toString());
                }
                pathToFile = pathToFile.substring(1);
                File selectedFile = new File(pathToFile);

                if (selectedFile.isFile()) {
                    if (filesInTabSet.contains(selectedFile)) {
                        for (int index = 0; index < tabbedPane.getTabCount(); index++) {
                            String toolTipText = tabbedPane.getToolTipTextAt(index);
                            if (toolTipText.equals(selectedFile.getAbsolutePath())) {
                                tabbedPane.setSelectedIndex(index);
                                break;
                            }
                        }
                    } else {
                        filesInTabSet.add(selectedFile);
                        try {
                            String content = new String(Files.readAllBytes(Paths.get(pathToFile)));
                            JTextArea textArea = new JTextArea(content);
                            textArea.setWrapStyleWord(true);
                            textArea.setLineWrap(true);

                            highlightEntries(queryField.getText(), textArea, Color.yellow);

                            JScrollPane scrollPane = new JScrollPane(textArea);

                            tabbedPane.addTab(selectedFile.getName(),
                                              scrollPane);
                            tabbedPane.setToolTipTextAt(tabbedPane.getTabCount()-1,
                                                        selectedFile.getAbsolutePath());
                            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);

                            activeTextComponent = textArea;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                activeTextComponent = ((JTextArea)((JScrollPane)tabbedPane.getComponentAt(((JTabbedPane)e.getSource()).getSelectedIndex())).getViewport().getComponent(0));
                highlightEntries(queryField.getText(), activeTextComponent, Color.YELLOW);
            }
        });
    }
}
