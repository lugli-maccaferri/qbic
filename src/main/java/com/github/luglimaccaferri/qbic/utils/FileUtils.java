package com.github.luglimaccaferri.qbic.utils;

import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.JSONResponse;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import org.apache.tika.Tika;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class FileUtils {

    public static String[] PRINTABLE_MIMETYPES = { "application/json", "text/plain", "application/x-yaml", "text/yaml", "text/x-log"};

    public static ArrayList<String> readAllLines(File file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
        ArrayList<String> lines = new ArrayList<String>();
        String current_line;

        while((current_line = reader.readLine()) != null){
            lines.add(current_line); // ottimo per i file molto lunghi
        }

        return lines;

    }

    public static boolean isValidPath(String supposed_path, String main_path) throws IOException {
        File f = Path.of(supposed_path).toFile();
        return f.getCanonicalPath().contains(main_path);
    }

    public static JSONResponse handleResource(File resource) throws IOException {

        if(resource == null) return HTTPError.NOT_FOUND;

        if(resource.isDirectory()){

            File[] files = resource.listFiles();
            if(files == null) return new Ok().put("is_directory", true)
                    .put("content", new String[]{});

            String[] str_files = Arrays.stream(files).map(File::getName).toArray(size -> new String[files.length]);

            return new Ok().put("is_directory", true)
                    .put("content", str_files);

        }

        String mime_type = new Tika().detect(resource);
        if(!Arrays.asList(PRINTABLE_MIMETYPES).contains(mime_type)) return new Ok().put("is_directory", false).put("content", "non-printable content");

        ArrayList<String> file_lines = FileUtils.readAllLines(resource);

        return new Ok().put("is_directory", false)
                .put("content", String.join("\n", file_lines));

    }

}
