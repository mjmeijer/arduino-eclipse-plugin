/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package io.sloeber.autoBuild.api;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import io.sloeber.autoBuild.integration.AutoBuildConfigurationDescription;
import io.sloeber.schema.api.IBuilder;

/**
 * Interface implemented by toolchain integrators to perform the actual build.
 *
 * @author Doug Schaefer
 * @since 8.0
 */
public abstract class IBuildRunner {

    /**
     * Perform the build.
     *
     * @param kind
     *            - kind from the IncrementalProjectBuilder
     * @param project
     *            - project being built
     * @param icConfigurationDescription
     *            - configuration being built
     * @param console
     *            - console to use for build output
     * @param markerGenerator
     *            - generator to add markers for build problems
     * @param monitor
     *            - progress monitor in the initial state where
     *            {@link IProgressMonitor#beginTask(String, int)}
     *            has not been called yet.
     * @throws CoreException
     *             standard core exception if something goes wrong
     */
    public abstract boolean invokeBuild(int kind, AutoBuildConfigurationDescription autoData, IBuilder builder,
            IMarkerGenerator markerGenerator, IncrementalProjectBuilder projectBuilder, IConsole console,
            IProgressMonitor monitor) throws CoreException;

    public abstract String getName();

    public abstract boolean supportsParallelBuild();

    public abstract boolean supportsStopOnError();

    public abstract boolean supportsCustomCommand();

    public abstract boolean supportsMakeFiles();

    public abstract boolean supportsAutoBuild();

    public abstract boolean supportsIncrementalBuild();

    public abstract boolean supportsCleanBuild();

}
