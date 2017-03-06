# FileRenamer
FileRenamer is a Java GUI program to rename file names of a directory in a batch fashion. You can use your favorate text editor to rename them.
## 1 How to run
1. From command line shell, type java -jar FileRenamer.jar
2. From GUI shell, double-click FileRenamer.jar icon.

It runs in GUI mode.

## 2 GUI main window
 Element  | Description
----------|-------------
Directory   | Base directory.  Use the button to pop up a file browser.  You can drop a file icon into the text field, as well.
Inclusion pattern   | Glob file name filter to include.
Exclusion pattern   | Glob file name filter to exclude.
Hidden  | To include hidden files
Original names  | Text area to show original file names
New file names  | Text area to edit file names.  You can copy it to your favorate text editor to edit them and copy back.
Refresh | Refreshes Original names and New file names to the currect contents of the direcotory.
Rename   | Renames file names according to New file names.  Cyclic renaming as well as swapping names are also possible.

