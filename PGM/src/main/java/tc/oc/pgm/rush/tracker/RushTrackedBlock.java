package tc.oc.pgm.rush.tracker;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;

import com.google.api.client.repackaged.com.google.common.base.Objects;

public class RushTrackedBlock {

    private final Location location;
    private final MaterialData material;
    private MaterialData currentMaterial;

    public RushTrackedBlock(BlockState blockState) {
        this.location = blockState.getLocation();
        this.material = blockState.getMaterialData();
    }

    public MaterialData getCurrentMaterial() {
        return currentMaterial;
    }

    public void setCurrentMaterial(Block block) {
        setCurrentMaterial(block.getState());
    }

    public void setCurrentMaterial(BlockState state) {
        setCurrentMaterial(state.getData());
    }

    public void setCurrentMaterial(MaterialData material) {
        this.currentMaterial = material;
        getBlock().setTypeIdAndData(material.getItemTypeId(), material.getData(), false);
    }

    public Location getLocation() {
        return location;
    }

    public MaterialData getMaterial() {
        return material;
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public BlockState getBlockState() {
        return getBlock().getState();
    }

    public void renew() {
        setCurrentMaterial(material);
    }

    public boolean equal(BlockState blockState) {
        return Objects.equal(location, blockState.getLocation());
    }
}
