package com.williambl.bigbuckets;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;

public class BigBucketItem extends Item {

    public BigBucketItem() {
        super();
        this.setCreativeTab(CreativeTabs.MISC);
        this.setMaxStackSize(1);
    }

    protected ItemStack emptyBucket(ItemStack stack, EntityPlayer player) {
        int fullness = getFullness(stack);

        if (fullness - 1 >= 0)
            setFullness(stack, fullness - 1);
        if (fullness - 1 == 0)
            setFluid(stack, null);
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        boolean empty = this.getFullness(playerIn.getHeldItem(handIn)) == 0 || this.getFluid(playerIn.getHeldItem(handIn)) == null;
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);
        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onBucketUse(playerIn, worldIn, itemstack, raytraceresult);
        if (ret != null) return ret;

        if (raytraceresult == null || raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        } else {
            BlockPos blockpos = raytraceresult.getBlockPos();

            if (!worldIn.isBlockModifiable(playerIn, blockpos)) {
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
            } else if (empty || FluidUtil.getFluidHandler(worldIn, blockpos, raytraceresult.sideHit) != null) {
                if (!playerIn.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                } else {
                    playerIn.addStat(StatList.getObjectUseStats(this));
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, this.fillBucket(itemstack, playerIn, blockpos, worldIn, raytraceresult.sideHit));
                }
            } else {
                boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
                BlockPos blockpos1 = flag1 && raytraceresult.sideHit == EnumFacing.UP ? blockpos : blockpos.offset(raytraceresult.sideHit);

                if (!playerIn.canPlayerEdit(blockpos1, raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                } else if (this.tryPlaceContainedLiquid(playerIn, worldIn, blockpos1, itemstack)) {
                    if (playerIn instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) playerIn, blockpos1, itemstack);
                    }

                    playerIn.addStat(StatList.getObjectUseStats(this));
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                } else {
                        playerIn.addStat(StatList.getObjectUseStats(this));
                        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, this.fillBucket(itemstack, playerIn, blockpos, worldIn, raytraceresult.sideHit));
                }
            }
        }
    }

    private ItemStack fillBucket(ItemStack stack, EntityPlayer player, BlockPos fluidpos, World world, EnumFacing side) {
        int capacity = getCapacity(stack);
        int fullness = getFullness(stack);
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidpos, side);
        if (handler != null) {
            FluidStack fluidStack = handler.drain(Fluid.BUCKET_VOLUME, false);
            if (fluidStack != null) {
                if (fullness == 0) {
                    setFluid(stack, handler.drain(Fluid.BUCKET_VOLUME, true));
                } else if (fluidStack.isFluidEqual(getFluid(stack))) {
                    if (fullness < capacity) {
                        setFullness(stack, fullness + handler.drain(Fluid.BUCKET_VOLUME, true).amount/Fluid.BUCKET_VOLUME);
                    }
                }
            }
        }
        return stack;
    }

    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn, ItemStack stack) {
        if (this.getFluid(stack) == null) {
            return false;
        } else {
            IBlockState iblockstate = worldIn.getBlockState(posIn);
            Material material = iblockstate.getMaterial();
            boolean flag = !material.isSolid();
            boolean flag1 = iblockstate.getBlock().isReplaceable(worldIn, posIn);

            if (!worldIn.isAirBlock(posIn) && !flag && !flag1) {
                return false;
            } else {
                FluidActionResult fluidResult = FluidUtil.tryPlaceFluid(player, worldIn, posIn, FluidUtil.getFilledBucket(this.getFluid(stack)), this.getFluid(stack));
                if (fluidResult.isSuccess()) {
                    setFullness(stack, getFullness(stack) - 1);
                    return true;
                }
                return false;
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        TextComponentTranslation fluidName;
        if (getFluid(stack) != null)
            fluidName = new TextComponentTranslation(getFluid(stack).getUnlocalizedName());
        else
            fluidName = new TextComponentTranslation("fluid.none");
        tooltip.add(new TextComponentString("Fluid: ").appendSibling(new TextComponentString(fluidName.getUnformattedComponentText())).getUnformattedComponentText());
        tooltip.add("Capacity: " + getCapacity(stack));
        tooltip.add("Fullness: " + getFullness(stack));
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (getFluid(stack) == null)
            return super.getItemStackDisplayName(stack);
        TextComponentTranslation fluidName;
        if (getFluid(stack) != null)
            fluidName = new TextComponentTranslation(getFluid(stack).getUnlocalizedName());
        else
            fluidName = new TextComponentTranslation("fluid.none");
        return super.getItemStackDisplayName(stack) + "(" + fluidName.getUnformattedComponentText() + ")";
    }

    public FluidStack getFluid(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("BigBuckets");

        return FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("FluidStack"));
    }

    public int getCapacity(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("BigBuckets");
        return tag.getInteger("Capacity");
    }

    public int getFullness(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("BigBuckets");
        FluidStack fluidStack = getFluid(stack);
        if (fluidStack == null)
            return 0;
        else
            return fluidStack.amount/Fluid.BUCKET_VOLUME;
    }

    public void setFluid(ItemStack stack, @Nullable FluidStack fluid) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("BigBuckets");
        tag.setTag("FluidStack", fluid.writeToNBT(tag.getCompoundTag("FluidStack")));
    }

    public void setCapacity(ItemStack stack, int capacity) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("BigBuckets");
        tag.setInteger("Capacity", capacity);
    }

    public void setFullness(ItemStack stack, int fullness) {
        NBTTagCompound tag = stack.getOrCreateSubCompound("BigBuckets");
        FluidStack fluidStack = getFluid(stack);
        if (fluidStack != null) {
            fluidStack.amount = fullness*Fluid.BUCKET_VOLUME;
            setFluid(stack, fluidStack);
        }
    }

    public boolean canAcceptFluid(ItemStack stack, Fluid fluid) {
        return getFullness(stack) != getCapacity(stack) && (getFluid(stack).getFluid() == fluid || getFluid(stack) == null);
    }
}
