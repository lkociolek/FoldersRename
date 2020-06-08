package dev.kociolek.folderrename;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Simple console tool to rename all folders names in specified location
 * Folders have been exported from macOS Apple Photos
 * Target folder pattern should be YYYY-MM-DD
 *
 * @author Łukasz Kociołek
 * @version 1.0
 * 
 */
public class FoldersRename {

    static Logger logger = Logger.getLogger(FoldersRename.class.getName());

    private static final String SEPARATOR_SPACE = " ";
    private static final String SEPARATOR_COMMA = ",";
    private static final Set<String> newFoldersSet = new HashSet<>();
    private static Map<String, String> monthsMap;
    private static String location;

    static {
        populateMonthsMap();
    }

    private static void populateMonthsMap() {
        monthsMap = new HashMap<>();
        monthsMap.put("stycznia", "01");
        monthsMap.put("lutego", "02");
        monthsMap.put("marca", "03");
        monthsMap.put("kwietnia", "04");
        monthsMap.put("maja", "05");
        monthsMap.put("czerwca", "06");
        monthsMap.put("lipca", "07");
        monthsMap.put("sierpnia", "08");
        monthsMap.put("września", "09");
        monthsMap.put("października", "10");
        monthsMap.put("listopada", "11");
        monthsMap.put("grudnia", "12");
    }

    public static void main(String[] args) {
        location = args[0];
        File[] folders = getFolders();
        for (File folder : folders) {
            changeFolderName(folder);
        }
    }

    private static void changeFolderName(File folder) {
        if (folder.getName().contains(SEPARATOR_SPACE)) {
            String newFolderName = getNewFolderName(folder);
            if (newFoldersSet.contains(newFolderName)) {
                moveContent(folder, newFolderName);
            } else {
                rename(folder, newFolderName);
            }
        }
    }

    private static void moveContent(File folder, String newFolderName) {
        try {
            File newFolder = new File(newFolderName);
            logger.info("Taki katalog istnieje. Kopiuję zawartość z \"" + folder.getPath() + "\" do \"" + newFolder + "\"");
            File[] files = getFiles(folder);
            for (File file : files) {
                moveFile(newFolderName, file);
            }
            logger.info("Kasuje katalog " + folder.getName());
            folder.delete();
        } catch (IOException e) {
            logger.log(Level.WARN, e.getLocalizedMessage());
        }
    }

    private static void moveFile(String newFolderName, File file) throws IOException {
        Path source = Paths.get(file.getPath());
        Path newDir = Paths.get(newFolderName);
        Path target = newDir.resolve(source.getFileName());
        Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void rename(File folder, String newFolderName) {
        File newFolder = new File(newFolderName);
        folder.renameTo(newFolder);
        newFoldersSet.add(newFolderName);
    }

    private static String getNewFolderName(File folder) {
        String oldFolderName = folder.getName();
        if (oldFolderName.contains(SEPARATOR_COMMA)) {
            String[] split = oldFolderName.split(SEPARATOR_COMMA);
            oldFolderName = split[1].trim();
        }
        String[] oldFolder = oldFolderName.split(SEPARATOR_SPACE);
        String day = getDay(oldFolder);
        String month = getMonth(oldFolder);
        String year = getYear(oldFolder);
        String newFolderName = year + "-" + month + "-" + day;
        String newFolderPath = location + "/" + newFolderName;
        logger.info("Stara nazwa folderu to: \"" + oldFolderName + "\";\tNowa nazwa folderu to: \"" + newFolderName + "\"");
        return newFolderPath;
    }

    private static File[] getFolders() {
        File file = new File(location);
        File[] folders = file.listFiles(f -> f.isDirectory() && !f.isHidden());
        if(folders != null) {
            logger.info("Znaleziono " + folders.length + " katalogów");
            return folders;
        } else {
            throw new NullPointerException();
        }
    }

    private static File[] getFiles(File folder) {
        File file = new File(folder.getPath());
        File[] files = file.listFiles(File::isFile);
        if(files != null) {
            logger.info("Znaleziono " + files.length + " plików do przeniesienia");
            return files;
        } else {
            throw new NullPointerException();
        }
    }

    private static String getYear(String[] oldFolder) {
        return oldFolder[2];
    }

    private static String getMonth(String[] oldFolder) {
        return monthsMap.get(oldFolder[1]);
    }

    private static String getDay(String[] oldFolder) {
        return (oldFolder[0].length() == 1) ? ("0" + oldFolder[0]) : oldFolder[0];
    }

}