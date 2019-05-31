package com.gofficer.codenames;

import com.artemis.Component;
import com.gofficer.codenames.components.*;
import com.gofficer.codenames.network.notification.EntityUpdate;
import net.mostlyoriginal.api.network.marshal.common.MarshalDictionary;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkDictionary extends MarshalDictionary {

    public NetworkDictionary() {
        registerAll(

                // Game Notifications
                EntityUpdate.class,
//                EntityUpdate.EntityUpdateBuilder.class,
                // Other
                boolean[][].class,
                boolean[].class,
                int[][].class,
                int[].class,
                Integer[].class,
                ConcurrentHashMap.class,
                HashMap.class,
                HashSet.class,
                Component.class,
                Component[].class,
                Class.class,
                Class[].class, // TODO not sure how to do this in kotlin hence the java file
                Object.class,
                Network.class,
                Network.Client.JoinRoomRequest.class,
                EntityUpdate.EntityUpdateBuilder.class,

                PositionComponent.class,
                CardComponent.class,
                NetworkComponent.class,
                RevealedComponent.class,
                TextureReferenceComponent.class
        );
    }

    private void registerAll(Class... classes) {
        topId = 40;
        for (Class clazz : classes) {
            register(topId++, clazz);
        }
    }
}