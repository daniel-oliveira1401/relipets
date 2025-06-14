package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import net.daniel.relipets.utils.Utils;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
public class ChunkLoadManager {
    public List<ChunkLoadRequest> requests = new ArrayList<>();

    public void addRequest(ChunkLoadRequest request){
        Utils.log("Added chunk load request");
        this.requests.add(request);
        this.forceChunkLoad(request);
    }

    private void forceChunkLoad(ChunkLoadRequest request){
        for(Vec2f chunkCoord : request.getChunkCoords()){
            request.getWorld().setChunkForced((int)chunkCoord.x, (int)chunkCoord.y, true);
        }
    }

    public void processActiveRequestsIfAny(){

        for(Iterator<ChunkLoadRequest> iterator = this.requests.iterator() ; iterator.hasNext();){

            ChunkLoadRequest request = iterator.next();

            //this is here to cover the edge case of having two requests that target the same chunk and
            //the fulfillment of the first request causes that chunk to unload and so the second request
            //may never complete because one of the chunks that it relies on is not forceloaded anymore
            this.forceChunkLoad(request);

            boolean chunksLoaded = true;

            for (Vec2f chunkCoord : request.getChunkCoords()){
                if(!request.getWorld().isChunkLoaded((int)chunkCoord.x, (int)chunkCoord.y)){
                    chunksLoaded = false;
                    break;
                }
            }

            if(chunksLoaded){

                boolean succeeded = request.getAction().getAsBoolean();
                if(succeeded){
                    iterator.remove();
                    Utils.log("Chunk load request fulfilled");
                    this.unloadChunks(request.getChunkCoords(), request.getWorld());

                }else{
                    Utils.log("Chunks were loaded but request could not be fulfilled...");
                }



            }
        }

    }

    public void cleanupUnfulfilledRequests(){
        this.requests.forEach((r)-> this.unloadChunks(r.getChunkCoords(), r.getWorld()));
    }

    private void unloadChunks(List<Vec2f> chunkCoords, ServerWorld world){
        for(Vec2f chunkCoord : chunkCoords){
            world.setChunkForced((int)chunkCoord.x, (int)chunkCoord.y, false);
        }
    }

}
