package me.pseudoknight.chnaughty;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

@MSExtension("CHNaughty")
public class CHNaughty extends AbstractExtension {

	public Version getVersion() {
		return new SimpleVersion(3,11,0);
	}

	@Override
	public void onStartup() {
		System.out.println("CHNaughty " + getVersion() + " loaded.");
	}

	@Override
	public void onShutdown() {
		System.out.println("CHNaughty " + getVersion() + " unloaded.");
	}

}
