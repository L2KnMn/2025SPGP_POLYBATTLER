package kr.ac.tukorea.ge.lkm.polybattler.polybattler.game;

import java.util.HashMap;
import java.util.Map;

public class ServiceLocator {
    private static final Map<Class<?>, Object> services = new HashMap<>();

    public static <T> void registerService(Class<T> type, T service) {
        services.put(type, service);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> type) {
        return (T) services.get(type);
    }

    // 초기 서비스 등록 (GameManager)
    public static void initialize() {
        registerService(IGameManager.class, GameManager.getInstance());
    }
}