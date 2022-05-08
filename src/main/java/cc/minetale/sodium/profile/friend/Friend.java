package cc.minetale.sodium.profile.friend;

import cc.minetale.sodium.cache.ProfileCache;
import cc.minetale.sodium.cache.RequestCache;
import cc.minetale.sodium.profile.Profile;
import cc.minetale.sodium.util.Redis;

public class Friend {

    public static final int MAX_OUTGOING_REQUESTS = 100;
    public static final int MAX_FRIENDS = 100;

    public static RemoveResponse removeFriend(Profile player, Profile target) {
        if (!player.isFriends(target) || !target.isFriends(player)) { return RemoveResponse.NOT_ADDED; }

        player.removeFriend(target);
        target.removeFriend(player);

        player.save();
        target.save();

        ProfileCache.modifyProfile(player);
        ProfileCache.modifyProfile(target);

        return RemoveResponse.SUCCESS;
    }

    public static CancelResponse removeRequest(Profile player, Profile target) {
        var playerUuid = player.getUuid();
        var targetUuid = target.getUuid();
        var cache = RequestCache.getFriendRequest();

        return Redis.runRedisCommand(jedis -> {
            var incoming = cache.getRawIncoming(playerUuid);

            if (!incoming.contains(targetUuid)) { return CancelResponse.NO_REQUEST; }

            cache.removeCache(playerUuid, targetUuid);

            return CancelResponse.SUCCESS;
        });
    }

    public static AddResponse addRequest(Profile player, Profile target) {
        var playerUuid = player.getUuid();
        var targetUuid = target.getUuid();
        var cache = RequestCache.getFriendRequest();

        return Redis.runRedisCommand(jedis -> {
            if (player.isFriends(target) || target.isFriends(player)) { return AddResponse.ALREADY_FRIENDS; }
            if (playerUuid.equals(targetUuid)) { return AddResponse.TARGET_IS_PLAYER; }
            if (!target.getOptionsProfile().isReceivingFriendRequests()) { return AddResponse.REQUESTS_TOGGLED; }
            if (player.isIgnoring(target)) { return AddResponse.TARGET_IGNORED; }
            if (target.isIgnoring(player)) { return AddResponse.PLAYER_IGNORED; }

            var outgoing = jedis.smembers(cache.getOutgoingKey(playerUuid));

            if (outgoing.size() >= MAX_OUTGOING_REQUESTS) { return AddResponse.MAX_OUTGOING; }
            if (outgoing.contains(targetUuid.toString())) { return AddResponse.REQUEST_EXIST; }

            var incoming = jedis.smembers(cache.getIncomingKey(playerUuid));

            if (incoming.contains(targetUuid.toString())) { return AddResponse.PENDING_REQUEST; }

            cache.pushCache(playerUuid, targetUuid);

            return AddResponse.SUCCESS;
        });
    }

    public static AcceptResponse acceptRequest(Profile player, Profile target) {
        var playerUuid = player.getUuid();
        var targetUuid = target.getUuid();
        var cache = RequestCache.getFriendRequest();

        return Redis.runRedisCommand(jedis -> {
            var outgoing = jedis.smembers(cache.getOutgoingKey(playerUuid));

            if (!outgoing.contains(targetUuid.toString())) { return AcceptResponse.NO_REQUEST; }

            cache.removeCache(playerUuid, targetUuid);

            if (player.getFriends().size() >= MAX_FRIENDS) { return AcceptResponse.PLAYER_MAX_FRIENDS; }
            if (target.getFriends().size() >= MAX_FRIENDS) { return AcceptResponse.TARGET_MAX_FRIENDS; }
            if (player.isIgnoring(target)) { return AcceptResponse.TARGET_IGNORED; }
            if (target.isIgnoring(player)) { return AcceptResponse.PLAYER_IGNORED; }

            player.removeFriend(target);
            target.removeFriend(player);

            player.save();
            target.save();

            ProfileCache.modifyProfile(player);
            ProfileCache.modifyProfile(target);

            return AcceptResponse.SUCCESS;
        });
    }

    public enum RemoveResponse { NOT_ADDED, SUCCESS }
    public enum CancelResponse { NO_REQUEST, SUCCESS }
    public enum AddResponse { ALREADY_FRIENDS, TARGET_IS_PLAYER, MAX_OUTGOING, REQUEST_EXIST, PENDING_REQUEST, REQUESTS_TOGGLED, TARGET_IGNORED, PLAYER_IGNORED, SUCCESS }
    public enum AcceptResponse { NO_REQUEST, PLAYER_MAX_FRIENDS, TARGET_MAX_FRIENDS, TARGET_IGNORED, PLAYER_IGNORED, SUCCESS }

}
