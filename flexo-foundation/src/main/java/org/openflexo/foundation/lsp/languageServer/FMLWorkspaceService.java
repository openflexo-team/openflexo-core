package org.openflexo.foundation.lsp.languageServer;

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
        throw new UnsupportedOperationException("Unimplemented method 'didChangeConfiguration'");
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'didChangeWatchedFiles'");
    }
}
