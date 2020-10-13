package com.clussmanproductions.economycontrol.blocks;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.ModBlocks;
import com.clussmanproductions.economycontrol.gui.GuiProxy;
import com.clussmanproductions.economycontrol.proxy.CommonProxy;
import com.clussmanproductions.economycontrol.tile.ATMTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.permission.PermissionAPI;

public class BlockATM extends Block {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public BlockATM()
	{
		super(Material.IRON);
		setHardness(2f);
		setHarvestLevel("pickaxe", 2);
		setRegistryName("atm");
		setUnlocalizedName(EconomyControl.MODID + ".atm");
		setCreativeTab(EconomyControl.TAB);
	}
	
	@SideOnly(Side.CLIENT)
	public void initModel()
	{
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		playerIn.openGui(EconomyControl.instance, GuiProxy.GUI_IDs.ATM, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new ATMTileEntity();
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		if (!worldIn.isRemote)
		{
			ATMTileEntity te = (ATMTileEntity)worldIn.getTileEntity(pos);
			te.setOwner(placer.getName());
			worldIn.setBlockState(pos.up(), ModBlocks.atm_upper.getDefaultState());
			
			worldIn.notifyBlockUpdate(pos, state, state, 2);
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof ATMTileEntity)
		{
			ATMTileEntity atm = (ATMTileEntity)te;
			if (!atm.getOwner().equals(player.getName()) && !PermissionAPI.hasPermission(player, CommonProxy.Permissions.breakAnyATM))
			{
				if (!world.isRemote)
				{
					player.sendMessage(new TextComponentString("You do not own this ATM"));
				}
				return false;
			}
		}
		
		if (!world.isRemote && world.getBlockState(pos.up()).getBlock() == ModBlocks.atm_upper)
		{
			world.destroyBlock(pos.up(), false);
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
	}
}

