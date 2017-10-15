package io.arusland.storage.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @author Ruslan Absalyamov
 * @since 2017-03-09
 * <p>
 * From http://www.mkyong.com/java/how-to-compress-files-in-zip-format/
 */
public class ZipUtil {
    private static final Logger log = Logger.getLogger(ZipUtil.class);
    private final List<String> fileList;
    private final String sourceFullpath;
    private final File sourceDir;

    ZipUtil(File dir) {
        this.fileList = new ArrayList<String>();
        this.sourceDir = dir.isDirectory() ? dir : dir.getParentFile();
        try {
            this.sourceFullpath = this.sourceDir.getCanonicalPath();
            log.info("sourceFullpath: " + sourceFullpath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void zipDir(List<File> files, File destinationZipFile) {
        log.info("files: " + files);
        File dir = files.size() == 1 ? files.get(0) : files.get(0).getParentFile();
        ZipUtil appZip = new ZipUtil(dir);
        appZip.generateFileList(files);
        appZip.zipIt(destinationZipFile);
    }

    /**
     * Zip it
     *
     * @param zipFile output ZIP file location
     */
    public void zipIt(File zipFile) {
        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            log.info("Output to Zip : " + zipFile);

            for (String file : this.fileList) {
                String fromFullPath = sourceFullpath + File.separator + file;
                log.info(String.format("File Added '%s' from '%s'", file, fromFullPath));
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);

                try (FileInputStream in = new FileInputStream(fromFullPath)) {
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
            }

            zos.closeEntry();
            //remember close it
            zos.close();

            log.info("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     *
     * @param node file or directory
     */
    public void generateFileList(File node) {
        log.info("node: " + node);

        //add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            log.info("node children: " + node);

            if (subNote != null) {
                log.info("node children: " + subNote.length);

                for (String filename : subNote) {
                    generateFileList(new File(node, filename));
                }
            }
        }
    }

    public void generateFileList(List<File> files) {
        for (File file : files) {
            generateFileList(file);
        }
    }

    /**
     * Format the file path for zip
     *
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file) {
        return file.substring(sourceFullpath.length() + 1, file.length());
    }
}
