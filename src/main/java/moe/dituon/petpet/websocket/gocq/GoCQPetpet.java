package moe.dituon.petpet.websocket.gocq;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;

public class GoCQPetpet {
    private static class GoCQPetpetInstance {
        private static final GoCQPetpet INSTANCE = new GoCQPetpet();
    }
    public static GoCQPetpet getInstance() {
        return GoCQPetpetInstance.INSTANCE;
    }
    GoCQPetService service = new GoCQPetService();
    GoCQAPIWebSocketClient apiClient = null;
    GoCQEventWebSocketClient eventClient = null;
    LinkedHashMap<Long, String> imageCachePool;
    private GoCQPetpet() {
        service.readConfig();
        service.readData();

//        if (service.respondReply) {
//            imageCachePool = new LinkedHashMap<>(service.cachePoolSize, 0.75f, true) {
//                @Override
//                public boolean removeEldestEntry(Map.Entry eldest) {
//                    return size() > service.cachePoolSize;
//                }
//            };
////            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, this::cacheMessageImage);
////            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessagePostSendEvent.class, this::cacheMessageImage);
//        }

        try{
            System.out.println(service.apiWebSocketUri);
            apiClient = new GoCQAPIWebSocketClient(new URI(service.apiWebSocketUri));
            eventClient = new GoCQEventWebSocketClient(new URI(service.eventWebSocketUri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
