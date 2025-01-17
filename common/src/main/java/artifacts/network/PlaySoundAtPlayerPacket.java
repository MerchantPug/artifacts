package artifacts.network;

import artifacts.Artifacts;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public record PlaySoundAtPlayerPacket(Holder<SoundEvent> soundEvent, float volume, float pitch, long seed) implements CustomPacketPayload {

    public static final Type<PlaySoundAtPlayerPacket> TYPE = new Type<>(Artifacts.id("play_sound_at_player"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaySoundAtPlayerPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.SOUND_EVENT),
            PlaySoundAtPlayerPacket::soundEvent,
            ByteBufCodecs.FLOAT,
            PlaySoundAtPlayerPacket::volume,
            ByteBufCodecs.FLOAT,
            PlaySoundAtPlayerPacket::pitch,
            ByteBufCodecs.VAR_LONG,
            PlaySoundAtPlayerPacket::seed,
            PlaySoundAtPlayerPacket::new
    );

    public static void sendSound(ServerPlayer player, Holder<SoundEvent> soundEvent, float volume, float pitch) {
        long seed = player.level().random.nextLong();
        NetworkManager.sendToPlayer(player, new PlaySoundAtPlayerPacket(soundEvent, volume, pitch, seed));
        player.level().playSeededSound(player, player, soundEvent, SoundSource.PLAYERS, volume, pitch, seed);
    }

    void apply(NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        player.level().playSeededSound(player, player, soundEvent, SoundSource.PLAYERS, volume, pitch, seed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
