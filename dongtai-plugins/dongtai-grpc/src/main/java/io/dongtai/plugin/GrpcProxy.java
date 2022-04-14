package io.dongtai.plugin;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

import java.util.HashMap;
import java.util.Map;

public class GrpcProxy {
    private static Map<String, Object> metadata;

    public static Object interceptChannel(Object channel, String traceKey, String traceId) {
        try {
            Channel interceptedChannel = (Channel) channel;
            return ClientInterceptors.intercept(interceptedChannel, new DongTaiClientInterceptor(traceKey, traceId));
        } catch (Exception e) {
            // fixme: remove throw exception
            e.printStackTrace();
        }
        return channel;
    }

    public static Object interceptService(Object service) {
        try {
            metadata = null;
            metadata = new HashMap<String, Object>(32);
            ServerServiceDefinition interceptedService = (ServerServiceDefinition) service;
            return ServerInterceptors.intercept(interceptedService, new DongTaiServerInterceptor());
        } catch (Exception e) {
            // fixme: remove throw exception
            e.printStackTrace();
        }
        return service;
    }

    public static Map<String, Object> getServerMeta() {
        return metadata;
    }

    public static void addMetaItem(String key, Object obj) {
        if (metadata != null) {
            metadata.put(key, obj);
        }
    }
}