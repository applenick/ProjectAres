package tc.oc.commons.bukkit.sparklings;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.permissions.PermissionBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class SparklingManifest extends HybridManifest {
    @Override
    protected void configure() {
        requestStaticInjection(SparklingUtil.class);

        new PluginFacetBinder(binder())
            .register(RaindropCommands.class);

        final PermissionBinder permissions = new PermissionBinder(binder());
        for(int i = SparklingConstants.MULTIPLIER_MAX; i > 0; i = i - SparklingConstants.MULTIPLIER_INCREMENT) {
            permissions.bindPermission().toInstance(new Permission("raindrops.multiplier." + i, PermissionDefault.FALSE));
        }
    }
}
