package cc.minetale.sodium.cache;

import cc.minetale.sodium.util.Redis;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record RequestCache(String key, int ttl) {

    //    @Getter private static final RequestCache partyRequest;
    @Getter private static final RequestCache friendRequest;

    static {
//        partyRequest = new RequestCache("party-request", 30 * 60);
        friendRequest = new RequestCache("friend-request", 24 * 60 * 60);
    }

    public Set<UUID> getRawOutgoing(UUID player) {
        var rawRequests = Redis.runRedisCommand(jedis -> jedis.smembers(getOutgoingKey(player)));
        var requests = new HashSet<UUID>();

        if (rawRequests != null && !rawRequests.isEmpty()) {
            for (var request : rawRequests) {
                requests.add(UUID.fromString(request.split(":")[0]));
            }
        }

        return requests;
    }

    public Set<UUID> getRawIncoming(UUID player) {
        var rawRequests = Redis.runRedisCommand(jedis -> jedis.smembers(getIncomingKey(player)));
        var requests = new HashSet<UUID>();

        if (rawRequests != null && !rawRequests.isEmpty()) {
            for (var request : rawRequests) {
                requests.add(UUID.fromString(request.split(":")[0]));
            }
        }

        return requests;
    }

    public Set<Request> getOutgoing(UUID player) {
        var rawRequests = Redis.runRedisCommand(jedis -> jedis.smembers(getOutgoingKey(player)));
        var requests = new HashSet<Request>();

        if (rawRequests != null && !rawRequests.isEmpty()) {
            for (var request : rawRequests) {
                var split = request.split(":");

                requests.add(new Request(
                        player,
                        UUID.fromString(split[0]),
                        Long.parseLong(split[1]) - System.currentTimeMillis()
                ));
            }
        }

        return requests;
    }

    public Set<Request> getIncoming(UUID player) {
        var rawRequests = Redis.runRedisCommand(jedis -> jedis.smembers(getIncomingKey(player)));
        var requests = new HashSet<Request>();

        if (rawRequests != null && !rawRequests.isEmpty()) {
            for (var request : rawRequests) {
                var split = request.split(":");

                requests.add(new Request(
                        UUID.fromString(split[0]),
                        player,
                        Long.parseLong(split[1]) - System.currentTimeMillis()
                ));
            }
        }

        return requests;
    }

    public void removeCache(UUID player, UUID target) {
        Redis.runRedisCommand(jedis -> {
            var pipeline = jedis.pipelined();

            var outgoing = jedis.smembers(getOutgoingKey(player));
            var incoming = jedis.smembers(getIncomingKey(target));

            for (var member : outgoing) {
                if (member.contains(target.toString())) {
                    pipeline.srem(getOutgoingKey(player), member);
                }
            }

            for (var member : incoming) {
                if (member.contains(player.toString())) {
                    pipeline.srem(getIncomingKey(target), member);
                }
            }

            pipeline.sync();

            return null;
        });
    }

    public void pushCache(UUID player, UUID target) {
        var cur = System.currentTimeMillis();

        var outgoing = target + ":" + (cur + ttl * 1000L);
        var incoming = player + ":" + (cur + ttl * 1000L);

        Redis.runRedisCommand(jedis -> {
            var pipeline = jedis.pipelined();

            pipeline.sadd(getOutgoingKey(player), outgoing);
            pipeline.sadd(getIncomingKey(target), incoming);

            pipeline.sendCommand(Redis.CustomCommand.EXPIREMEMBER, getOutgoingKey(player), outgoing, String.valueOf(ttl));
            pipeline.sendCommand(Redis.CustomCommand.EXPIREMEMBER, getIncomingKey(target), incoming, String.valueOf(ttl));

            pipeline.sync();

            return null;
        });
    }

    public String getOutgoingKey(UUID identifier) {
        return "minetale:outgoing-" + key + ":" + identifier;
    }

    public String getIncomingKey(UUID identifier) {
        return "minetale:incoming-" + key + ":" + identifier;
    }

    public record Request(UUID initiator, UUID target, long ttl) {}

}
