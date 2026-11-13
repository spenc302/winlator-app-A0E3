package com.winlator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import com.winlator.container.Container;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/** Copies a user-selected game directory into a Winlator container and identifies x86-64 PE executables. */
public final class GameImportManager {
    private static final int MAX_DEPTH = 32;
    private static final String[] COLUMNS = {
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE
    };

    public static final class Result {
        public final File installDirectory;
        public final List<File> executables;
        Result(File installDirectory, List<File> executables) {
            this.installDirectory = installDirectory;
            this.executables = executables;
        }
    }

    private GameImportManager() {}

    public static Result importGame(Context context, Uri treeUri, Container container) throws Exception {
        String rootId = DocumentsContract.getTreeDocumentId(treeUri);
        if (container == null || container.getUserDir() == null) throw new IllegalStateException("The selected Winlator container is unavailable.");

        File target = new File(container.getUserDir(), "Documents/A0E3/Age of Empires III Definitive Edition");
        if (target.exists()) throw new IllegalStateException("An imported game folder already exists in this container. Remove it before importing again.");
        if (!target.mkdirs()) throw new IllegalStateException("Unable to create the destination folder in the selected container.");

        try {
            copyDocumentTree(context, treeUri, rootId, target, 0);
            List<File> candidates = new ArrayList<>();
            findX64Executables(target, candidates);
            if (candidates.isEmpty()) throw new IllegalStateException("The selected folder was copied, but no 64-bit Windows executable was found. Select the actual game installation folder.");
            return new Result(target, candidates);
        }
        catch (Exception e) {
            deleteRecursively(target);
            throw e;
        }
    }

    private static void copyDocumentTree(Context context, Uri treeUri, String parentId, File destination, int depth) throws Exception {
        if (depth > MAX_DEPTH) throw new IllegalStateException("The selected folder is nested too deeply to import safely.");
        ContentResolver resolver = context.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId);
        try (Cursor cursor = resolver.query(childrenUri, COLUMNS, null, null, null)) {
            if (cursor == null) throw new IllegalStateException("The selected storage provider did not return folder contents.");
            while (cursor.moveToNext()) {
                String documentId = cursor.getString(0);
                String name = safeName(cursor.getString(1));
                String mime = cursor.getString(2);
                if (name == null || name.equals(".") || name.equals("..")) continue;
                File output = new File(destination, name);
                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                    if (!output.mkdirs() && !output.isDirectory()) throw new IllegalStateException("Unable to create folder: " + name);
                    copyDocumentTree(context, treeUri, documentId, output, depth + 1);
                }
                else {
                    Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
                    try (InputStream input = resolver.openInputStream(fileUri);
                         FileOutputStream outputStream = new FileOutputStream(output)) {
                        if (input == null) throw new IllegalStateException("Unable to read file: " + name);
                        byte[] buffer = new byte[1024 * 1024];
                        int count;
                        while ((count = input.read(buffer)) != -1) outputStream.write(buffer, 0, count);
                    }
                }
            }
        }
    }

    private static String safeName(String value) {
        if (value == null) return null;
        String name = value.replace("/", "_").replace("\\", "_").trim();
        return name.isEmpty() ? null : name;
    }

    private static void findX64Executables(File directory, List<File> results) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) findX64Executables(file, results);
            else if (file.isFile() && file.getName().toLowerCase().endsWith(".exe") && isX64PE(file)) results.add(file);
        }
    }

    private static boolean isX64PE(File file) {
        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            if (input.length() < 64 || input.readUnsignedByte() != 'M' || input.readUnsignedByte() != 'Z') return false;
            input.seek(0x3c);
            long peOffset = Integer.toUnsignedLong(Integer.reverseBytes(input.readInt()));
            if (peOffset < 64 || peOffset + 6 > input.length()) return false;
            input.seek(peOffset);
            if (input.readUnsignedByte() != 'P' || input.readUnsignedByte() != 'E' || input.readUnsignedByte() != 0 || input.readUnsignedByte() != 0) return false;
            return Short.reverseBytes(input.readShort()) == (short)0x8664;
        }
        catch (Exception e) { return false; }
    }

    public static void discardImport(Result result) {
        if (result != null && result.installDirectory != null) deleteRecursively(result.installDirectory);
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) deleteRecursively(child);
        file.delete();
    }
}
