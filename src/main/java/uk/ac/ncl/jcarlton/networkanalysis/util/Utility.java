package uk.ac.ncl.jcarlton.networkanalysis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Utility class that provides handy methods that
 * can/could be used across the scope of the
 * project.
 *
 * @author Jonathan Carlton
 * @version 1.0
 */
public class Utility {

    public Utility(){}

    /**
     * @param fileName
     * @param arrSize
     * @return
     */
    public String[] getTokens(String fileName, int arrSize) {
        String[] result = new String[arrSize];

        File file = new File(getClass().getResource("/access-codes/" + fileName).getFile());

        int i = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result[i] = line;
                i++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     *
     * @param str
     * @return
     */
    public boolean isWhitespace(String str) {
        if (str == null) return false;

        for (int i = 0; i < str.length(); i++) {
            if ((!Character.isWhitespace(str.charAt(i))))
                return false;
        }
        return true;
    }
}
