import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import org.junit.Test
import com.badlogic.ashley.core.Family
import com.gofficer.codenames.systems.RemoveSystem
import com.gofficer.codenames.components.RemoveComponent
import junit.framework.TestCase.assertEquals
import org.junit.runner.RunWith


/**
 * Tests the Removeable System.
 */
@RunWith(GdxTestRunner::class)
class RemoveSystemTest {

    private val deltaTime = 0.16f

    @Test
    fun shouldRemoveRemovableComponents() {

        val engine = Engine()

        val system = RemoveSystem()
        val e = Entity()

        engine.addSystem(system)
        engine.addEntity(e)
        engine.update(deltaTime)

        // Ensure entities not removed
        val entities = engine.getEntitiesFor(Family.all().get())
        assertEquals(1, entities.size())

        // Add removable component to all
        entities.forEach {
            it.add(RemoveComponent())
        }

        engine.update(deltaTime)

        // Ensure removed
        val entitiesAfterRemove = engine.getEntitiesFor(Family.all().get())
        assertEquals(0, entitiesAfterRemove.size())
    }


    @Test
    fun shouldRemoveOnlyRemovableComponents() {

        val engine = Engine()

        val system = RemoveSystem()

        engine.addSystem(system)
        engine.addEntity(Entity())
        engine.addEntity(Entity())
        engine.addEntity(Entity())
        engine.addEntity(Entity())
        engine.addEntity(Entity())
        engine.addEntity(Entity())
        engine.update(deltaTime)

        // Ensure entities not removed
        val entities = engine.getEntitiesFor(Family.all().get())
        assertEquals(6, entities.size())


        // Add removable component to half (evens)
        entities.forEachIndexed { index, entity ->
            if (index % 2 == 0) {
                entity.add(RemoveComponent())
            }
        }

        engine.update(deltaTime)

        // Ensure half removed
        val entitiesAfterRemove = engine.getEntitiesFor(Family.all().get())
        assertEquals(3, entitiesAfterRemove.size())
    }
}
