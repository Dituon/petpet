package moe.dituon.petpet.websocket.gocq;

import moe.dituon.petpet.plugin.DataUpdater;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import static moe.dituon.petpet.share.BasePetService.LOGGER;

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


        if (service.respondReply) {
            imageCachePool = new LinkedHashMap<>(service.cachePoolSize, 0.75f, true) {
                @Override
                public boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > service.cachePoolSize;
                }
            };
//            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, this::cacheMessageImage);
//            GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessagePostSendEvent.class, this::cacheMessageImage);
        }

        if (service.autoUpdate){
            new Thread(() -> {
                var uploader = new DataUpdater(service, service.dataRoot);
                if (uploader.autoUpdate()){
                    LOGGER.info("Petpet 模板更新完毕, 正在重载");
                    service.readData();
                }
            });
        }

        try{
            LOGGER.info("WebSocket API URL: " + service.apiWebSocketUri);
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
