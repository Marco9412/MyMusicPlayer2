package com.panni.mymusicplayer2.youtubedl;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RootFsExtractor {

    final private static String SOURCE_FILE_NAME = "armrootfs.zip";

    public boolean extract(Context context) {
        try {
            ZipInputStream inputStream = new ZipInputStream(context.getAssets().open("Files/" + SOURCE_FILE_NAME));
            while (true) {
                ZipEntry entry = inputStream.getNextEntry();
                if (entry == null) {
                    break;
                }

                File targetFile = new File(context.getFilesDir(), entry.getName());
                if (entry.isDirectory()) {
                    if (!targetFile.mkdirs()) {
                        throw new RuntimeException("Failed to create directory " + targetFile.getAbsolutePath());
                    }
                    Os.chmod(targetFile.getAbsolutePath(), 0b111111101);
                } else {
                    BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                    byte buf[] = new byte[4096];
                    while (true) {
                        int c = inputStream.read(buf, 0, buf.length);
                        if (c <= 0) {
                            break;
                        }
                        outStream.write(buf, 0, c);
                    }
                    outStream.close();
                    if (entry.getName().contains("bin/") || entry.getName().contains("libexec") || entry.getName().contains("lib/apt/methods")) {
                        Os.chmod(targetFile.getAbsolutePath(), 0b111111101);  // rwxrwxr-x
                    }
                }
            }

            return true;
        } catch (IOException ex) {

        } catch (ErrnoException ex) {

        }

        return false;
    }
}
