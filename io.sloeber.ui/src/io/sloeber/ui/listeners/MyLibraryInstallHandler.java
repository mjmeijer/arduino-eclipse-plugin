package io.sloeber.ui.listeners;

import java.util.Map;

import io.sloeber.core.api.IInstallLibraryHandler;
import io.sloeber.core.api.Json.library.LibraryJson;
import io.sloeber.ui.helpers.MyPreferences;

public class MyLibraryInstallHandler implements IInstallLibraryHandler {

	@Override
	public boolean autoInstall() {
		return MyPreferences.getAutomaticallyInstallLibrariesOption();
	}

	@Override
	public Map<String, LibraryJson> selectLibrariesToInstall(Map<String, LibraryJson> proposedLibsToInstall) {
		return proposedLibsToInstall;
	}

}
