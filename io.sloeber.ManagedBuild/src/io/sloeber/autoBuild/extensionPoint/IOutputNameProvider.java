package io.sloeber.autoBuild.extensionPoint;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import io.sloeber.schema.api.IConfiguration;
import io.sloeber.schema.api.IInputType;
import io.sloeber.schema.api.IOutputType;
import io.sloeber.schema.api.ITool;

public interface IOutputNameProvider {
    /***********
     * Given a file provide the name of the file that should be created
     * oly the filename should be provided the final location of the file is not
     * relevant for the name provider
     * Assume the file is c:\X/Y/Z/c.cpp a correct return value could be the string
     * "c.cpp.o"
     * 
     * Note that configurationdescription, a inputtype are provided as information
     * for advanced name provider functionality
     * Therefore these can be null for convenience reasons
     * 
     * @param inputFile
     *            The file that is to be processed by tool
     * @param config
     *            The configuration the build is run on
     * @param inputType
     *            The input type the inputFile belongs to
     * 
     * @return the file name the tool should generate or null if the inputFile
     *         should be ignored
     */
    public String getOutputFileName(IFile inputFile, ICConfigurationDescription config, IInputType inputType,
            IOutputType outputType);
}
