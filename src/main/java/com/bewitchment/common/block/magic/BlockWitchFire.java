package com.bewitchment.common.block.magic;

import java.util.Arrays;
import java.util.Random;

import com.bewitchment.api.transformation.DefaultTransformations;
import com.bewitchment.common.Bewitchment;
import com.bewitchment.common.block.BlockMod;
import com.bewitchment.common.core.capability.transformation.CapabilityTransformationData;
import com.bewitchment.common.core.net.NetworkHandler;
import com.bewitchment.common.core.net.messages.WitchFireTP;
import com.bewitchment.common.item.ModItems;
import com.bewitchment.common.lib.LibBlockName;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockWitchFire extends BlockMod {
	
	public static final PropertyEnum<EnumFireType> TYPE = new PropertyEnum<EnumFireType>("type", EnumFireType.class, Arrays.asList(EnumFireType.values())) {};
	
	public BlockWitchFire() {
		super(LibBlockName.WITCHFIRE, Material.FIRE);
		setTickRandomly(false);
		MinecraftForge.EVENT_BUS.register(this);
		this.setDefaultState(blockState.getBaseState().withProperty(TYPE, EnumFireType.NORMAL));
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		final AxisAlignedBB FLAT_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0025D, 1.0D);
		return FLAT_AABB;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 0;
	}
	
	@Override
	public boolean isCollidable() {
		return true;
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(TYPE).getLight();
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(TYPE, EnumFireType.values()[meta]);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.scheduleBlockUpdate(pos, state.getBlock(), 1, 10);
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			world.scheduleBlockUpdate(pos, state.getBlock(), 1, 1);
			
			AxisAlignedBB aa = new AxisAlignedBB(pos);
			world.getEntitiesWithinAABB(EntityPlayer.class, aa).parallelStream()
				.filter(p -> p.getCapability(CapabilityTransformationData.CAPABILITY, null).getType()==DefaultTransformations.VAMPIRE)
				.forEach(p -> p.attackEntityFrom(DamageSource.IN_FIRE, 1f));
			switch (state.getValue(TYPE)) {
				case NORMAL:
					world.getEntitiesWithinAABB(EntityItem.class, aa).parallelStream()
							.forEach(i -> itemInFire(i, world, pos, state));
					if (world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN) {
						world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
					}
					break;
				case FROSTFIRE:
					world.getEntitiesWithinAABB(EntityItem.class, aa).parallelStream()
						.filter(i -> Block.getBlockFromItem(i.getItem().getItem())==Blocks.IRON_ORE)
						.forEach(i -> spawnColdIron(world, rand, i));
					break;
				default:
					break;
			}
		}
	}
	
	private void itemInFire(EntityItem i, World world, BlockPos pos, IBlockState state) {
		if (isMundane(i)) {
			i.setDead();
		} else {
			for (EnumFireType ft : EnumFireType.values()) {
				if (ft.isIngredient(i.getItem())) {
					world.setBlockState(pos, state.withProperty(TYPE, ft), 3);
					i.setDead();
					break;
				}
			}
		}
	}
	
	private void spawnColdIron(World world, Random rand, EntityItem i) {
		ItemStack nuggets = new ItemStack(ModItems.cold_iron_ingot, 1 + rand.nextInt(9));
		EntityItem ei = new EntityItem(world, i.posX, i.posY, i.posZ, nuggets);
		ei.setNoPickupDelay();
		i.setDead();
		world.spawnEntity(ei);
	}
	
	private boolean isMundane(EntityItem i) {
		Block b = Block.getBlockFromItem(i.getItem().getItem());
		if (b instanceof BlockStone || b instanceof BlockDirt || b instanceof BlockSand || b instanceof BlockGravel) {
			return true;
		}
		return false;
	}
	
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
		worldIn.setBlockToAir(pos);
	}
	
	@SubscribeEvent
	public void onChat(ClientChatEvent evt) {
		if (Bewitchment.proxy.isPlayerInEndFire()) {
			evt.setCanceled(true);
			NetworkHandler.HANDLER.sendToServer(new WitchFireTP(evt.getOriginalMessage()));
		}
	}
	
	@Override
	public void registerModel() {
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	public static enum EnumFireType implements IStringSerializable {
		
		NORMAL(15, 0xc032db, Ingredient.EMPTY), ENDFIRE(3, 0x0B4D42, Ingredient.fromItem(Items.ENDER_PEARL)), FROSTFIRE(8, 0xa4f8ff, Ingredient.fromItem(Items.SNOWBALL));
		
		private int light, color;
		private Ingredient ingredient;
		
		EnumFireType(int lightValue, int color, Ingredient i) {
			light = lightValue;
			this.color = color;
			this.ingredient = i;
		}
		
		public int getLight() {
			return light;
		}
		
		public int getColor() {
			return color;
		}
		
		public boolean isIngredient(ItemStack stack) {
			return ingredient.apply(stack);
		}
		
		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		
	}
	
}