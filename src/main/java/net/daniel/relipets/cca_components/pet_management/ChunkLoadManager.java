package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
public class ChunkLoadManager {
    public List<ChunkLoadRequest> requests = new ArrayList<>();

    public void addRequest(ChunkLoadRequest request){
        System.out.println("Added chunk load request");
        this.requests.add(request);
        for(Vec2f chunkCoord : request.getChunkCoords()){
            request.getWorld().setChunkForced((int)chunkCoord.x, (int)chunkCoord.y, true);
        }
    }

    public void processActiveRequestsIfAny(){

        for(Iterator<ChunkLoadRequest> iterator = this.requests.iterator() ; iterator.hasNext();){

            ChunkLoadRequest request = iterator.next();

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
                    System.out.println("Chunk load request fulfilled");
                    this.unloadChunks(request.getChunkCoords(), request.getWorld());

                }else{
                    System.out.println("Chunks were loaded but request could not be fulfilled...");
                }



            }
        }

    }

    public void consumeRequest(ChunkLoadRequest request){



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
