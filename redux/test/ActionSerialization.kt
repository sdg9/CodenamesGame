
import com.gofficer.codenames.redux.actions.TouchCard
import com.gofficer.codenames.redux.utils.actionToNetworkBytes
import com.gofficer.codenames.redux.utils.bytesToHexString
import com.gofficer.codenames.redux.utils.networkBytesToAction
import kotlin.test.*



class ActionSerialization {

    @Test
    fun convertTouchCardToMessagePack() {
        val touchCard = TouchCard(1)

        // serialize to message pack

        val byteArray = actionToNetworkBytes(touchCard)
        assertEquals("93 0d 01 82 a2 69 64 01 ac 69 73 46 72 6f 6d 53 65 72 76 65 72 c2 ", bytesToHexString(byteArray))
    }

    @Test
    fun convertTouchCardToMessagePack2() {
        val touchCard = TouchCard(1)
        touchCard.isFromServer = true

        val byteArray = actionToNetworkBytes(touchCard)
        assertEquals("93 0d 01 82 a2 69 64 01 ac 69 73 46 72 6f 6d 53 65 72 76 65 72 c3 ", bytesToHexString(byteArray))
    }


    // TODO show conversion from ProtocolMessage to object

    @Test
    fun convertProtocolMessageToAction() {

        val touchCard = TouchCard(1)

        val networkBytes = actionToNetworkBytes(touchCard)

        val deserializedTouchCard = networkBytesToAction(networkBytes)

        assertEquals(touchCard, deserializedTouchCard)
    }
}