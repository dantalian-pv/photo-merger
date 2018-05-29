# photo-merger
Merge multiple folders into one with files deduplication function based on CRC calculations

## Preconditions

This application generally was created to copy/move photographies placed in different folders into one folder avoiding duplications. But the application can also be used for any types of files.
Assume the situation when you copy photographies from your mobile phone into different folders, drives or devices multiple times just to free the space on your phone. And now you have multiple copies of one file in different places. After a while you decided to re-structure and rename some of this photos in one the folders.
But after some time you want to get rid of this multiple copies and finally make one well structured catalog.
Exactly for this purpose this application was created. The application detects duplications even if a file was renamed or has a different path.

## Synopsis

The application has the next workflow:

* Calculate CRC (crc64) for all files which are going to be merged into one "target" folder;
* Store this data into temporary file because metadata about all files, possibly, will not fit into RAM;
* Sort metadata based on CRC;
* Search for CRC duplications in the list and copy/move only one file of all dups;

For preventing any data loss the application only moves/copies files, but never deletes or rewrites them.

## Usage

### Requirements

To run the application `jvm` (`java`, `jre`) version 8.0 is needed

### Running

Run the application using:

`$> java -jar photo-merger.jar`

Run the application storing output into the log file:

`$> java -jar photo-merger.jar > out.log`

### User interface

The application contains:

* "Copy/Move files" checkbox - if selected, copy files to target folder, otherwise - move files
* "Keep path" - if checked - copy/move files keeping original folder structure, otherwise - copy only file without subfolders. (See details below)
* "+" - Add source folders (can be multiple) to the list
* "Start" - Select the "target" folder and start the process

### "Keep path" explanation

When "Keep path" checkbox is selected the application will copy/move files keeping subfolder structure.

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