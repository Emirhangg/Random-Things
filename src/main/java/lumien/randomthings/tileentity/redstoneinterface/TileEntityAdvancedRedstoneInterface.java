package lumien.randomthings.tileentity.redstoneinterface;

import java.util.HashSet;
import java.util.Set;

import lumien.randomthings.item.ItemPositionFilter;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.InventoryUtil;
import lumien.randomthings.util.NBTUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

public class TileEntityAdvancedRedstoneInterface extends TileEntityRedstoneInterface implements IInventoryChangedListener
{
	InventoryBasic positionInventory = new InventoryBasic("Advanced Redstone Interface", false, 9);

	HashSet<BlockPos> targets;

	public TileEntityAdvancedRedstoneInterface()
	{
		targets = new HashSet<>();
		positionInventory.addInventoryChangeListener(this);
	}

	public Set<BlockPos> getTargets()
	{
		return targets;
	}

	@Override
	protected boolean isTargeting(BlockPos pos)
	{
		return targets.contains(pos);
	}

	@Override
	public void writeDataToNBT(NBTTagCompound compound, boolean sync)
	{
		NBTTagList nbtTargetList = new NBTTagList();

		synchronized (TileEntityRedstoneInterface.lock)
		{
			for (BlockPos pos : targets)
			{
				NBTTagCompound targetCompound = new NBTTagCompound();

				NBTUtil.writeBlockPosToNBT(targetCompound, "target", pos);
				nbtTargetList.appendTag(targetCompound);
			}
		}

		compound.setTag("targets", nbtTargetList);

		NBTTagCompound inventoryCompound = new NBTTagCompound();
		InventoryUtil.writeInventoryToCompound(inventoryCompound, positionInventory);
		compound.setTag("inventory", inventoryCompound);
	}

	@Override
	public void readDataFromNBT(NBTTagCompound compound, boolean sync)
	{
		NBTTagList nbtTargetList = compound.getTagList("targets", 10);

		synchronized (TileEntityRedstoneInterface.lock)
		{
			if (nbtTargetList != null)
			{
				for (int i = 0; i < nbtTargetList.tagCount(); i++)
				{
					NBTTagCompound targetCompound = nbtTargetList.getCompoundTagAt(i);

					this.targets.add(NBTUtil.readBlockPosFromNBT(targetCompound, "target"));
				}
			}
		}

		NBTTagCompound inventoryCompound = compound.getCompoundTag("inventory");

		if (inventoryCompound != null)
		{
			InventoryUtil.readInventoryFromCompound(inventoryCompound, positionInventory);
		}
	}

	@Override
	public void onInventoryChanged(IInventory inventory)
	{
		if (this.world != null && this.pos != null)
		{
			HashSet<BlockPos> newTargets = new HashSet<>();

			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				ItemStack stack = inventory.getStackInSlot(i);

				BlockPos target;
				if (!stack.isEmpty() && stack.getItem() == ModItems.positionFilter && (target = ItemPositionFilter.getPosition(stack)) != null)
				{
					newTargets.add(target);
				}
			}

			HashSet<BlockPos> changedPositions = new HashSet<>();

			if (!this.world.isRemote)
			{
				synchronized (TileEntityRedstoneInterface.lock)
				{
					for (BlockPos target : newTargets)
					{
						if (!targets.contains(target))
						{
							changedPositions.add(target); // Added
						}
					}

					for (BlockPos oldTarget : targets)
					{
						if (!newTargets.contains(oldTarget))
						{
							changedPositions.add(oldTarget); // Added
						}
					}

					this.targets = newTargets;

					IBlockState state = this.world.getBlockState(this.pos);
					this.world.notifyBlockUpdate(pos, state, state, 3);

					for (BlockPos changedPos : changedPositions)
					{
						IBlockState targetState = world.getBlockState(changedPos);
						targetState.neighborChanged(world, changedPos, Blocks.REDSTONE_BLOCK, this.pos); // TODO
																											// DANGEROUS
						world.notifyNeighborsOfStateChange(changedPos, Blocks.REDSTONE_BLOCK, false);
					}
				}
			}
		}
	}

	public IInventory getTargetInventory()
	{
		return positionInventory;
	}

	@Override
	protected void notifyTargets(Block neighborBlock)
	{
		for (BlockPos target : targets)
		{
			if (target != null)
			{
				IBlockState targetState = world.getBlockState(target);
				targetState.neighborChanged(world, target, neighborBlock, this.pos);
				world.notifyNeighborsOfStateChange(target, neighborBlock, false);
			}
		}
	}
}
