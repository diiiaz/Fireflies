package io.github.diiiaz.fireflies.item.custom;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public interface GlassBottleItemCustomInterface {

    default ActionResult useOnBlock(ItemUsageContext context) { return ActionResult.PASS; }
}
