package wirezat.realisticores;

import net.fabricmc.api.ClientModInitializer;

public class RealisticOresClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ModItemGroups.registerModItemGroups();
	}
}