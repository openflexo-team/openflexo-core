package org.openflexo.foundation.lsp.server;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Workspace service for the FML Language Server.
 * Currently not implemented.
 */
public class FMLWorkspaceService implements WorkspaceService{
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // TODO Auto-generated method stub
        System.out.println("Configuration modifié");
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // TODO Auto-generated method stub
        System.out.println("Changement de fichier");
    }
}
