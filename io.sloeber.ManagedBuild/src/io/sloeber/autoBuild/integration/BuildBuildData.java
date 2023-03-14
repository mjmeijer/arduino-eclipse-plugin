/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
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
 * Baltasar Belyavsky (Texas Instruments) - bug 340219: Project metadata files are saved unnecessarily
 *******************************************************************************/
package io.sloeber.autoBuild.integration;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.settings.model.COutputEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import io.sloeber.autoBuild.Internal.BuilderFactory;
import io.sloeber.autoBuild.core.Activator;
import io.sloeber.schema.api.IBuilder;
import io.sloeber.schema.api.IToolChain;
import io.sloeber.schema.internal.Builder;
import io.sloeber.schema.internal.Configuration;

public class BuildBuildData extends CBuildData {
    private Builder fBuilder;
    private Configuration fCfg;
    private IProject myProject;
    ICConfigurationDescription myCdtConfigurationDescription;
    BuildEnvironmentContributor myBuildEnvironmentContributor;
    private ICOutputEntry[] myEntries = null;// new ICOutputEntry[0];
    private IPath myBuilderCWD;

    public BuildBuildData(IBuilder builder, ICConfigurationDescription configurationDescription) {
        myCdtConfigurationDescription = configurationDescription;
        myProject = myCdtConfigurationDescription.getProjectDescription().getProject();
        fBuilder = (Builder) builder;
        fCfg = (Configuration) fBuilder.getParent().getParent();
        myBuildEnvironmentContributor = new BuildEnvironmentContributor(fCfg);
        myBuilderCWD = fCfg.getBuildFolder(myCdtConfigurationDescription).getLocation();

        //        IPath path = new Path(myBuilderCWD.toString());
        //        IPath projFullPath = myProject.getFullPath();
        //        if (projFullPath.isPrefixOf(path)) {
        //            path = path.removeFirstSegments(projFullPath.segmentCount()).makeRelative();
        //        } else {
        //            path = Path.EMPTY;
        //        }

        myEntries = new ICOutputEntry[] {
                new COutputEntry(myBuilderCWD, null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED) };
    }

    //    public BuildBuildData(Configuration fCfg2, IProject project) {
    //        fCfg = fCfg2;
    //        IToolChain toolchain = fCfg.getToolChain();
    //        fBuilder = (Builder) toolchain.getBuilder();
    //        myProject = project;
    //
    //    }

    public Configuration getConfiguration() {
        return fCfg;
    }

    @Override
    public IPath getBuilderCWD() {
        return myBuilderCWD;
    }

    //	private IPath createAbsolutePathFromWorkspacePath(IPath path){
    //		IStringVariableManager mngr = VariablesPlugin.getDefault().getStringVariableManager();
    //		String locationString = mngr.generateVariableExpression("workspace_loc", path.toString()); //$NON-NLS-1$
    //		return new Path(locationString);
    //	}

    @Override
    public String[] getErrorParserIDs() {
        return fCfg.getErrorParserList();
    }

    @Override
    public ICOutputEntry[] getOutputDirectories() {
        return myEntries;
    }

    @Override
    public void setBuilderCWD(IPath path) {
        myBuilderCWD = path;
    }

    @Override
    public void setErrorParserIDs(String[] ids) {
        // fCfg.setErrorParserList(ids);
    }

    @Override
    public void setOutputDirectories(ICOutputEntry[] entries) {
        myEntries = entries;
    }

    @Override
    public String getId() {
        return fBuilder.getId();
    }

    @Override
    public String getName() {
        return fBuilder.getName();
    }

    @Override
    public boolean isValid() {
        return fBuilder != null;
    }

    //    public void setName(String name) {
    //        //TODO
    //    }

    @Override
    public IEnvironmentContributor getBuildEnvironmentContributor() {
        return myBuildEnvironmentContributor;
    }

    @Override
    public ICommand getBuildSpecCommand() {
        try {
            return BuilderFactory.createCommandFromBuilder(myProject, this.fBuilder);
        } catch (CoreException cx) {
            Activator.log(cx);
            return null;
        }
    }

    //    public IBuilder getBuilder() {
    //        return fBuilder;
    //    }

    public ICConfigurationDescription getCdtConfigurationDescription() {
        return myCdtConfigurationDescription;
    }

}
