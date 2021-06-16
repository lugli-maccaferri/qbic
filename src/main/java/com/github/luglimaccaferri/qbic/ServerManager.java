package com.github.luglimaccaferri.qbic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ServerManager {

    private final Path folder_path;
    private final String name;
    private final String version;

    private Process pr;
    private ProcessBuilder pb;
    private BufferedReader reader;
    private BufferedWriter writer;

    private Thread tr;

    public enum ServerState {
        MISSING_FILE_ERROR,
        TO_INIT,
        EULA_ERROR,
        SHUTTING_DOWN,
        STOPPED,
        STARTING_UP,
        RUNNING,
        UNKNOWN
    }
    private ServerState state;

    public ServerManager(String folder_location, String name, String version) {

        this.folder_path = Paths.get(folder_location);  //Exception
        this.name = name;
        this.version = version;

        this.state = ServerState.UNKNOWN;
        updateState();

    }

    //temp
    public void printRecap() {

        System.out.println("Path: " + folder_path);
        System.out.println("Name: " + name);
        System.out.println("Version: " + version);
        System.out.println("Current state: " + getState());

    }

    private boolean serverFileExists() {

       Path server_file_path = this.folder_path.resolve("server.jar");
       return Files.exists(server_file_path);

    }

    private boolean serverPropertiesFileExists() {

        Path server_properties_file_path = this.folder_path.resolve("server.properties");
        return Files.exists(server_properties_file_path);

    }

    private boolean eulaFileExists() {

        Path eula_file_path = this.folder_path.resolve("eula.txt");
        return Files.exists(eula_file_path);

    }

    private boolean eulaCheck() {

        if (this.state == ServerState.TO_INIT)
            return false;

        String true_line = "eula=true";
        String false_line = "eula=false";
        int true_pos = -1;
        int false_pos = -1;

        Path eula_file_path = this.folder_path.resolve("eula.txt");
        Scanner scanner;
        try {

            scanner = new Scanner(eula_file_path.toFile());

            int i = 0;
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
                i++;
                if(line.toLowerCase().startsWith(true_line.toLowerCase())) {
                    true_pos = i;
                }
                else if(line.toLowerCase().startsWith(false_line.toLowerCase())) {
                    false_pos = i;
                }

            }

        } catch (FileNotFoundException e) {

            System.out.println("Error: Eula file not found!");
            return false;

        }

        if (true_pos >= 0 && true_pos > false_pos)
            return true;

        return false;

    }

    private void updateState() {

        switch (this.state) {
            case RUNNING, SHUTTING_DOWN, STARTING_UP:
                return;
            default:
                break;
        }

        if (!serverFileExists())
            this.state = ServerState.MISSING_FILE_ERROR;
        else if (!eulaFileExists())
            this.state = ServerState.TO_INIT;
        else if (!eulaCheck())
            this.state = ServerState.EULA_ERROR;
        else
            this.state = ServerState.STOPPED;

    }

    public ServerState getState() {

        updateState();
        return this.state;

    }

    public ServerState nextState() {

        updateState();

        return switch (this.state) {
            case MISSING_FILE_ERROR, TO_INIT, EULA_ERROR -> this.state;
            case RUNNING -> ServerState.STOPPED;
            default -> ServerState.RUNNING;
        };

    }

    private boolean writeToServerStdin(String in) {

       if (writer == null) {
           System.out.println("Error: Writer is null");
           return false;
       }

        try {
            writer.write(in);
            writer.flush();
            System.out.println("Wrote \"" + in.replaceAll("\n", "") + "\" correctly");
            return true;
        }
        catch (IOException e) {
            System.out.println("Error: Exception during write. Server process probably ended unexpectedly.");
            return false;
        }

    }

    public boolean sendCommandToServer(String cmd) {

        if (state != ServerState.RUNNING && state != ServerState.SHUTTING_DOWN)
            return false;

        if (!cmd.startsWith("/"))
            cmd = "/" + cmd;
        if (!cmd.endsWith("\n"))
            cmd = cmd + "\n";

        return writeToServerStdin(cmd);

    }

    private boolean waitForServerStop() {

        if (pr == null)
            return false;

        int timeout_sec = 20;

        try {

            System.out.println("Waiting " + timeout_sec + " seconds max for server stop...");
            if (pr.waitFor(timeout_sec, TimeUnit.SECONDS)) {
                System.out.println("Server stopped successfully!");
                return true;
            }

            System.out.println("Server failed to stop in the given time");
            return false;

        }
        catch (InterruptedException e) {
            System.out.println("Error");
            return false;
        }

    }

    private void serverPropertiesModifier() {

        //

    }

    public int getCurrentPlayers() {

        if (!sendCommandToServer("list"))
            return -1;

        String line;

        try {
            line = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error while reading server output line");
            return -1;
        }
        while (line != null) {
            if (line.contains("There are"))
                break;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                System.out.println("Error while reading server output line");
                return -1;
            }
        }

        if (line == null) {
            System.out.println("Error, no output received from server");
            return -1;
        }

        int ret;
        try {
            ret = Integer.parseInt(line.replaceAll(".+are (\\d+).+", "$1"));
        }
        catch (NumberFormatException e) {
            System.out.println("Error, could not convert the output received from server");
            ret = -1;
        }

        return ret;

    }

    private void syncFirstStart() {

        this.state = ServerState.STARTING_UP;

        System.out.println("\nNow going to initialize the server...");

        pb = new ProcessBuilder("java", "-Xmx1024M", "-Xms1024M" ,"-jar", "server.jar", "nogui");
        pb.redirectErrorStream(true);   //May also want to keep them separated
        pb.directory(this.folder_path.toFile());

        //run command
        try {
            pr = pb.start();
        } catch (IOException e) {
            System.out.println("Error while starting process");
            this.state = ServerState.STOPPED;
            updateState();
            return;
        }

        System.out.println("Server performing first start...");

        /*
        System.out.println("Setting up output redirection...");
        reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;
         */

        System.out.println("Waiting for process to end...");
        if (!waitForServerStop()) {
            System.out.println("Error");
            this.state = ServerState.STOPPED;
            updateState();
            return;
        }

        /*
        System.out.println("Closing buffered reader...");
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();    //Act accordingly
            System.out.println("ERROR 4");
            return;
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();    //Act accordingly
            System.out.println("ERROR 4");
            return;
        }
        */

        System.out.println("All done!\n");

        this.state = ServerState.STOPPED;

    }

    private void firstStart() {

        Runnable first_start_runnable = new Runnable() {
            @Override
            public void run() {
                syncFirstStart();
            }
        };

        tr = new Thread(first_start_runnable);
        tr.start();

    }

    private void syncStart() {

        this.state = ServerState.STARTING_UP;

        System.out.println("\nNow going to start the server...");

        pb = new ProcessBuilder("java", "-Xmx1024M", "-Xms1024M" ,"-jar", "server.jar", "nogui");
        pb.redirectErrorStream(true);   //May also want to keep them separated
        pb.directory(this.folder_path.toFile());

        //run command
        try {
            pr = pb.start();
        } catch (IOException e) {
            System.out.println("Error while starting process");
            this.state = ServerState.STOPPED;
            updateState();
            return;
        }

        System.out.println("Server starting...");
        System.out.println("Setting up output redirection...");
        reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));

        String line;
        System.out.println("Waiting for server to finish starting up...");

        //Reads server output until the line containing "Done" is found
        try {
            line = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error while reading server output line");
            return;
        }
        while (line != null) {
            if (line.contains("Done"))
                break;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                System.out.println("Error while reading server output line");
                return;
            }
        }

        if (line == null) {
            System.out.println("Server failed to start\n");
            this.state = ServerState.STOPPED;
            return;
        }

        System.out.println("Server started successfully!\n");

        this.state = ServerState.RUNNING;

    }

    private void start() {

        Runnable start_runnable = new Runnable() {
            @Override
            public void run() {
                syncStart();
            }
        };

        tr = new Thread(start_runnable);
        tr.start();

    }

    private void syncStop() {

        this.state = ServerState.SHUTTING_DOWN;

        if (!sendCommandToServer("stop"))
            System.out.println("Error: Couldn't send stop command to server");

        if (!waitForServerStop()) {
            System.out.println("Error");
            this.state = ServerState.STOPPED;
            updateState();
            return;
        }

        this.state = ServerState.STOPPED;

    }

    private void stop() {

        Runnable stop_runnable = new Runnable() {
            @Override
            public void run() {
                syncStop();
            }
        };

        tr = new Thread(stop_runnable);
        tr.start();

    }

    public void nextStep() {

        ServerState next = nextState();

        switch (next) {

            case MISSING_FILE_ERROR:
                //Display error message
                break;
            case TO_INIT:
                firstStart();
                break;
            case EULA_ERROR:
                //Display error message
                break;
            case SHUTTING_DOWN, STARTING_UP, UNKNOWN:
                break;
            case RUNNING:
                start();
                break;
            default:
                stop();

        }

    }

}