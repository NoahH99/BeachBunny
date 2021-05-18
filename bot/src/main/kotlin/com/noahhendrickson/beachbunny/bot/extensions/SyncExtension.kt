package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.noahhendrickson.beachbunny.database.tables.*
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.behavior.requestMembers
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.role.RoleCreateEvent
import dev.kord.core.event.role.RoleDeleteEvent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.collect
import mu.KotlinLogging
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@ExperimentalStdlibApi
@PrivilegedIntent
class SyncExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Database"

    override suspend fun setup() {
        event<Event> {
            action {
                newSuspendedTransaction {
                    when (val event = event) {
                        is ReadyEvent -> event.startRecovery()
                        is RoleCreateEvent -> insertRoleAndGetId(event.role.id.value, event.guildId.value)
                        is RoleDeleteEvent -> deleteRole(event.roleId.value, event.guildId.value)
                        is MemberJoinEvent -> {
                            val member = event.member
                            insertUserAndGetId(member.id.value, bot = member.isBot)
                        }
                    }
                }
            }
        }
    }

    private suspend fun ReadyEvent.startRecovery() {
        logger.info { "Starting recovery..." }

        val time = measureTimeMillis {
            val roleIds: MutableMap<Long, Set<Long>> = mutableMapOf()

            getGuilds().collect { guild ->
                logger.info { "Guild: ${guild.name}" }

                roleIds[guild.id.value] = buildSet {
                    guild.roleIds.forEach { id -> add(id.value) }
                }

                guild.requestMembers().collect { chunk ->
                    chunk.members.forEach { member -> insertUserAndGetId(member.id.value, bot = member.isBot) }
                }
            }

            roleIds.updateRoles()

            kord.editPresence {
                playing("the same song for 6 weeks")
                status = PresenceStatus.Online
            }
        }

        logger.info { "Finished recovery in ${time}ms." }
    }
}
