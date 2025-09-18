package rabbit.utils.database

import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import rabbit.plugins.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

typealias SerializableAny = @Serializable Any

@Serializable
class EMPTY

abstract class BaseIntEntity<OutputDto : SerializableAny>(id: EntityID<Int>, table: BaseIntIdTable): IntEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt

    abstract fun toOutputDto(): OutputDto
    val json = Json { ignoreUnknownKeys = true }

    var noUpdate: Boolean = true

    companion object {
        inline fun <reified T: Any> createRollbackInstanceTemplate(instanceKlass: KClass<T>): T {
            val args = List(instanceKlass.primaryConstructor?.parameters?.size ?: 0) { null }
            return instanceKlass.primaryConstructor?.call(*args.toTypedArray()) ?: throw Exception("Failed build rollback instance")
        }
    }

//    inline fun <reified Demand : Any, reified Model: BaseIntIdTable> createSlice(demand: Demand, model: Model) {
//        val demandDtoKlass = Demand::class
//        val modelObjectKlass = Model::class
//        val slice = mutableListOf<Any>()
//        demandDtoKlass.primaryConstructor?.parameters?.forEach { arg ->
//            val field = demandDtoKlass.memberProperties.first { it.name == arg.name }
//            val fieldValue = field.call(demand)
//            if (fieldValue == true) {
//                val modelField = (modelObjectKlass.memberProperties.firstOrNull {
//                  it.name == arg.name
//                } ?: return@forEach).call(model) ?: return@forEach
//                slice.add(modelField)
//            }
//        }
//    }

    inline fun <reified Output : SerializableAny, reified Update : SerializableAny> initRollbackInstance(onUpdate: Update, output: SerializableAny = toOutputDto()): Update {
        val updateDtoKlass = Update::class
        val outputDtoKlass = Output::class
        val rollbackInstance = createRollbackInstanceTemplate(updateDtoKlass)
        noUpdate = true
        updateDtoKlass.primaryConstructor?.parameters?.forEach { param ->
            val prop = updateDtoKlass.memberProperties.first { it.name == param.name }
            val currentProp = try { outputDtoKlass.memberProperties.first { it.name == param.name } } catch (_: Exception) { null }
                ?: return@forEach
            val currentValue = currentProp.call(output)
            val onUpdateValue = prop.call(onUpdate)
            val defaultValue = prop.call(rollbackInstance)
            Logger.debug(prop.name, "main")
            Logger.debug(currentValue, "main")
            Logger.debug(onUpdateValue, "main")
            Logger.debug(defaultValue, "main")
            if (onUpdateValue != currentValue &&
                onUpdateValue != defaultValue &&
                prop.name != "id"
            ) {
                if (prop is KMutableProperty<*>) {
                    noUpdate = false
                    prop.setter.call(rollbackInstance, currentValue)
                }
            }
        }

        return rollbackInstance
    }


    @Serializable
    data class EventInstance <T: SerializableAny, R: SerializableAny> (
        val rollbackInstance: T,
        val afterInstance: R
    )

    @OptIn(InternalSerializationApi::class)
    inline fun <reified Output : SerializableAny, reified Update : SerializableAny> createEncodedRollbackUpdateDto(onUpdate: Update, output: SerializableAny = toOutputDto()): String {
        val updateDtoKlass = Update::class
        val rollbackDto = initRollbackInstance<Output, Update>(onUpdate, output)

        if (noUpdate) {
            return json.encodeToString(
                EventInstance.serializer(
                    EMPTY::class.serializer(),
                    EMPTY::class.serializer()
                ),
                EventInstance(EMPTY(), EMPTY())
            )
        }

        val eventInstance = EventInstance(
            rollbackDto, onUpdate
        )

        return json.encodeToString(EventInstance.serializer(updateDtoKlass.serializer(), updateDtoKlass.serializer()), eventInstance)
    }

    inline fun <reified Output : OutputDto, reified Update : SerializableAny> getRollbackInstance(
        onUpdate: Update,
        output: OutputDto = toOutputDto()
    ): Update = initRollbackInstance<Output, Update>(onUpdate, output)

    @OptIn(InternalSerializationApi::class)
    protected inline fun <reified Output : OutputDto> createRollbackRemoveDto(): String {
        val outputDtoKlass = Output::class
        val outputDto = toOutputDto()

        val eventInstance = EventInstance(
            outputDto as Output, EMPTY()
        )

        return json.encodeToString(EventInstance.serializer(outputDtoKlass.serializer(), EMPTY.serializer()), eventInstance)
    }

    @OptIn(InternalSerializationApi::class)
    protected inline fun <reified Output : SerializableAny> createRollbackRemoveDto(dto: Output): String {
        val outputDtoKlass = Output::class

        val eventInstance = EventInstance(
            dto, EMPTY()
        )

        return json.encodeToString(EventInstance.serializer(outputDtoKlass.serializer(), EMPTY.serializer()), eventInstance)
    }

}

val BaseIntEntity<*>.idValue: Int
    get() = this.id.value

suspend fun <T> parallelQueryProcessing(query: Query, batchSize: Int, processor: ResultRow.() -> T): List<T> {
    return withContext(Dispatchers.Default) {
        val queryResult = query.toList()
        val querySize = queryResult.size
        val batches = querySize / batchSize + 1
        val deferred = mutableListOf<Deferred<List<T>>>()
        repeat(batches) {
            deferred.add(async {
                val batchProcessResult = mutableListOf<T>()
                repeat(batchSize) { batchIndex ->
                    if (it * batchSize + batchIndex <= querySize - 1)
                        batchProcessResult.add(processor(queryResult[it * batchSize + batchIndex]))
                    else
                        return@async batchProcessResult
                }
                batchProcessResult
            })
        }
        deferred
    }.awaitAll().flatten()
}
