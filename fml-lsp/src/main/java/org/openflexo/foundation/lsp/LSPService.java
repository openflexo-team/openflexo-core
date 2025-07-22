package org.openflexo.foundation.lsp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.lsp.server.FMLLanguageServer;
import org.openflexo.foundation.lsp.websocket.WebSocketLauncher;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * LSPService is a Flexo service that starts a Language Server Protocol (LSP) server
 * for FML
 *
 * This service supports both:
 * - multiple TCP connections (typically used by IDEs)
 * - WebSocket connections (used by web-based editors)
 *
 * Each client connection creates an independent LSP session, handled in parallel.
 */
public class LSPService extends FlexoServiceImpl{

    private static LSPService instance;

    private FMLLanguageServer server;
    private Launcher<LanguageClient> launcher;
    
    // Port number arbitrarily chosen for the server; can be changed if needed.
    private static final int PORT = 5007;
    
    private final ExecutorService executor = Executors.newCachedThreadPool(); // For several clients
    
    // Indicates whether the server is currently running.
    // Marked as volatile to ensure visibility across multiple threads.
    private volatile boolean running = true;
    
    private static final Logger logger = Logger.getLogger(LSPService.class.getPackage().getName());


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
     
    
    public void start() {
        // start the LSP server using TCP (for IDEs)
        new Thread(this::startLSPServer).start();

        // start the LSP server using WebSocket (for web editor)
        new Thread(this::startWebSocketServer).start();
    }
    
    
    /**
     * Starts the FML Language Server on a background thread.
     * 
     * Opens a socket on port 5007, waits for a client connection, and
     * sets up the LSP communication channel using the LSP4J launcher.
     * 
     * This method blocks the thread while waiting for messages from the client.
     */
    public void startLSPServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            
            if (logger.isLoggable(Level.INFO)) {
    			logger.info("LSP Server running on port " + PORT);
    		}

            while (running) {
                Socket clientSocket = serverSocket.accept();
                if (logger.isLoggable(Level.INFO)) {
                	logger.info("New client connected from " + clientSocket.getInetAddress());
        		}
                executor.submit(() -> handleClient(clientSocket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    
    /**
    * Handles a client connection over a given socket.
    *
    * Initializes a {@link FMLLanguageServer}, sets up an LSP {@link Launcher} for 
    * communication, logs session activity, and listens for requests until the client disconnects.
    *
    * @param socket the client {@link Socket} connection.
    */
    private void handleClient(Socket socket) {
        String sessionId = UUID.randomUUID().toString();

        try (
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream()
        ) {
            FMLLanguageServer languageServer = new FMLLanguageServer(this.getServiceManager());
            
           // Create a log file for the session
            
            // Retrieve the system temporary directory
            String tempDir = System.getProperty("java.io.tmpdir");
            
            File logFile = new File(tempDir + sessionId + ".log");
            
            // Make sure the directory exist
            logFile.getParentFile().mkdirs();
            PrintWriter traceWriter = new PrintWriter(new FileWriter(logFile), true);

            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
                languageServer,
                in,
                out,
                false,
                traceWriter
            );
            
            languageServer.connect(launcher.getRemoteProxy());


            if (logger.isLoggable(Level.INFO)) {
            	logger.info("New LSP session started with ID: " + sessionId);
            }

            launcher.startListening().get();

        } catch (Exception e) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.warning("Error occurred in session " + sessionId + e.toString());
        	}
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {}
            
            if (logger.isLoggable(Level.INFO)) {
            	logger.info("LSP session " + sessionId + " has been closed.");
            }
        }
    }
    
    
    /**
     * Starts the WebSocket server to handle incoming connections.
     * Creates and launches a new {@link WebSocketLauncher} instance, using the current
     * service manager for initialization.
     */
    public void startWebSocketServer() {
        (new WebSocketLauncher()).start(this.getServiceManager());
    }

    /**
     * Shuts down the server and releases resources.
     * Stops background tasks, terminates the executor, and logs the shutdown event.
     */
    public void shutdown() {
        running = false;
        executor.shutdown();
        if (logger.isLoggable(Level.INFO)) {
        	logger.info("LSP server has been closed.");
        }
    }

    
    public FMLLanguageServer getServer() {
    	return server;
    }

}
