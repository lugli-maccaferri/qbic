package com.github.luglimaccaferri.qbic.utils;

import com.github.luglimaccaferri.qbic.Core;
import com.github.luglimaccaferri.qbic.data.models.Server;
import com.github.luglimaccaferri.qbic.http.models.HTTPError;
import com.github.luglimaccaferri.qbic.http.models.JSONResponse;
import com.github.luglimaccaferri.qbic.http.models.Ok;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.apache.tika.Tika;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class FileUtils {

    public static String[] PRINTABLE_MIMETYPES = { "text/x-java-properties", "application/json", "text/plain", "application/x-yaml", "text/yaml", "text/x-log"};

    public static CompletableFuture<Void> downloadResourceToServer(String url, Server server, String resource_name, String copy_to){

        return CompletableFuture.runAsync(() -> {

            try {
                downloadResourceToServer(url, server, resource_name).get();
                Files.copy(Path.of(server.getMainDirectory() + "/" + resource_name), Path.of(copy_to));
            } catch (InterruptedException | ExecutionException | IOException e) {
                // e.printStackTrace();
                if(e instanceof IOException) Core.logger.warn("failed to copy %s", resource_name);
                else Core.logger.warn("failed to download %s", url);
            }

        });

    }
    public static CompletableFuture<Void> downloadResourceToServer(String url, Server server, String resource_name){

        return CompletableFuture.runAsync(() -> {

            System.out.printf("downloading %s...%n", url);
            Request request = new Request.Builder().url(url).build();
            try {

                Instant start = Instant.now();
                Response response = Core.getHttpClient().newCall(request).execute();
                ResponseBody body = response.body();
                long len = Objects.requireNonNull(body).contentLength();
                BufferedSource src = body.source();
                File file = new File(server.getMainDirectory() + "/" + resource_name);
                BufferedSink sink = Okio.buffer(Okio.sink(file));
                long read = 0, current_read_bytes = 0;

                while(
                        (read = src.read(sink.getBuffer(), 2048)) != -1
                ){
                    current_read_bytes += read;
                }

                System.out.printf("wrote %d bytes (%ds)!%n", current_read_bytes, Duration.between(start, Instant.now()).toMillis() / 1000);
                sink.writeAll(src);
                sink.flush();
                sink.close();
                response.close();

            } catch (IOException e) {

                // e.printStackTrace();
                Core.logger.warn("failed to download %s", url);

            }

        });

    }

    public static void deleteDirectory(Path path) throws IOException {

        Files
                .walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

    }

    public static void resolveAndCreate(Server server, String path, boolean is_dir) throws IOException {

        String[] subdirs = path.split("/");

        for (String subdir: subdirs) if (path.equals("") || path.equals(" ")) return;

        // Files.createDirectories(Path.of(path));

        if(!is_dir){

            String filename = subdirs[subdirs.length - 1];
            String no_filename = String.join("/", Arrays.asList(Arrays.copyOfRange(subdirs, 0, subdirs.length - 1)));
            System.out.println(no_filename);
            Files.createDirectories(Path.of(server.getMainDirectory() + "/" + no_filename));
            Files.createFile(Path.of(server.getMainDirectory() + "/" + path));

        } else Files.createDirectories(Path.of(server.getMainDirectory() + "/" + path));

    }

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

        File f = Path.of(main_path + "/" + supposed_path).toFile();
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
