package com.github.luglimaccaferri.qbic.data.models;

import org.eclipse.jetty.websocket.api.Session;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class ServerRunner extends Thread {

    private Server server;
    private ProcessBuilder builder;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private long pid;

    public long getPid() { return pid; }
    public void destroyProcess(){
        this.process.destroy();
    }

    public ServerRunner(Server server){

        this.server = server;
        // "java -Xmx" + xmx + " -Xms" + xms + " -jar server.jar -nogui"
        this.builder = new ProcessBuilder("java", "-Xmx" + server.getXmx(), "-Xms" + server.getXms(), "-jar", "server.jar", "nogui");
        this.builder.redirectErrorStream(true);
        this.builder.directory(Path.of(server.getMainDirectory().toString()).toFile());

    }

    @Override
    public void run() {

        try {
            System.out.format("starting %s\n", server.getServerName());
            this.process = this.builder.start();
            this.reader = new BufferedReader(
                    new InputStreamReader(
                            this.process.getInputStream()
                    )
            );

            this.pid = this.process.pid();

            System.out.printf("started server %s (pid: %d)%n", server.getServerName(), this.pid);

            String line;
            try{
                while((line = this.reader.readLine()) != null){
                    if(server.getSessions().size() > 0){
                        final String s = line;
                        server.getSessions().forEach(session -> {
                            try {
                                session.getRemote().sendString(s);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }catch(IOException e){

                System.out.format("closed console stream for %s\n", server.getServerId());

            }finally{
                // qualcosa è successo (processo fermato o in error)
                Server.addCreated(server);
                server.getSessions().forEach(Session::close);

                System.out.format("server %s stopped!\n", server.getServerName());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
