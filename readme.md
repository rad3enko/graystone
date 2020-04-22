# Graystone log observer
**Graystone** is Apache Lucene powered file browser implementing search by text docs function, filtering by file mask, inclusions navigation.

## Features
- Parallel documents indexing process without locking UI
- Phrase search by files in the selected directory (including subdirectories)
- Wildcard search in file names
- Multi-tab browsing over documents
- Highlighting search query inclusions in text

### To implement:

- Add logic for navigation buttons
- Close tabs feature
- Partial (chunk) loading text file (1 Gb text and more)

## Screenshot
![github-small](https://user-images.githubusercontent.com/10897930/79989701-d4f69480-84b8-11ea-8c75-b46873b35946.png)

- Search: Apache Lucene
- UI: Java Swing