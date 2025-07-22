package org.openflexo.foundation.lsp.parser;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;



/**
 * This class is responsible for resolving or creating {@link CompilationUnitResource} instances
 * based on FML files and the associated {@link ResourceCenter}.
 * 
 * It check if a resource already exists, locate or create the appropriate Resource Center,
 * and handle the creation of new compilation unit resources.
 */
public class CompilationUnitResourceResolver {
	
    private final ResourceManager resourceManager;
    private final FlexoResourceCenterService resourceCenterService;
    private final FMLTechnologyAdapter fmlTA;
    
    
    protected static final String RESOURCE_CENTER_URI = "http://openflexo.org/test/TestResourceCenter";
    private static final String FML_EXTENSION = ".fml";
    
    private static final Logger logger = Logger.getLogger(CompilationUnitResourceResolver.class.getPackage().getName());
    
    public CompilationUnitResourceResolver(FlexoServiceManager serviceManager) {
        this.resourceManager = serviceManager.getResourceManager();
        this.resourceCenterService = serviceManager.getResourceCenterService();
        
        this.fmlTA = getFMLTechnologyAdapter();
    }
    
    
    /**
     * Retrieves the {@link FMLTechnologyAdapter} from the resource manager.
     * 
     * @return the FMLTechnologyAdapter if found, otherwise null
     */
    private FMLTechnologyAdapter getFMLTechnologyAdapter() {
    	FMLTechnologyAdapter fmlTA = null;
        for (TechnologyAdapter<?> ta : resourceManager.getTechnologyAdapters()) {
            if (ta instanceof FMLTechnologyAdapter) {
                fmlTA = (FMLTechnologyAdapter) ta;
                return fmlTA;
            }
        }
        if (logger.isLoggable(Level.WARNING)) {
			logger.warning("FMLTechnologyAdapter not found");
		}
        return null;
        
    }
	
    /**
     * Resolves an existing {@link CompilationUnitResource} from a given FML file,
     * or creates a new one if it does not exist yet.
     *
     * @param fmlFile the .fml file to resolve or create a resource for
     * @return the corresponding {@link CompilationUnitResource}, or null if an error occurred
     */
	public CompilationUnitResource resolveOrCreateCompilationUnitResource(File fmlFile) {
		
		if (!fmlFile.exists()) {
			if (logger.isLoggable(Level.WARNING)) {
    			logger.warning("FML file not found : " + fmlFile.getAbsolutePath());
    		}
            return null;
        }		
		
		
		// Step 1: Check if a resource already exists for this file
		String filePath = fmlFile.toURI().toString();
		FlexoResource<?> resource = resourceManager.getResource(filePath);
		if(resource != null) {
			return (CompilationUnitResource) resource;
		}
				  
		
		 // Step 2: Check if file is within a known resource center
        FlexoResourceCenter<File> resourceCenter = resourceCenterService.getResourceCenterContaining(fmlFile);
        if (resourceCenter == null) {
        	
            //File is not in a known resource center -> create a new one.
            File folder = fmlFile.getParentFile();
            DirectoryResourceCenter rc = createResourceCenter(folder, resourceCenterService);
            
            if(rc == null) {
            	return null;
            }
            
            return (CompilationUnitResource) rc.getResource(filePath);
        
        }   
        else {
        	// File is in a known resource center but no resource exists yet -> create it
            return createCompilationUnitResource(fmlFile, resourceCenter);
        }
        
	}
	
	
	/**
     * Creates a new {@link CompilationUnitResource} in the specified resource center.
     * 
     * @param fmlFile the FML file
     * @param resourceCenter the resource center in which the resource will be created
     * @return the newly created CompilationUnitResource, or null in case of failure
     */
	private CompilationUnitResource createCompilationUnitResource(File fmlFile,
			FlexoResourceCenter<File> resourceCenter) {
		
		CompilationUnitResourceFactory factory = fmlTA.getCompilationUnitResourceFactory();
        String baseName = fmlFile.getName().replace(FML_EXTENSION, "");

        
        //Retrieves or creates the corresponding folder inside the RC
        RepositoryFolder folder;
		try {
			folder = resourceCenter.getRepositoryFolder(fmlFile.getParentFile(), true);
			
			//Create a new resource
	        CompilationUnitResource resource = factory.makeTopLevelCompilationUnitResource(
	            baseName,
	            fmlFile.toURI().toString(),
	            folder,
	            true // createEmptyContents
	        );
	
	        return resource;
		} 
		catch (IOException | SaveResourceException | ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		return null;
	}

	/**
     * Creates a new {@link DirectoryResourceCenter} based on a folder, and registers it with the RC service.
     * 
     * @param folder   the base folder for the new resource center
     * @param rcService the resource center service to register with
     * @return the created {@link DirectoryResourceCenter}, or null in case of error
     */
	private static DirectoryResourceCenter createResourceCenter(File folder, FlexoResourceCenterService rcService) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.warning("Invalid folder for ResourceCenter: " + folder);
    		}
            return null;
        }

        try {
            DirectoryResourceCenter resourceCenter = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(folder, rcService);
            resourceCenter.setDefaultBaseURI(RESOURCE_CENTER_URI);
            rcService.addToResourceCenters(resourceCenter);
            return resourceCenter;     
        } 
        catch (Exception e) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.warning("Failed to create ResourceCenter for folder: " + folder.getAbsolutePath() + e.toString());
    		}
            return null;
        }
    }

}
