package com.williambl.elysium.machine.electrode;

import com.williambl.elysium.machine.BeamPowered;
import com.williambl.elysium.machine.ElysiumMachineBlock;
import com.williambl.elysium.registry.ElysiumBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class ElectrodeBlock extends BlockEntity implements Clearable, SidedInventory, NamedScreenHandlerFactory, BeamPowered {
    private static final int NUM_SLOTS = 9;
    @Nullable
    private BlockPos beamSourcePos;
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(NUM_SLOTS, ItemStack.EMPTY);
    @Nullable
    private UUID ownerUUID;

    public ElectrodeBlock(BlockPos blockPos, BlockState blockState) {
        super(ElysiumBlocks.ELECTRODE_BE, blockPos, blockState);
    }

    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    public int size() {
        return NUM_SLOTS;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < stacks.size() ? stacks.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    @NotNull
    @Override
    public Text getDisplayName() {
        return Text.translatable("container.elysium.electrode");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int i, @NotNull PlayerInventory inventory, @NotNull PlayerEntity player) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, i, inventory, this, 1);
    }

    @Nullable
    public BlockPos getBeamSourcePos() {
        return this.beamSourcePos;
    }

    public void setBeamSourcePos(@Nullable BlockPos pos) {
        this.beamSourcePos = pos;
    }

    public boolean canAcceptBeam(Direction beamDir) {
        return ((ElysiumMachineBlock) ElysiumBlocks.ELECTRODE).isReceivingSide(this.getCachedState(), beamDir.getOpposite());
    }

    @NotNull
    @Override
    public int[] getAvailableSlots(@NotNull Direction side) {
        int[] slots = new int[NUM_SLOTS];
        for (int i = 0; i < NUM_SLOTS; slots[i] = i++) {
        }
        return slots;
    }

    @Override
    public boolean canInsert(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return index < NUM_SLOTS;
    }

    @Override
    public boolean canExtract(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index < NUM_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public void setStack(int slot, @NotNull ItemStack stack) {
        if (slot < NUM_SLOTS) {
            this.stacks.set(slot, stack);
        }
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removing = this.getStack(slot);
        return removing.isEmpty() ? ItemStack.EMPTY : removing.split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removing = this.getStack(slot);
        this.setStack(slot, ItemStack.EMPTY);
        return removing;
    }

    public boolean canBeUsedBy(PlayerEntity player) {
        return Objects.equals(player.getUuid(), this.ownerUUID) || player.isCreativeLevelTwoOp();
    }

    @Override
    public boolean canPlayerUse(@NotNull PlayerEntity player) {
        if (this.world != null && this.world.getBlockEntity(this.pos) == this) {
            if (!this.canBeUsedBy(player)) {
                return false;
            }
            return !(player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D);
        }
        return false;
    }

    public static void tick(World level, BlockPos pos, BlockState state, ElectrodeBlock be) {
        int power = state.get(ElysiumBlocks.ELYSIUM_POWER);
        if (be.getBeamSourcePos() != null) {
            int actualPower = be.getBeamPower(level);
            if (actualPower == 0) {
                be.setBeamSourcePos(null);
            }

            if (actualPower != power) {
                Direction neighbourDir = state.get(Properties.FACING).getOpposite();
                BlockPos neighbourPos = pos.offset(neighbourDir);
                level.setBlockState(pos, state.getStateForNeighborUpdate(neighbourDir, level.getBlockState(neighbourPos), level, pos, neighbourPos));
            }
        }
    }
}
