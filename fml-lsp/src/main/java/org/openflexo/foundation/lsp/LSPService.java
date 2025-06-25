package org.openflexo.foundation.lsp;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.lsp.server.FMLLanguageServer;



public class LSPService extends FlexoServiceImpl{

    private static LSPService instance;

    private FMLLanguageServer server;
    private ExecutorService executor;
    private Launcher<LanguageClient> launcher;



    public LSPService(){

    }

    public static LSPService createInstance(){
        instance = new LSPService();
        return instance;
    }

    @Override
    public void initialize() {
        start();
    }

    @Override
    public String getServiceName() {
        return "LSPService";
    }

    public static LSPService getInstance(){
        return instance;
    }
    
    /**
     * Starts the FML Language Server on a background thread.
     * 
     * Opens a socket on port 5007, waits for a client connection, and
     * sets up the LSP communication channel using the LSP4J launcher.
     * 
     * This method blocks the thread while waiting for messages from the client.
     */
    public void start() {
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(5007)) {
                System.out.println("LSP Server waiting for client on port 5007...");

                Socket clientSocket = serverSocket.accept();
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                server = new FMLLanguageServer();
                
                //TODO gérer les logs avec le traceWriter
                PrintWriter traceWriter = null;

                launcher = LSPLauncher.createServerLauncher(server, in, out, false, traceWriter);
                LanguageClient client = launcher.getRemoteProxy();
                server.connect(client);

                launcher.startListening().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
    
    public FMLLanguageServer getServer() {
    	return server;
    }

}
