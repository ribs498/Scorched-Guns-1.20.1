package top.ribs.scguns.datagen;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Gun;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public abstract class GunProvider implements DataProvider
{
    protected final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final Map<ResourceLocation, Gun> gunMap = new HashMap<>();

    protected GunProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "guns");
        this.registries = registries;
    }

    protected abstract void registerGuns();

    protected final void addGun(ResourceLocation id, Gun gun)
    {
        this.gunMap.put(id, gun);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache)
    {
        return this.registries.thenCompose(provider ->
        {
            this.gunMap.clear();
            this.registerGuns();
            return CompletableFuture.allOf(this.gunMap.entrySet().stream().map(entry -> {
                ResourceLocation key = entry.getKey();
                Gun gun = entry.getValue();
                Path path = this.pathProvider.json(key);
                JsonObject object = gun.toJsonObject();
                return DataProvider.saveStable(cache, object, path);
            }).toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName()
    {
        return "Guns: " + Reference.MOD_ID;
    }

}
