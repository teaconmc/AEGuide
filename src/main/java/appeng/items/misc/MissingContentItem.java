package appeng.items.misc;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import appeng.api.stacks.AEKeyType;

public class MissingContentItem extends Item {
    public MissingContentItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag advanced) {
        super.appendHoverText(stack, context, lines, advanced);
    }

    public record BrokenStackInfo(Component displayName, @Nullable AEKeyType keyType, long amount) {
    }
}
