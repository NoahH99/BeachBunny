package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.noahhendrickson.beachbunny.bot.database.insertOrUpdate
import com.noahhendrickson.beachbunny.database.tables.RoleTable
import com.noahhendrickson.beachbunny.database.tables.insertRoleAndGetId
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.requestMembers
import dev.kord.core.entity.Guild
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.collect
import mu.KotlinLogging
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@PrivilegedIntent
class SyncExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Sync"

    override suspend fun setup() {
        event<Event> {
            action {
                when (val event = event) {
                    is ReadyEvent -> event.startSync()
                }
            }
        }
    }

    private suspend fun ReadyEvent.startSync() {
        val time = measureTimeMillis {
            logger.info { "Starting sync..." }

            getGuilds().collect { guild ->
                logger.info { "Syncing guild ${guild.name} (${guild.id.value})" }

                val guildId = guild.insertOrUpdate()
                val roleIds = guild.roleIds

                newSuspendedTransaction {
                    guild.removeDeletedRoles(guildId)

                    roleIds.forEach { insertRoleAndGetId(it.value, guildId) }

                    guild.requestMembers().collect { chunk ->
                        chunk.members.forEach { it.insertOrUpdate() }
                    }
                }
            }

            kord.editPresence {
                playing("the same song for 6 weeks")
                status = PresenceStatus.Online
            }
        }

        logger.info { "Finished sync in ${time}ms." }
    }

    private fun Guild.removeDeletedRoles(guildId: EntityID<Int>) {
        RoleTable.select { RoleTable.guildId eq guildId }.forEach {
            val roleSnowflake = Snowflake(it[RoleTable.roleSnowflake])

            if (roleSnowflake !in roleIds) {
                RoleTable.deleteWhere {
                    (RoleTable.roleSnowflake eq roleSnowflake.value) and (RoleTable.guildId eq guildId)
                }
            }
        }
    }
}
