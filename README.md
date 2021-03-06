# photo-merger
Merge multiple folders into one with files deduplication function based on CRC calculations

## Preconditions

This application generally was created to copy/move photographs placed in different folders into one folder avoiding duplications. But the application can also be used for any types of files.
Assume the situation when you copy photographs from your mobile phone into different folders, drives or devices multiple times just to free the space on your phone. And now you have multiple copies of one file in different places. After a while you decided to re-structure and rename some of this photos in one of the folders.
But after some time you want to get rid of this multiple copies and finally make one well structured catalog.
Exactly for this purpose this application was created. The application detects duplications even if a file was renamed or has a different path.

## Synopsis

The application has the next workflow:

* Gather metadata of all files in "source" and "target" folders;
* Store this metadata into a temporary file inside target folder, because metadata of all files, possibly, will not fit into RAM;
* Sort this metadata by file size to simplify files comparison; 
* Calculate CRC (crc64) for files with the same size which are going to be merged into one "target" folder;
* Search for CRC duplications in the list and copy/move only one file of all duplicates (or just move/copy a file to target folder if no duplicates found), or delete duplicates from source folders if "Delete duplicates" option selected;

## Usage

### Requirements

To run the application `jvm` (`java`, `jre`) version 8.0 or higher is needed

### Compiling from source

#### Requirements

* `java` version at least 1.8 https://java.com/ru/download/
* `maven` version at least 3.5 https://maven.apache.org/download.cgi
* `git` - (optional) to download sources

#### Compiling steps

* Download source by one of the ways
  * `$> git clone https://github.com/dantalian-pv/photo-merger.git`
  * Or download using any web-browser from https://github.com/dantalian-pv/photo-merger/archive/master.zip
* Go into `photo-merger` directory
* Run `$> mvn clean package`



### Running

* Extract archive from `cli/target/photo-merger-cli-0.1.1.tar.gz` or `ui/target/photo-merger-ui-0.1.1.tar.gz` to any suitable place
* Go into `bin/` sub-folder 
* Run the application using:
  * `$> ./photo-merger-cli` - for command line version on Linux
  * `$> photo-merger-cli.bat` - for command line version on Windows
  * `$> ./photo-merger-ui` - for UI version on Linux
  * `$> photo-merger-ui.bat` - for UI version on Windows

Log files will be places in `logs/` sub-folder

### User interface

The application contains:

* "Copy files/Move files/Delete duplicates" combo box - copy/move files from source directories to target directory or delete duplicates from source directories
* "Keep path" - if checked - copy/move files keeping original folder structure, otherwise - copy/move only files without subfolders. (See details below)
* "+" - Add source folders (can be multiple) to the list
* "Start" - Select the "target" folder and start the process

### "Keep path" explanation

When "Keep path" checkbox is selected the application will copy/move files keeping subfolder structure. The checkbox is not relevant when "Delete duplicates" is selected.

For example:

* Source folder is: `/tmp/src`
* Which contains one file with path: `/tmp/src/dev/release/index.html`
* And target folder is `/home/user/target`

If the checkbox is **selected** then file will be copied/moved to: `/home/user/target/dev/release/index.html`.
 
So subfolder `dev/release` will be created in the target folder.

But if checkbox is **not selected** the fill will be copied/moved to: `/home/user/target/index.html`.

### What if?

What will happen when the application sees that some file in the target folder with some path/name already exists. Well, the application will compare source and target file's CRCs and if they are different, the application will add a suffix to the source file while copying/moving, so in the end there will be two different files with two different names in target folder.

## Technical details

Workflow:

1. Calculate amount of files in all source folders and target folder (skipping hidden folders and files), and use this number to show adequate progress bar;
2. Pick up a file size for every file and store this metadata into a temporary file (`target/.metadata/.metadata-N`) by batches by M (default: 1000) items in ordered way, based on file size;
3. Merge this batches into one big file keeping items in the ordered way;
4. Read this file and compare neighbor items. If they have the same size - calculate CRC, then copy/move only first one if CRC is the same;

Every step is running in multiple threads (default: 4) to make this tasks in parallel.

### Algorithm complexity

1. N - calculate amount of files
2. N - store all file entries into metadata files sorted by file size
3. N*log(N) - merge metadata files
4. N - move/copy files and calculate CRC for files with the same size

Sum: `3*N + N*log(N)`, or `O(N*log(N))`.

As you can see here, there is one technical optimization not to calculate CRC for all files, but only for files with the same size.