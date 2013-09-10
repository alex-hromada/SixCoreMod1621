package com.basspro.scm.world.biome;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.biome.BiomeCache;

public class BiomeCacheSCM {
    
    /** Reference to the WorldChunkManager */
    private final WorldChunkManagerSCM chunkManager;

    /** The last time this BiomeCache was cleaned, in milliseconds. */
    private long lastCleanupTime;

    /**
     * The map of keys to BiomeCacheBlocks. Keys are based on the chunk x, z coordinates as (x | z << 32).
     */
    private LongHashMap cacheMap = new LongHashMap();

    /** The list of cached BiomeCacheBlocks */
    private List cache = new ArrayList();

    public BiomeCacheSCM(WorldChunkManagerSCM par1WorldChunkManager)
    {
        this.chunkManager = par1WorldChunkManager;
    }

    /**
     * Returns a biome cache block at location specified.
     */
    public BiomeCacheBlockSCM getBiomeCacheBlock(int par1, int par2)
    {
        par1 >>= 4;
        par2 >>= 4;
        long k = (long)par1 & 4294967295L | ((long)par2 & 4294967295L) << 32;
        BiomeCacheBlockSCM biomecacheblock = (BiomeCacheBlockSCM)this.cacheMap.getValueByKey(k);

        if (biomecacheblock == null)
        {
            biomecacheblock = new BiomeCacheBlockSCM(this, par1, par2);
            this.cacheMap.add(k, biomecacheblock);
            this.cache.add(biomecacheblock);
        }

        biomecacheblock.lastAccessTime = MinecraftServer.func_130071_aq();
        return biomecacheblock;
    }

    /**
     * Returns the BiomeGenBase related to the x, z position from the cache.
     */
    public BiomeGenBaseSCM getBiomeGenAt(int par1, int par2)
    {
        return this.getBiomeCacheBlock(par1, par2).getBiomeGenAt(par1, par2);
    }

    /**
     * Removes BiomeCacheBlocks from this cache that haven't been accessed in at least 30 seconds.
     */
    public void cleanupCache()
    {
        long i = MinecraftServer.func_130071_aq();
        long j = i - this.lastCleanupTime;

        if (j > 7500L || j < 0L)
        {
            this.lastCleanupTime = i;

            for (int k = 0; k < this.cache.size(); ++k)
            {
                BiomeCacheBlockSCM biomecacheblock = (BiomeCacheBlockSCM)this.cache.get(k);
                long l = i - biomecacheblock.lastAccessTime;

                if (l > 30000L || l < 0L)
                {
                    this.cache.remove(k--);
                    long i1 = (long)biomecacheblock.xPosition & 4294967295L | ((long)biomecacheblock.zPosition & 4294967295L) << 32;
                    this.cacheMap.remove(i1);
                }
            }
        }
    }

    /**
     * Returns the array of cached biome types in the BiomeCacheBlock at the given location.
     */
    public BiomeGenBaseSCM[] getCachedBiomes(int par1, int par2)
    {
        return this.getBiomeCacheBlock(par1, par2).biomes;
    }

    /**
     * Get the world chunk manager object for a biome list.
     */
    static WorldChunkManagerSCM getChunkManager(BiomeCacheSCM par0BiomeCache)
    {
        return par0BiomeCache.chunkManager;
    }

}
