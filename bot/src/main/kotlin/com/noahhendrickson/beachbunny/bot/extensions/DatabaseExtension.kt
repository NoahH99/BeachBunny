package com.noahhendrickson.beachbunny.bot.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.noahhendrickson.beachbunny.database.tables.insertGuild
import com.noahhendrickson.beachbunny.database.tables.insertUser
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.behavior.requestMembers
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.collect
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@PrivilegedIntent
class DatabaseExtension(override val bot: ExtensibleBot) : Extension(bot) {

    override val name = "Database"

    override suspend fun setup() {
        event<Event> {
            action {
                newSuspendedTransaction {

                    when (val event = event) {
                        is ReadyEvent -> event.startRecovery()
                    }
                }
            }
        }
    }

    private suspend fun ReadyEvent.startRecovery() {
        logger.info { "Starting recovery..." }

        val time = measureTimeMillis {
            val roleIds: MutableSet<Pair<Long, Long>> = mutableSetOf()

            getGuilds().collect { guild ->
                insertGuild(guild.id.value)

                guild.roleIds.forEach { id -> roleIds.add(id.value to guild.id.value) }
                guild.requestMembers().collect { chunk ->
                    chunk.members.forEach { member -> insertUser(member.id.value) }
                }
            }

            kord.editPresence {
                playing("the same song for 6 weeks")
                status = PresenceStatus.Online
            }
        }

        logger.info { "Finished recovery in ${time}ms." }
    }
}
