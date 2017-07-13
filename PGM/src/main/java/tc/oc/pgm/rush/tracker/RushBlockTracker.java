package tc.oc.pgm.rush.tracker;

import java.util.Optional;
import java.util.Set;

import org.bukkit.block.BlockState;

import com.google.common.collect.Sets;

public class RushBlockTracker {

    private final Set<RushTrackedBlock> blocks = Sets.newHashSet();

    protected void onBlockPlace(BlockState blockState) {
        RushTrackedBlock trackedBlock = getBlock(blockState);
        trackedBlock.setCurrentMaterial(blockState);
        blocks.add(trackedBlock);
    }

    public void renewAll() {
        renewAll(true);
    }

    public void renewAll(boolean clear) {
        blocks.forEach(RushTrackedBlock::renew);

        if (clear) {
            blocks.clear();
        }
    }

    protected RushTrackedBlock getBlock(BlockState blockState) {
        return block(blockState).orElse(new RushTrackedBlock(blockState));
    }

    protected Optional<RushTrackedBlock> block(BlockState blockState) {
        return blocks.stream().filter(trackedBlock -> trackedBlock.equal(blockState)).findAny();
    }
}
