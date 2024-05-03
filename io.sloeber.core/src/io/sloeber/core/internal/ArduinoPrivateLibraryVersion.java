package io.sloeber.core.internal;

import static io.sloeber.core.api.Const.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import io.sloeber.core.api.IArduinoLibraryVersion;
import io.sloeber.core.common.InstancePreferences;

public class ArduinoPrivateLibraryVersion implements IArduinoLibraryVersion {
	private IPath myInstallPath;
	private String myName;
	private IPath myFQN;

	public ArduinoPrivateLibraryVersion(IPath installPath) {
		myInstallPath = installPath;
		myName = myInstallPath.lastSegment();
		calcFQN();
	}

	public ArduinoPrivateLibraryVersion(String curSaveString) {
		String[] parts=curSaveString.split(SEMI_COLON);
		myName=parts[parts.length-1];
		calcFQN();
		String privateLibPaths[] = InstancePreferences.getPrivateLibraryPaths();
		for (String curLibPath : privateLibPaths) {
			Path curPrivPath=new Path(curLibPath);
			if(curPrivPath.append(myName).toFile().exists()) {
				myInstallPath=curPrivPath.append(myName);
				return;
			}
		}
		//This should not happen
	}

	@Override
	public String getName() {
		return myName;
	}

	@Override
	public IPath getInstallPath() {
		return myInstallPath;
	}

	@Override
	public boolean isHardwareLib() {
		return false;
	}

	@Override
	public boolean isPrivateLib() {
		return true;
	}

	@Override
	public IPath getExamplePath() {
		IPath Lib_examples = getInstallPath().append(eXAMPLES_FODER);
		if (Lib_examples.toFile().exists()) {
			return Lib_examples;
		}
		return getInstallPath().append(EXAMPLES_FOLDER);
	}

	private void calcFQN() {
		myFQN=  Path.fromPortableString(SLOEBER_LIBRARY_FQN);
		myFQN= myFQN.append(PRIVATE).append(getName());
	}

	@Override
	public String[] getBreadCrumbs() {
		return myFQN.segments();
	}

	@Override
	public IPath getFQN() {
		return myFQN;
	}

}
