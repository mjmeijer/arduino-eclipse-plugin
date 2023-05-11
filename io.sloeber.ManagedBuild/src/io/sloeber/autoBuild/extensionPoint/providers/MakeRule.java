package io.sloeber.autoBuild.extensionPoint.providers;

import static io.sloeber.autoBuild.core.Messages.*;
import static io.sloeber.autoBuild.extensionPoint.providers.AutoBuildCommon.*;
import static io.sloeber.autoBuild.integration.AutoBuildConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Path;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import io.sloeber.autoBuild.api.BuildException;
import io.sloeber.autoBuild.integration.AutoBuildConfigurationDescription;
import io.sloeber.schema.api.IInputType;
import io.sloeber.schema.api.IOutputType;
import io.sloeber.schema.api.ITool;

public class MakeRule {

    private Map<IOutputType, Set<IFile>> myTargets = new LinkedHashMap<>(); //Macro file target map
    private Map<IInputType, Set<IFile>> myPrerequisites = new LinkedHashMap<>();//Macro file prerequisites map
    private Map<String, Set<IFile>> myDependencies = new LinkedHashMap<>(); //Macro file target map
    private ITool myTool = null;
    private int mySequenceGroupID = 0;

    public ITool getTool() {
        return myTool;
    }

    public MakeRule(ITool tool, IInputType inputType, IFile inputFile, IOutputType outputType, IFile outFile,
            int sequenceID) {
        addPrerequisite(inputType, inputFile);
        addTarget(outputType, outFile);
        myTool = tool;
        mySequenceGroupID = sequenceID;
    }

    public MakeRule(ITool tool, IInputType inputType, Set<IFile> inputFiles, IOutputType outputType, IFile outFile,
            int sequenceID) {
        addPrerequisites(inputType, inputFiles);
        addTarget(outputType, outFile);
        myTool = tool;
        mySequenceGroupID = sequenceID;
    }

    public void getDependencies() {
        myDependencies.clear();
        for (Entry<IOutputType, Set<IFile>> curTarget : myTargets.entrySet()) {
            IOutputType curOutputType = curTarget.getKey();
            Set<IFile> files = curTarget.getValue();
            String depkey = curOutputType.getBuildVariable() + DEPENDENCY_SUFFIX;
            Set<IFile> depFiles = new HashSet<>();
            for (IFile curTargetFile : files) {
                depFiles.add(myTool.getDependencyFile(curTargetFile));
            }
            depFiles.remove(null);
            myDependencies.put(depkey, depFiles);
        }
    }

    public Set<IFile> getPrerequisiteFiles() {
        HashSet<IFile> ret = new HashSet<>();
        for (Set<IFile> cur : myPrerequisites.values()) {
            ret.addAll(cur);
        }
        return ret;
    }

    public Map<IInputType, Set<IFile>> getPrerequisites() {
        return myPrerequisites;
    }

    public Set<IFile> getTargetFiles() {
        Set<IFile> ret = new HashSet<>();
        for (Set<IFile> cur : myTargets.values()) {
            ret.addAll(cur);
        }
        return ret;
    }

    public Set<IFile> getDependencyFiles() {
        getDependencies();
        Set<IFile> ret = new HashSet<>();
        for (Set<IFile> cur : myDependencies.values()) {
            ret.addAll(cur);
        }
        return ret;
    }

    public Map<IOutputType, Set<IFile>> getTargets() {
        return myTargets;
    }

    public Set<String> getAllMacros() {
        Set<String> ret = getTargetMacros();
        ret.addAll(getPrerequisiteMacros());
        ret.addAll(getDependencyMacros());
        return ret;
    }

    public Set<String> getTargetMacros() {
        HashSet<String> ret = new LinkedHashSet<>();
        for (IOutputType cur : myTargets.keySet()) {
            ret.add(cur.getBuildVariable());
        }
        return ret;
    }

    public Set<String> getPrerequisiteMacros() {
        HashSet<String> ret = new LinkedHashSet<>();
        for (IInputType cur : myPrerequisites.keySet()) {
            ret.add(cur.getBuildVariable());
        }
        ret.remove(EMPTY_STRING);
        return ret;
    }

    public Set<String> getDependencyMacros() {
        getDependencies();
        HashSet<String> ret = new LinkedHashSet<>();
        ret.addAll(myDependencies.keySet());
        return ret;
    }

    public HashSet<IFile> getMacroElements(String macroName) {
        HashSet<IFile> ret = new HashSet<>();

        for (Entry<IOutputType, Set<IFile>> cur : myTargets.entrySet()) {
            if (macroName.equals(cur.getKey().getBuildVariable())) {
                ret.addAll(cur.getValue());
            }
        }
        for (Entry<IInputType, Set<IFile>> cur : myPrerequisites.entrySet()) {
            if (macroName.equals(cur.getKey().getBuildVariable())) {
                ret.addAll(cur.getValue());
            }
        }
        Set<IFile> tmp = myDependencies.get(macroName);
        if (tmp != null) {
            ret.addAll(tmp);
        }
        return ret;
    }

    private void addTarget(IOutputType outputType, IFile file) {
        Set<IFile> files = myTargets.get(outputType);
        if (files == null) {
            files = new HashSet<>();
            files.add(file);
            myTargets.put(outputType, files);
        } else {
            files.add(file);
        }
    }

    private void addPrerequisite(IInputType inputType, IFile file) {
        Set<IFile> files = myPrerequisites.get(inputType);
        if (files == null) {
            files = new HashSet<>();
            files.add(file);
            myPrerequisites.put(inputType, files);
        } else {
            files.add(file);
        }
    }

    /**
     * validate if the makerule contains valid recipes
     * 
     * @return true if valid
     *         false if not
     */
    private boolean validateRecipes() {
        Set<IFile> local_targets = getTargetFiles();
        Set<IFile> local_prerequisites = getPrerequisiteFiles();
        if (local_targets.size() != 1) {
            System.err.println("Only 1 target per build rule is supported in this managed build"); //$NON-NLS-1$
            return false;
        }
        if (local_prerequisites.size() == 0) {
            System.err.println("0 prerequisites is not supported in this managed build"); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    public String[] getRecipes(IFolder niceBuildFolder, AutoBuildConfigurationDescription autoBuildConfData) {
        if (!validateRecipes()) {
            return new String[0];
        }
        Set<IFile> local_targets = getTargetFiles();

        IFile targetFile = local_targets.toArray(new IFile[1])[0];

        Set<String> flags = new LinkedHashSet<>();
        Map<String, Set<String>> niceNameList = new HashMap<>();
        for (Entry<IInputType, Set<IFile>> cur : myPrerequisites.entrySet()) {
            String cmdVariable = cur.getKey().getAssignToCmdVarriable();
            for (IFile curPrereqFile : cur.getValue()) {
                Set<String> niceNames = niceNameList.get(cmdVariable);
                if (niceNames == null) {
                    niceNames = new HashSet<>();
                    niceNames.add(GetNiceFileName(niceBuildFolder, curPrereqFile));
                    niceNameList.put(cmdVariable, niceNames);
                }
                niceNames.add(GetNiceFileName(niceBuildFolder, curPrereqFile));
                //JABA I'm not sure it is necessary to loop through all the prerequisites to add all flags
                try {
                    flags.addAll(
                            Arrays.asList(myTool.getToolCommandFlags(autoBuildConfData, curPrereqFile, targetFile)));
                } catch (BuildException e) {
                    e.printStackTrace();
                }
            }
        }

        String buildRecipes[] = myTool.getRecipes(autoBuildConfData, flags,
                GetNiceFileName(niceBuildFolder, targetFile), niceNameList);
        ArrayList<String> ret = new ArrayList<>();
        for (String curRecipe : buildRecipes) {
            String resolvedCommand = resolve(curRecipe, EMPTY_STRING, WHITESPACE, autoBuildConfData);
            if (resolvedCommand.isBlank()) {
                resolvedCommand = curRecipe;
            }
            if (!resolvedCommand.isBlank()) {
                ret.add(resolvedCommand);
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    //    private Set<String> getBuildFlags(AutoBuildConfigurationData autoBuildConfData, IFile sourceFile,
    //            IFile outputFile) {
    //        Set<String> flags = new LinkedHashSet<>();
    //        // Get the tool command line options
    //        try {
    //
    //            //IResourceInfo buildContext = config.getResourceInfo(sourceFile.getFullPath().removeLastSegments(1), false);
    //            flags.addAll(Arrays.asList(myTool.getToolCommandFlags(autoBuildConfData, sourceFile, outputFile)));
    //
    //            myTool.getInputTypes();
    //        } catch (BuildException e) {
    //            e.printStackTrace();
    //        }
    //        return flags;
    //    }

    private String expandCommandLinePattern(AutoBuildConfigurationDescription autoBuildConfData, Set<String> flags,
            String outputFlag, String outputName, Set<String> inputResources) {
        String cmd = myTool.getToolCommand();
        // try to resolve the build macros in the tool command
        String resolvedCommand = resolve(cmd, EMPTY_STRING, WHITESPACE, autoBuildConfData);
        if (resolvedCommand != null && (resolvedCommand = resolvedCommand.trim()).length() > 0)
            cmd = resolvedCommand;
        return expandCommandLinePattern(cmd, flags, outputFlag, outputName, inputResources,
                getToolCommandLinePattern(autoBuildConfData, myTool));
    }

    private static String expandCommandLinePattern(String commandName, Set<String> flags, String outputFlag,
            String outputName, Set<String> inputResources, String commandLinePattern) {

        String command = commandLinePattern;
        if (commandLinePattern == null || commandLinePattern.length() <= 0) {
            command = DEFAULT_PATTERN;
        }

        String quotedOutputName = outputName;
        // if the output name isn't a variable then quote it
        if (quotedOutputName.length() > 0 && quotedOutputName.indexOf("$(") != 0) { //$NON-NLS-1$
            quotedOutputName = DOUBLE_QUOTE + quotedOutputName + DOUBLE_QUOTE;
        }

        String inputsStr = ""; //$NON-NLS-1$
        if (inputResources != null) {
            for (String inp : inputResources) {
                if (inp != null && !inp.isEmpty()) {
                    // if the input resource isn't a variable then quote it
                    if (inp.indexOf("$(") != 0) { //$NON-NLS-1$
                        inp = DOUBLE_QUOTE + inp + DOUBLE_QUOTE;
                    }
                    inputsStr = inputsStr + inp + WHITESPACE;
                }
            }
            inputsStr = inputsStr.trim();
        }

        String flagsStr = String.join(WHITESPACE, flags);

        command = command.replace(makeVariable(CMD_LINE_PRM_NAME), commandName);
        command = command.replace(makeVariable(FLAGS_PRM_NAME), flagsStr);
        command = command.replace(makeVariable(OUTPUT_FLAG_PRM_NAME), outputFlag);
        // command = command.replace(makeVariable(OUTPUT_PREFIX_PRM_NAME), myTool.getOutputPrefix());
        command = command.replace(makeVariable(OUTPUT_PRM_NAME), quotedOutputName);
        command = command.replace(makeVariable(INPUTS_PRM_NAME), inputsStr);

        return command;
    }

    public void addPrerequisites(IInputType inputType, Set<IFile> files) {
        Set<IFile> entrypoint = myPrerequisites.get(inputType);
        if (entrypoint != null) {
            entrypoint.addAll(files);
        } else {
            Set<IFile> copyOfFiles = new HashSet<>();
            copyOfFiles.addAll(files);
            myPrerequisites.put(inputType, copyOfFiles);
        }
    }

    /**
     * A simple rule is a rule that takes exactly 1 input type
     * and exactly 1 output type containing exactly 1 file
     * 
     * @return true if this rule is a simple rule
     *         otherwise false
     */

    public boolean isSimpleRule() {
        //TOFIX 2 times the same test with an or???
        if ((myTargets.size() != 1) || (myTargets.size() != 1)) {
            return false;
        }
        //        int counter = 0;
        //        for (Set<IFile> files : myTargets.values()) {
        //            if ((++counter > 1) || (files.size() != 1)) {
        //                return false;
        //            }
        //        }
        //        counter = 0;
        //        for (Set<IFile> files : myPrerequisites.values()) {
        //            if ((++counter > 1)) {
        //                return false;
        //            }
        //        }
        return true;

    }

    public boolean isTool(ITool targetTool) {
        return myTool.getName().equals(targetTool.getName());
    }

    public int getSequenceGroupID() {
        return mySequenceGroupID;
    }

    public void setSequenceGroupID(int mySequenceGroupID) {
        this.mySequenceGroupID = mySequenceGroupID;
    }

    public boolean isForFolder(IFolder folder) {
        for (Set<IFile> files : myPrerequisites.values()) {
            for (IFile file : files) {
                if (file.getParent().equals(folder)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean needsExecuting(IFolder buildfolder) {
        Set<IFile> dependencyFiles = new HashSet<>();
        //check whether all targets exists
        //also get the timestamp of the oldest target
        long jongestTargetTimeStamp = Long.MAX_VALUE;
        for (Set<IFile> curTargetSet : myTargets.values()) {
            for (IFile curTarget : curTargetSet) {
                if (!curTarget.exists()) {
                    return true;
                }
                //could be that a refresh is needed due to cached local time stamp
                jongestTargetTimeStamp = Math.min(jongestTargetTimeStamp, curTarget.getLocalTimeStamp());
            }
        }
        //check wether all dependency files exists
        getDependencies();//TODO JABA this is very error prone.
        for (Set<IFile> curDependencySet : myDependencies.values()) {
            for (IFile curDependency : curDependencySet) {
                if (!curDependency.exists()) {
                    return true;
                }
                dependencyFiles.add(curDependency);
            }
        }
        //get the newest prerequisite timeStamp
        long oldestPreReqTimeStamp = Long.MIN_VALUE;
        for (Set<IFile> curPrereqSet : myPrerequisites.values()) {
            for (IFile curPrereq : curPrereqSet) {
                oldestPreReqTimeStamp = Math.max(oldestPreReqTimeStamp, curPrereq.getLocalTimeStamp());
            }
        }
        if (oldestPreReqTimeStamp > jongestTargetTimeStamp) {
            return true;
        }
        //get the newest dependency timeStamp
        long oldestDependencyTimeStamp = Long.MIN_VALUE;
        for (IFile curdepFile : dependencyFiles) {
            oldestDependencyTimeStamp = Math.max(oldestDependencyTimeStamp,
                    getDepFileTimeStamp(curdepFile, buildfolder));
        }
        if (oldestDependencyTimeStamp >= jongestTargetTimeStamp) {
            return true;
        }
        return false;
    }

    /**
     * given a dependency file; return the time stamp of the youngest file mentioned
     * in the dependency file
     * 
     * @param depFile
     *            the dependency file created by a compiler
     * 
     * @return the timestamp of the oldest file in the files; 0 if a referenced file
     *         does not exist
     */
    private static long getDepFileTimeStamp(IFile curdepFile, IFolder buildPath) {
        long newestTime = Long.MIN_VALUE;
        File depFile = curdepFile.getLocation().toFile();
        String prefix = curdepFile.getParent().getLocation().toString();
        try (BufferedReader reader = new BufferedReader(new FileReader(depFile));) {
            String curLine = null;
            while ((curLine = reader.readLine()) != null) {
                if (curLine.endsWith(COLON)) {
                    String headerName = curLine.substring(0, curLine.length() - 1).replace("\\ ", BLANK);
                    headerName = buildPath.getFile(headerName).getLocation().toString();
                    Path headerFile = Path.of(headerName);
                    BasicFileAttributes attr = Files.readAttributes(headerFile, BasicFileAttributes.class);
                    newestTime = Math.max(attr.lastModifiedTime().toMillis(), newestTime);
                }
            }
            reader.close();
        } catch (@SuppressWarnings("unused") IOException e) {
            //e.printStackTrace();
            return Long.MAX_VALUE;
        }
        return newestTime;
    }

}
//    /**
//     * Returns the dependency <code>IPath</code>s relative to the build directory
//     *
//     * @param depCalculator
//     *            the dependency calculator
//     * @return IPath[] that are relative to the build directory
//     */
//    private IPath[] calculateDependenciesForSource(ArduinoGnuMakefileGenerator caller,
//            IManagedDependencyCalculator depCalculator) {
//        IPath[] addlDeps = depCalculator.getDependencies();
//        if (addlDeps != null) {
//            for (int i = 0; i < addlDeps.length; i++) {
//                if (!addlDeps[i].isAbsolute()) {
//                    // Convert from project relative to build directory relative
//                    IPath absolutePath = caller.getProject().getLocation().append(addlDeps[i]);
//                    addlDeps[i] = ManagedBuildManager.calculateRelativePath(caller.getTopBuildDir().getLocation(),
//                            absolutePath);
//                }
//            }
//        }
//        return addlDeps;
//    }
