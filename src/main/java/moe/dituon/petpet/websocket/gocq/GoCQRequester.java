package moe.dituon.petpet.websocket.gocq;

import moe.dituon.petpet.plugin.PluginPetService;

import java.util.HashMap;

public class GoCQRequester {
    private final HashMap<Long, ThreadLockObject<GoCQMemberDTO>> requestList = new HashMap<>();
    private final GoCQAPIWebSocketClient apiClient;

    public GoCQRequester(GoCQAPIWebSocketClient client){
        this.apiClient = client;
    }
    public GoCQMemberDTO getGroupMember(long groupId, long memberId){
        ThreadLockObject<GoCQMemberDTO> lock = new ThreadLockObject<>();
        PluginPetService.LOGGER.info(requestList+ "   id: " +memberId);
        requestList.put(memberId, lock);

        apiClient.send(new GoCQGetGroupMemberRequestParamDTO(
                groupId, memberId
        ).toRequestDTO().stringify());

        synchronized (lock){
            try {
                lock.wait();
                GoCQMemberDTO member = lock.get();
                requestList.remove(memberId);
                return member;
            } catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        }
    }

    public ThreadLockObject<GoCQMemberDTO> getThreadLock(Long id) {
        return requestList.get(id);
    }
}

class ThreadLockObject<T>{
    private T t;
    public void set(T t){
        this.t = t;
    }
    public T get(){
        return t;
    }
}
