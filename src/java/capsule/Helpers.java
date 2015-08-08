package capsule;

import java.util.List;

import com.google.common.base.Predicate;

import capsule.enchantments.Enchantments;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

public class Helpers {
	
	public static void swapRegions( WorldServer sourceWorld, WorldServer destWorld, BlockPos srcOriginPos, BlockPos destOriginPos, int size)
	{
		
		// 1st copy to dest world
		for (int y = size - 1; y >= 0; y--) {
			for (int x = 0; x < size; x++) {
				for (int z = 0; z < size; z++) {
					
					BlockPos srcPos = srcOriginPos.add(x,y,z);
					BlockPos destPos = destOriginPos.add(x,y,z);
					
					TileEntity srcTE = sourceWorld.getTileEntity(srcPos);
					IBlockState srcState = sourceWorld.getBlockState(srcPos);
					
					// store the current block
					destWorld.setBlockState(destPos, srcState, 4);
					TileEntity destTE = destWorld.getTileEntity(destPos);
					
					if(srcTE != null && destTE != null){
						NBTTagCompound nbt = new NBTTagCompound();
						srcTE.setPos(destPos);
						srcTE.setWorldObj(destWorld);
						srcTE.writeToNBT(nbt);
						destTE.readFromNBT(nbt);
						
						// reset
						sourceWorld.removeTileEntity(srcPos);
					}
					sourceWorld.setBlockState(srcPos, Blocks.air.getDefaultState(), 4);
				}
			}
		}
		
		for (int y = size - 1; y >= 0; y--) {
			for (int x = 0; x < size; x++) {
				for (int z = 0; z < size; z++) {
					
					BlockPos srcPos = srcOriginPos.add(x,y,z);
					BlockPos destPos = destOriginPos.add(x,y,z);
					
					sourceWorld.markBlockForUpdate(srcPos);
					destWorld.markBlockForUpdate(destPos);
					
				}
			}
		}


	}

	public static void teleportBlock(WorldServer sourceWorld, WorldServer destWorld, BlockPos srcPos, BlockPos destPos) {
		TileEntity srcTE = sourceWorld.getTileEntity(srcPos);
		IBlockState srcState = sourceWorld.getBlockState(srcPos);
		
		// store the current block
		destWorld.setBlockState(destPos, srcState);
		TileEntity destTE = destWorld.getTileEntity(destPos);
		
		if(srcTE != null && destTE != null){
			NBTTagCompound nbt = new NBTTagCompound();
			srcTE.setPos(destPos);
			srcTE.setWorldObj(destWorld);
			srcTE.writeToNBT(nbt);
			destTE.readFromNBT(nbt);
		}
		
		// remove from the world the stored block
		sourceWorld.removeTileEntity(srcPos);
		sourceWorld.setBlockState(srcPos, Blocks.air.getDefaultState());
		
		//destWorld.markBlockForUpdate(destPos);
		//sourceWorld.markBlockForUpdate(srcPos);
	}
	
	@SuppressWarnings({ "unchecked" })
	public static BlockPos findBottomBlock(EntityItem entityItem, List<Block> excludedBlocks) {
		if(entityItem.getEntityWorld() == null) return null;
		
		double i = entityItem.posX;
		double j = entityItem.posY;
		double k = entityItem.posZ;

        Iterable<BlockPos> blockPoss = BlockPos.getAllInBox(new BlockPos(i, j - 1, k), new BlockPos(i + 1, j + 1, k + 1));
        BlockPos closest = null;
        double closestDistance = 1000;
        for( BlockPos pos : blockPoss) {
        	Block block = entityItem.worldObj.getBlockState(new BlockPos(i, j - 1, k)).getBlock();
        	double distance = pos.distanceSqToCenter(i, j, k);
        	if (!excludedBlocks.contains(block) &&  distance < closestDistance) {
        		closest = pos;
            	closestDistance = distance;
            }
        }
        
		return closest;
	}
	
	
	public static BlockPos findClosestBlock(EntityItem entityItem, List<Block> excludedBlocks) {
		if(entityItem.getEntityWorld() == null) return null;
		
		double i = entityItem.posX;
		double j = entityItem.posY;
		double k = entityItem.posZ;

        @SuppressWarnings("unchecked")
		Iterable<BlockPos> blockPoss = BlockPos.getAllInBox(new BlockPos(i - 1, j - 1, k - 1), new BlockPos(i + 1, j + 1, k + 1));
        BlockPos closest = null;
        double closestDistance = 1000;
        for( BlockPos pos : blockPoss) {
        	Block block = entityItem.worldObj.getBlockState(pos).getBlock();
        	double distance = pos.distanceSqToCenter(i, j, k);
        	if (!excludedBlocks.contains(block) &&  distance < closestDistance) {
        		closest = pos;
            	closestDistance = distance;
            }
        }
        
		return closest;
	}
	

	public static BlockPos findSpecificBlock(EntityItem entityItem, int maxRange, Class searchedBlock) {
		if(entityItem.getEntityWorld() == null || searchedBlock == null) return null;
		
		double i = entityItem.posX;
		double j = entityItem.posY;
		double k = entityItem.posZ;
		
		for(int range = 1; range < maxRange; range ++){
			@SuppressWarnings("unchecked")
			Iterable<BlockPos> blockPoss = BlockPos.getAllInBoxMutable(new BlockPos(i - range, j - range, k - range), new BlockPos(i + range, j + range, k + range));
			for( BlockPos pos : blockPoss) {
	        	Block block = entityItem.worldObj.getBlockState(pos).getBlock();
	        	if(block.getClass().equals(searchedBlock)){
	        		return pos.add(0,0,0); // return a copy
	        	}
	        }
		}
		
		
		return null;
	}
	
	
	
	/*
	 * Color stuff
	 */
	
	/**
     * Return whether the specified armor has a color.
     */
    public static boolean hasColor(ItemStack stack)
    {
        return (!stack.hasTagCompound() ? false : (!stack.getTagCompound().hasKey("display", 10) ? false : stack.getTagCompound().getCompoundTag("display").hasKey("color", 3)));
    }

    /**
     * Return the color for the specified ItemStack.
     */
    public static int getColor(ItemStack stack)
    {
        NBTTagCompound nbttagcompound = stack.getTagCompound();

        if (nbttagcompound != null)
        {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

            if (nbttagcompound1 != null && nbttagcompound1.hasKey("color", 3))
            {
                return nbttagcompound1.getInteger("color");
            }
        }

        return 0xFFFFFF;
    }

    /**
     * Remove the color from the specified ItemStack.
     */
    public static void removeColor(ItemStack stack)
    {
        NBTTagCompound nbttagcompound = stack.getTagCompound();

        if (nbttagcompound != null)
        {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

            if (nbttagcompound1.hasKey("color"))
            {
                nbttagcompound1.removeTag("color");
            }
        }
    }

    /**
     * Sets the color of the specified ItemStack
     */
    public static void setColor(ItemStack stack, int color)
    {
        NBTTagCompound nbttagcompound = stack.getTagCompound();

        if (nbttagcompound == null)
        {
            nbttagcompound = new NBTTagCompound();
            stack.setTagCompound(nbttagcompound);
        }

        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

        if (!nbttagcompound.hasKey("display", 10))
        {
            nbttagcompound.setTag("display", nbttagcompound1);
        }

        nbttagcompound1.setInteger("color", color);
    }
    
    public static int getStoredEnchantmentLevel(int enchID, ItemStack stack)
    {
        if (stack == null || !(stack.getItem() instanceof ItemEnchantedBook))
        {
            return 0;
        }
        else
        {
            NBTTagList nbttaglist = ((ItemEnchantedBook)stack.getItem()).getEnchantments(stack);

            if (nbttaglist == null)
            {
                return 0;
            }
            else
            {
                for (int j = 0; j < nbttaglist.tagCount(); ++j)
                {
                    short short1 = nbttaglist.getCompoundTagAt(j).getShort("id");
                    short short2 = nbttaglist.getCompoundTagAt(j).getShort("lvl");

                    if (short1 == enchID)
                    {
                        return short2;
                    }
                }

                return 0;
            }
        }
    }
    
    public static final Predicate hasRecallEnchant = new Predicate()
    {
        public boolean apply(Entity entityIn)
        {
            return entityIn instanceof EntityItem && EnchantmentHelper.getEnchantmentLevel(Enchantments.recallEnchant.effectId, ((EntityItem)entityIn).getEntityItem()) > 0;
        }
        public boolean apply(Object obj)
        {
            return this.apply((Entity)obj);
        }
    };

}