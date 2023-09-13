package org.jboss.weld.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.jboss.weld.test.util.Utils;

/**
 * This class generates file with TestClassToHashMapper#OUTPUT_FILE_NAME file name in TestClassToHashMapper#TARGET_DIR
 * directory.
 * Generated file contains fully qualified name of each test class and its related hash. Class is executed using
 * exec-maven-plugin during
 * install maven phase.
 */
public class TestClassToHashMapper {

    private TestClassToHashMapper() {
    }

    public static final String TEST_SUFFIX = "Test.java";
    public static final String OUTPUT_FILE_NAME = "test-classes-with-hash.txt";
    public static final String TARGET_DIR = "target";
    public static final String PREFIX = "org";
    public static final String SUFFIX = ".java";

    public static void main(String[] args) {
        File userDir = new File(System.getProperty("user.dir"));
        File outputFile = new File(userDir + File.separator + TARGET_DIR + File.separator + OUTPUT_FILE_NAME);
        try {
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            FileWriter writer = new FileWriter(outputFile);
            List<File> files = (List<File>) FileUtils.listFiles(userDir, new TestFileFilter(), new DirFileFilter());
            for (File file : files) {
                String fqcn = file.getPath().substring(file.getPath().indexOf(PREFIX), file.getPath().indexOf(SUFFIX));
                fqcn = fqcn.replace(File.separator, ".");
                writer.append(fqcn + " " + Utils.getHashOfTestClass(fqcn));
                writer.append(System.lineSeparator());
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
        }

    }

    static class TestFileFilter implements IOFileFilter {
        @Override
        public boolean accept(File file) {
            return file.getName().endsWith(TEST_SUFFIX);
        }

        @Override
        public boolean accept(File file, String s) {
            return file.getName().endsWith(TEST_SUFFIX);
        }
    }

    static class DirFileFilter implements IOFileFilter {
        @Override
        public boolean accept(File file) {
            return true;
        }

        @Override
        public boolean accept(File file, String s) {
            return true;
        }
    }
}
