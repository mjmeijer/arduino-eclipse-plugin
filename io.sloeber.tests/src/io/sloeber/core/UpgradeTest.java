package io.sloeber.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import io.sloeber.core.api.ConfigurationPreferences;
import io.sloeber.core.api.SloeberProject;
import io.sloeber.providers.Arduino;

/**
 * this test assumes it is stared in a clean workspace and not in the UI thread
 *
 * this test copies previous versions sloeber projects into the new workspace
 * opens them and builds them if the build is successful the test is considered
 * successful
 *
 * @author jan
 *
 */
@SuppressWarnings({ "nls", "static-method" })
public class UpgradeTest {
    /**
     * Upgrade a single config project
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setup() {
        // stop bonjour as it clutters the console log
    	ConfigurationPreferences.setUseBonjour(false);
        Shared.waitForAllJobsToFinish();
        // TOFIX: this will have to change into a specific version
        // or we will have to add the install based on stored data
        Arduino.installLatestAVRBoards();
        Shared.waitForAllJobsToFinish();
    }

    @Test
    public void upgradeSingleConfigProjectFromVersion4_3_3() throws Exception {
        /*
         * A arduino uno project is upgraded that need the extra compile option extraCPP
         */
        String projectName = "upgradeSingleConfigProject";
        String inputZipFile = Shared.getprojectZip("upgradeSingleConfigProject4_3_3.zip").toOSString();
        // /io.sloeber.tests/src/projects/upgradeSingleConfigProject4_3_3.zip
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject theTestProject = root.getProject(projectName);
        Shared.unzip(inputZipFile, root.getLocation().toOSString());
        theTestProject.create(null);
        theTestProject.open(null);
        Shared.waitForAllJobsToFinish(); // for the indexer
        theTestProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertNotNull("The project has been automagically upgraded:" + projectName, Shared.hasBuildErrors(theTestProject));
        //try to convert the project
        SloeberProject.convertToArduinoProject(theTestProject, null);
        Shared.waitForAllJobsToFinish(); // for the indexer
        theTestProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertNull("Failed to compile the upgraded project:" + projectName, Shared.hasBuildErrors(theTestProject));
    }

    /**
     * Upgrade a triple dual project
     *
     * @throws Exception
     */
    @Test
    public void upgradeDualConfigProject() throws Exception {

    }

    /**
     * Upgrade a triple config project
     *
     * @throws Exception
     */
    @Test
    public void upgradeTripleConfigProject() throws Exception {

    }

}
