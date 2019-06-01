package com.gofficer.codenames;


/**
 * Not sure how to type Class[].class it kotlin, so importing from java for use in Kryo serialization
 */
public class NetworkDictionaryHelper  {

    static public Class getClazz() {
        return Class[].class;
    }
}