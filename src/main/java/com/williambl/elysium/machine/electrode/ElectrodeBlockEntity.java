package com.williambl.elysium.machine.electrode;

import com.williambl.elysium.ElysiumUtil;
import com.williambl.elysium.armour.ElysiumArmourComponent;
import com.williambl.elysium.machine.BeamPowered;
import com.williambl.elysium.machine.ElysiumMachineBlock;
import com.williambl.elysium.particles.ArcParticleOption;
import com.williambl.elysium.registry.ElysiumBlocks;
import com.williambl.elysium.registry.ElysiumSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.data.report.BlockListProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ElectrodeBlockEntity extends BlockEntity implements Clearable, SidedInventory, NamedScreenHandlerFactory, BeamPowered {
    private static final int NUM_SLOTS = 9;
    @Nullable
    private BlockPos beamSourcePos;
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private final Box shockAABB;
    @Nullable
    private UUID ownerUUID;

    public ElectrodeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ElysiumBlocks.ELECTRODE_BE, blockPos, blockState);
        this.shockAABB = (new Box(blockPos)).expand(12.0D);
    }

    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public static void tick(World level, BlockPos pos, BlockState state, ElectrodeBlockEntity be) {
        int power = state.get(ElysiumBlocks.ELYSIUM_POWER);
        if (be.getBeamSourcePos() != null) {
            int actualPower = be.getBeamPower(level);
            if (actualPower == 0) {
                be.setBeamSourcePos((BlockPos)null);
            }

            if (actualPower != power) {
                Direction neighbourDir = ((Direction)state.get(Properties.FACING)).getOpposite();
                BlockPos neighbourPos = pos.offset(neighbourDir);
                level.setBlockState(pos, state.getStateForNeighborUpdate(neighbourDir, level.getBlockState(neighbourPos), level, pos, neighbourPos));
            }
        }

        boolean hasRod = state.get(ElectrodeBlock.HAS_ROD);
        int charges = state.get(ElectrodeBlock.CHARGES);
        if (power >= 1) {
            if (charges < 4 && level.getTime() % (20L * (long)(5 - power)) == 0L) {
                ++charges;
                level.setBlockState(pos, (BlockState)state.with(ElectrodeBlock.CHARGES, charges), 2);
            }

            if (charges > 0 && level.getTime() % 10L == 0L) {
                Vec3d vec3Pos = Vec3d.ofCenter(pos);
                List<LivingEntity> entities = level.getEntitiesByClass(LivingEntity.class, be.shockAABB, ($) -> true).stream().filter((e) -> !be.isImmune(e)).filter((e) -> e.squaredDistanceTo(vec3Pos) <= (double)(hasRod ? 100 : 36)).filter((e) -> level.raycast(new RaycastContext(e.getEyePos(), vec3Pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.WATER, e)).getBlockPos().equals(pos)).filter((e) -> getConductivity(e) >= 0.0D).sorted(Comparator.comparingDouble((e) -> e.squaredDistanceTo(vec3Pos) + getConductivity(e))).limit(hasRod ? 1L : 3L).toList();

                for(LivingEntity entity : entities) {
                    ElysiumArmourComponent component = (ElysiumArmourComponent)ElysiumArmourComponent.KEY.getNullable(entity);
                    if (component != null && component.hasElysiumArmour()) {
                        component.addCharge(hasRod ? 6.0F : 4.0F);
                    } else {
                        entity.damage(ElysiumDamageSources.create(level, ElysiumDamageSources.ELECTRODE), hasRod ? 8.0F : 4.0F);
                    }

                    if (hasRod) {
                        Vec3d knockbackDir = vec3Pos.subtract(entity.getPos()).multiply(1.0D, 0.0D, 1.0D).normalize();
                        entity.takeKnockback(1.1D, knockbackDir.x, knockbackDir.z);
                    }

                    ((ServerWorld)level).spawnParticles(new ArcParticleOption(entity.getX(), entity.getBodyY(0.5D), entity.getZ()), vec3Pos.x, vec3Pos.y + (hasRod ? 1.0D : 0.5D), vec3Pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }

                if (entities.size() > 0) {
                    --charges;
                    level.setBlockState(pos, (BlockState)state.with(ElectrodeBlock.CHARGES, charges), 2);
                    level.playSound((PlayerEntity) null, pos, ElysiumSounds.ELECTRODE_ZAP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            }

        }
    }

    private boolean isImmune(LivingEntity entity) {
        if (!entity.isSpectator()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)entity;
                if (player.getAbilities().creativeMode) {
                    return true;
                }
            }

            if (!ElysiumArmourComponent.KEY.maybeGet(entity).filter(ElysiumArmourComponent::hasElysiumArmour).isEmpty() || !Objects.equals(entity.getUuid(), this.ownerUUID) && !this.stacks.stream().anyMatch((s) -> s.getName().getString().equals(entity.getEntityName()))) {
                return false;
            }
        }

        return true;
    }

    protected void writeNbt(@NotNull NbtCompound tag) {
        super.writeNbt(tag);
        NbtList inv = new NbtList();
        this.stacks.stream().map((s) -> s.writeNbt(new NbtCompound())).forEach(inv::add);
        tag.put("stacks", inv);
        if (this.ownerUUID != null) {
            tag.putUuid("owner", this.ownerUUID);
        }

    }

    public void readNbt(@NotNull NbtCompound tag) {
        super.readNbt(tag);
        NbtList listTag = tag.getList("stacks", 10);
        List<ItemStack> inv = listTag.stream().map(NbtCompound.class::cast).map(ItemStack::fromNbt).toList();

        for(int i = 0; i < this.stacks.size() && i < inv.size(); ++i) {
            this.stacks.set(i, (ItemStack)inv.get(i));
        }

        if (tag.containsUuid("owner")) {
            this.ownerUUID = tag.getUuid("owner");
        }

    }

    private static double getConductivity(Entity entity) {
        double entityMagnetism = ElysiumBlocks.ENTITY_CONDUCTIVITY.get(entity.getType()).orElse(0.0D);
        double itemMagnetism = ElysiumUtil.getItemForEntity(entity).flatMap(ElysiumBlocks.ITEM_CONDUCTIVITY::get).orElse(0.0D);
        double var10000;
        if (entity instanceof LivingEntity) {
            LivingEntity lE = (LivingEntity)entity;
            var10000 = getArmourConductivity(lE);
        } else {
            var10000 = 0.0D;
        }

        double armourMagnetism = var10000;
        return entityMagnetism + itemMagnetism + armourMagnetism;
    }

    private static double getArmourConductivity(LivingEntity entity) {
        double count = 0.0D;

        for(ItemStack armour : entity.getArmorItems()) {
            count += ElysiumBlocks.ITEM_CONDUCTIVITY.get(armour.getItem()).orElse(0.0D);
        }

        return count;
    }

    @NotNull
    public int[] getAvailableSlots(@NotNull Direction side) {
        int[] slots = new int[9];

        for(int i = 0; i < 9; slots[i] = i++) {
        }

        return slots;
    }

    public boolean canInsert(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return index < 9;
    }

    public boolean canExtract(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index < 9;
    }

    public int size() {
        return 9;
    }

    public boolean isEmpty() {
        return this.stacks.stream().allMatch(ItemStack::isEmpty);
    }

    @NotNull
    public ItemStack getCachedState(int slot) {
        return slot < 9 ? (ItemStack)this.stacks.get(slot) : ItemStack.EMPTY;
    }

    @NotNull
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removing = this.getCachedState(slot);
        return removing.isEmpty() ? ItemStack.EMPTY : removing.split(amount);
    }

    @NotNull
    public ItemStack removeStack(int slot) {
        ItemStack removing = this.getCachedState(slot);
        this.setStack(slot, ItemStack.EMPTY);
        return removing;
    }

    public void setStack(int slot, @NotNull ItemStack stack) {
        if (slot < 9) {
            this.stacks.set(slot, stack);
        }

    }

    public boolean canBeUsedBy(PlayerEntity player) {
        return Objects.equals(player.getUuid(), this.ownerUUID) || player.isCreativeLevelTwoOp();
    }

    public boolean canPlayerUse(@NotNull PlayerEntity player) {
        if (this.world != null && this.world.getBlockEntity(this.pos) == this) {
            if (!this.canBeUsedBy(player)) {
                return false;
            } else {
                return !(player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) > 64.0D);
            }
        } else {
            return false;
        }
    }

    public void clear() {
        this.stacks.clear();
    }

    @NotNull
    public Text getDisplayName() {
        return Text.translatable("container.elysium.electrode");
    }

    @Nullable
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
        return ((ElysiumMachineBlock)ElysiumBlocks.ELECTRODE).isReceivingSide(this.getCachedState(), beamDir.getOpposite());
    }
}