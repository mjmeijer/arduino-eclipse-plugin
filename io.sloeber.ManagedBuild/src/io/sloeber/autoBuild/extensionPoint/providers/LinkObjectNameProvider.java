package io.sloeber.autoBuild.extensionPoint.providers;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IFile;

import io.sloeber.autoBuild.extensionPoint.IOutputNameProvider;
import io.sloeber.schema.api.IInputType;
import io.sloeber.schema.api.IOutputType;
import io.sloeber.schema.api.ITool;

public class LinkObjectNameProvider implements IOutputNameProvider {
    public static String OBJECT_EXTENSION = ".o";

    public LinkObjectNameProvider() {
        // nothing to do here
    }

    @Override
    public String getOutputFileName(IFile inputFile, ICConfigurationDescription config, IInputType inputType,
            IOutputType outputType) {
        if (ArchiveNameProvider.isArchiveInputFile(inputFile)) {
            return null;
        }
        return inputFile.getName() + OBJECT_EXTENSION;
    }

}
