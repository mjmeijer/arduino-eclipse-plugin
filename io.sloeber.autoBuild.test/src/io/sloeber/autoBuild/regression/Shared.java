package io.sloeber.autoBuild.regression;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

public class Shared {
	private static boolean deleteProjects = true;
	private static boolean closeFailedProjects = false;

	public static void setDeleteProjects(boolean deleteProjects) {
		Shared.deleteProjects = deleteProjects;
	}

	public static void setCloseFailedProjects(boolean closeFailedProjects) {
		Shared.closeFailedProjects = closeFailedProjects;
	}

	public static boolean isCloseFailedProjects() {
		return closeFailedProjects;
	}

	public static boolean hasBuildErrors(IProject project) throws CoreException {
		IMarker[] markers = project.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		for (IMarker marker : markers) {
			if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR) {
				return true;
			}
		}
		CCorePlugin cCorePlugin = CCorePlugin.getDefault();
		ICProjectDescription prjCDesc = cCorePlugin.getProjectDescription(project);
		ICConfigurationDescription activeConfig = prjCDesc.getActiveConfiguration();

		IPath resultPath = project.getLocation().append(activeConfig.getName());
		String projName = project.getName();
		String[] validOutputss = { projName + ".elf", projName + ".bin", projName + ".hex", projName + ".exe",
				"application.axf" };
		for (String validOutput : validOutputss) {
			File validFile = resultPath.append(validOutput).toFile();
			if (validFile.exists()) {
				return false;
			}
		}

		return true;
	}

	public static void waitForAllJobsToFinish() {
		try {
			Thread.sleep(1000);
			IJobManager jobMan = Job.getJobManager();
			while (!(jobMan.isIdle())) {
				Thread.sleep(500);
				// If you do not get out of this loop it probably means you are
				// runnning the test in the gui thread
			}
			// As nothing is running now we can start installing
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("can not find installerjob");
		}
	}

	public static IPath getTemplateFolder(String templateName) throws Exception {
		Bundle bundle = Platform.getBundle("io.sloeber.tests");
		Path path = new Path("src/templates/" + templateName);
		URL fileURL = FileLocator.find(bundle, path, null);
		URL resolvedFileURL = FileLocator.toFileURL(fileURL);
		return new Path(resolvedFileURL.toURI().getPath());
	}

	public static IPath getprojectZip(String zipFileName) throws Exception {
		Bundle bundle = Platform.getBundle("io.sloeber.tests");
		Path path = new Path("src/projects/" + zipFileName);
		URL fileURL = FileLocator.find(bundle, path, null);
		URL resolvedFileURL = FileLocator.toFileURL(fileURL);
		return new Path(resolvedFileURL.toURI().getPath());
	}

	/**
	 * Convenience method to call BuildAndVerify with default project name and null
	 * as compile options
	 *
	 * @param boardDescriptor
	 * @param codeDescriptor
	 * @param compileOptions  can be null
	 * @return true if build is successful otherwise false
	 * @throws CoreException 
	 * @throws Exception 
	 */
	public static boolean BuildAndVerify(IProject theTestProject ) throws Exception, Exception {

		NullProgressMonitor monitor = new NullProgressMonitor();

		waitForAllJobsToFinish(); // for the indexer
		theTestProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
		if (hasBuildErrors(theTestProject)) {
			waitForAllJobsToFinish(); // for the indexer
			Thread.sleep(2000);
			theTestProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
			if (hasBuildErrors(theTestProject)) {
				waitForAllJobsToFinish(); // for the indexer
				Thread.sleep(2000);
				theTestProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				if (hasBuildErrors(theTestProject)) {
					if (closeFailedProjects) {
						theTestProject.close(null);
					}
					return false;
				}
			}
		}
		try {
			if (deleteProjects) {
				theTestProject.delete(true, true, null);
			} else {
				theTestProject.close(null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	// copied from
	// https://www.codejava.net/java-se/file-io/programmatically-extract-a-zip-file-using-java

	// /**
	// * This utility extracts files and directories of a standard zip file to
	// * a destination directory.
	// * @author www.codejava.net
	// *
	// */
	// public class UnzipUtility {
	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 * 
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	public static void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdirs();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}
	// }
	// end copy from
	// https://www.codejava.net/java-se/file-io/programmatically-extract-a-zip-file-using-java
}
