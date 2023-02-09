/*******************************************************************************
 * Copyright (c) 2004, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package io.sloeber.schema.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.core.resources.IResource;
import io.sloeber.schema.api.IConfiguration;
import io.sloeber.schema.api.IManagedProject;
import io.sloeber.schema.api.IProjectType;
import io.sloeber.schema.api.ISchemaObject;

public class ManagedProject implements IManagedProject {

    //  Parent and children
    private IProjectType projectType;
    private String projectTypeId;
    private IResource owner;
    //	private List configList;	//  Configurations of this project type
    private Map<String, Configuration> configMap = Collections
            .synchronizedMap(new LinkedHashMap<String, Configuration>());
    //  Miscellaneous
    private boolean isDirty = false;
    private boolean isValid = true;
    private boolean resolved = true;
	private String id;
	private String name;

    /*
     *  C O N S T R U C T O R S
     */

    /* (non-Javadoc)
     * Sets the Eclipse project that owns the Managed Project
     *
     * @param owner
     */
    protected ManagedProject(IResource owner) {
        this.owner = owner;
    }


    /*
     *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
     */

    /* (non-Javadoc)
     * Initialize the project information from the XML element
     * specified in the argument
     *
     * @param element An XML element containing the project information
     */
    protected boolean loadFromProject(ICStorageElement element) {
        // note: id and name are unique, so don't intern them
        // id
        id = (element.getAttribute(ISchemaObject.ID));

        // name
        name = (element.getAttribute(ISchemaObject.NAME));

        // projectType
        projectTypeId = element.getAttribute(PROJECTTYPE);
        if (projectTypeId != null && projectTypeId.length() > 0) {
            projectType = null;//TOFIX JABA ManagedBuildManager.getExtensionProjectType(projectTypeId);
            if (projectType == null) {
                return false;
            }
        }

        return true;
    }

    public void serializeProjectInfo(ICStorageElement element) {
        element.setAttribute(ISchemaObject.ID, id);

        if (name != null) {
            element.setAttribute(ISchemaObject.NAME, name);
        }

        if (projectType != null) {
            element.setAttribute(PROJECTTYPE, projectType.getId());
        }

        // I am clean now
        isDirty = false;
    }

    /*
     *  P A R E N T   A N D   C H I L D   H A N D L I N G
     */

    /* (non-Javadoc)
     * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#getOwner()
     */
    @Override
    public IResource getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#updateOwner(org.eclipse.core.resources.IResource)
     */
    @Override
    public void updateOwner(IResource resource) {
        if (!resource.equals(owner)) {
            // Set the owner correctly
            owner = resource;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#getProjectType()
     */
    //@Override
    public IProjectType getProjectType() {
        return projectType;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.build.managed.IManagedProject#getConfiguration()
     */
    @Override
    public IConfiguration getConfiguration(String id) {
        return configMap.get(id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#getConfigurations()
     */
    @Override
    public IConfiguration[] getConfigurations() {
        synchronized (configMap) {
            return configMap.values().toArray(new IConfiguration[configMap.size()]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#removeConfiguration(java.lang.String)
     */
    @Override
    public void removeConfiguration(String id) {
        final String removeId = id;

        //handle the case of temporary configuration
        if (!configMap.containsKey(id))
            return;

        configMap.remove(removeId);
        //
        //		IWorkspaceRunnable remover = new IWorkspaceRunnable() {
        //			public void run(IProgressMonitor monitor) throws CoreException {
        //				// Remove the specified configuration from the list and map
        //				Iterator iter = getConfigurationCollection().iterator();
        //				while (iter.hasNext()) {
        //					 IConfiguration config = (IConfiguration)iter.next();
        //					 if (config.getId().equals(removeId)) {
        //						// TODO:  For now we clean the entire project.  This may be overkill, but
        //						//        it avoids a problem with leaving the configuration output directory
        //					 	//        around and having the outputs try to be used by the makefile generator code.
        //					 	IResource proj = config.getOwner();
        //						IManagedBuildInfo info = null;
        //					 	if (proj instanceof IProject) {
        //							info = ManagedBuildManager.getBuildInfo(proj);
        //					 	}
        //						IConfiguration currentConfig = null;
        //						boolean isCurrent = true;
        //			 			if (info != null) {
        //			 				currentConfig = info.getDefaultConfiguration();
        //			 				if (!currentConfig.getId().equals(removeId)) {
        //			 					info.setDefaultConfiguration(config);
        //			 					isCurrent = false;
        //			 				}
        //			 			}
        //			 			((IProject)proj).build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        //
        //			 			ManagedBuildManager.performValueHandlerEvent(config,
        //			 					IManagedOptionValueHandler.EVENT_CLOSE);
        //						PropertyManager.getInstance().clearProperties(config);
        ////					 	getConfigurationList().remove(config);
        //						getConfigurationMap().remove(removeId);
        //
        //						if (info != null) {
        //							if (!isCurrent) {
        //			 					info.setDefaultConfiguration(currentConfig);
        //							} else {
        //								// If the current default config is the one being removed, reset the default config
        //								String[] configs = info.getConfigurationNames();
        //								if (configs.length > 0) {
        //									info.setDefaultConfiguration(configs[0]);
        //								}
        //							}
        //			 			}
        //						break;
        //					}
        //				}
        //			}
        //		};
        //		try {
        //			ResourcesPlugin.getWorkspace().run( remover, null );
        //		}
        //		catch( CoreException e ) {}
    }

    /* (non-Javadoc)
     * Adds the Configuration to the Configuration list and map
     *
     * @param Tool
     */
    public void addConfiguration(Configuration configuration) {
      //  if (!configuration.isTemporary())
            configMap.put(configuration.getId(), configuration);
    }

    /**
     * (non-Javadoc)
     * Safe accessor for the list of configurations.
     *
     * @return List containing the configurations
     */
    private Collection<Configuration> getConfigurationCollection() {
        synchronized (configMap) {
            return new ArrayList<>(configMap.values());
        }
    }

    /*
     *  M O D E L   A T T R I B U T E   A C C E S S O R S
     */

    /*
     *  O B J E C T   S T A T E   M A I N T E N A N C E
     */

    /* (non-Javadoc)
     * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#(getDefaultArtifactName)
     */
    @Override
    public String getDefaultArtifactName() {
        return CdtVariableResolver.createVariableReference(CdtVariableResolver.VAR_PROJ_NAME);
    }

  
    /* (non-Javadoc)
     * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#isValid()
     */
    @Override
    public boolean isValid() {
        //  TODO:  In the future, children could also have a "valid" state that should be checked
        return isValid;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.managedbuilder.core.IManagedProject#setValid(boolean)
     */
    @Override
    public void setValid(boolean isValid) {
        //  TODO:  In the future, children could also have a "valid" state...
        this.isValid = isValid;
    }


    public void setProjectType(IProjectType projectType) {
        if (this.projectType != projectType) {
            this.projectType = projectType;
            if (this.projectType == null) {
                projectTypeId = null;
            } else {
                projectTypeId = this.projectType.getId();
            }
        }
    }

    public String getId() {
    	return id;
    }
   public String getName() {
    	return name;
    }
}

