

import com.fasterxml.jackson.databind.ObjectMapper
import com.gofficer.colyseus.network.*
import org.apache.commons.codec.binary.Hex;
import org.junit.Test
import org.msgpack.jackson.dataformat.MessagePackFactory


/**
 * 5/18/2019
 * Test to see what messagepack serialization approaches work best
 *
 * Trying it out to see what boilerplate code will look like
 */
class SerializingTechniques {

    /**
     * Ints and strings
     */
    @Test
    fun appraoch1() {
        val protocol = 1
        val anyObj = "hello"
        val packedByteArray = pack(protocol, TestSubType.ITEM_1, SomeType("Hello", "Goodbye"))

        val pattern: Regex = "(\\S{2})".toRegex()
        println()
        println(Hex.encodeHexString(packedByteArray).replace(pattern, "$1 "))

        val unpacked = unpackUnknown(packedByteArray)

        val message = messageHelper(unpacked)
        testHelper(message)


        val packedByteArray2 = pack(protocol, TestSubType.ITEM_2, SomeOtherType(1, "Ahoy"))
        val unpacked2 = unpackUnknown(packedByteArray2)
        val message2 = messageHelper(unpacked2)
        testHelper(message2)
    }

    @Test
    fun appraoch2() {
        val protocol = 1
        val anyObj = "hello"
        val packedByteArray = pack(protocol, TestSubType.ITEM_1, TestTouchCard(12))

        val pattern: Regex = "(\\S{2})".toRegex()
        println()
        println(Hex.encodeHexString(packedByteArray).replace(pattern, "$1 "))

        val unpacked = unpackUnknown(packedByteArray)

        println(unpacked)
//        val message = messageHelper(unpacked)
//        testHelper(message)
//
//        val packedByteArray2 = pack(protocol, TestSubType.ITEM_2, SomeOtherType(1, "Ahoy"))
//        val unpacked2 = unpackUnknown(packedByteArray2)
//        val message2 = messageHelper(unpacked2)
//        testHelper(message2)
    }
}

interface BaseAction

sealed class TestNetworkProtocolAction(var type: Int) : BaseAction {
    var isFromServer: Boolean = false
}

data class TestTouchCard(val id: Int): TestNetworkProtocolAction(SubProtocol.TOUCH_CARD)
data class TestTouchCard2(val id: Int): BaseAction

// Order matter when using ordinal
//enum class SubType {
//    ONE,
//    TWO,
//    THREE
//}

interface Type
data class SomeType(var someString: String, var anotherString: String): Type
//data class SomeType(var someString: String? = null, var anotherString: String? = null): Type
data class SomeOtherType(val someInt: Int, val someString: String): Type

/**
 * Pros/cons
 *
 * Using jackson msgpackMapper I can convert by type to proper object using reflection, but data classes need default
 * constructors thus instead of something like
 *   data class SomeType(var someString: String, var anotherString: String): Type
 * I have to do
 *   data class SomeType(var someString: String? = null, var anotherString: String? = null): Type
 * Which seems kinda annoying
 */

// Option A: MsgPackMapper approach
enum class SubTypeA(val type: Class<*>) {
    ONE(SomeType::class.javaObjectType),
    TWO(SomeOtherType::class.javaObjectType)
}

fun messageHelperA(protocolMessage: ProtocolMessage?) : Type? {
    val subProtocol = protocolMessage?.subProtocol
    val message = protocolMessage?.message
    if(subProtocol == null || message == null) {
        return null
    }

    val msgpackMapper = ObjectMapper(MessagePackFactory())
    return when (subProtocol) {
        SubTypeA.ONE.ordinal -> {
//            SubType.ONE.value
//            return moshiPack.unpack<SomeType>(message)
//            return msgpackMapper.readValue(message, SubType.ONE.value)
//            return msgpackMapper.readValue(message, SomeType::class.javaObjectType)
            return msgpackMapper.readValue(message, SubTypeA.ONE.type) as Type
        }
        SubTypeA.TWO.ordinal -> moshiPack.unpack<SomeOtherType>(message)
        else -> null
    }
}

// Option B: Moshi mapper
/**
 * Probably just use this approach
 */
enum class SubType {
    ONE,
    TWO
}


class TestSubType {

    companion object {

        const val ITEM_1 = 1
        const val ITEM_2 = 2

        // TODO define sub protocols here, essentially number to redux action types

    }
}

fun messageHelper(protocolMessage: ProtocolMessage?) : Type? {
    val subProtocol = protocolMessage?.subProtocol
    val message = protocolMessage?.message
    if(subProtocol == null || message == null) {
        return null
    }

    return when (subProtocol) {
        TestSubType.ITEM_1 -> moshiPack.unpack<SomeType>(message)
        TestSubType.ITEM_2 -> moshiPack.unpack<SomeOtherType>(message)
        else -> null
    }
}

fun testHelper(message: Type?) {
    when (message) {
        is SomeType -> println("Found SomeType: ${message.someString} and ${message.anotherString}")
        is SomeOtherType -> println("Found SomeOtherType: ${message.someInt} and ${message.someString}")
    }
}
