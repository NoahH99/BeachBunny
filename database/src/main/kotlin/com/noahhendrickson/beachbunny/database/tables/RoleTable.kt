package com.noahhendrickson.beachbunny.database.tables

import com.noahhendrickson.beachbunny.database.insertAndGetId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

object RoleTable : IntIdTable() {

    val roleSnowflake = long("role_snowflake").uniqueIndex()

    val guildId = reference("guild_id", GuildTable.id)

}

fun insertRoleAndGetId(
    roleSnowflake: Long,
    guildSnowflake: Long,
): EntityID<Int> {
    val guildId = insertGuildAndGetId(guildSnowflake)
    return insertRoleAndGetId(roleSnowflake, guildId)
}

fun insertRoleAndGetId(
    roleSnowflake: Long,
    guildId: EntityID<Int>,
): EntityID<Int> {
    val expression: SqlExpressionBuilder.() -> Op<Boolean> =
        { (RoleTable.roleSnowflake eq roleSnowflake) and (RoleTable.guildId eq guildId) }

    return RoleTable.insertAndGetId(expression) {
        it[RoleTable.roleSnowflake] = roleSnowflake
        it[RoleTable.guildId] = guildId
    }
}

fun Map<Long, Set<Long>>.updateRoles() {
    for ((guildSnowflake, roleSnowflakes) in this) {
        val guildId = insertGuildAndGetId(guildSnowflake)

        roleSnowflakes.forEach { roleSnowflake -> insertRoleAndGetId(roleSnowflake, guildId) }

        RoleTable.select { RoleTable.guildId eq guildId }.forEach {
            val roleSnowflake = it[RoleTable.roleSnowflake]

            if (roleSnowflake !in roleSnowflakes)
                RoleTable.deleteWhere {
                    (RoleTable.roleSnowflake eq roleSnowflake) and (RoleTable.guildId eq guildId)
                }
        }
    }
}

fun deleteRole(
    roleSnowflake: Long,
    guildSnowflake: Long,
) {
    val guildId = insertGuildAndGetId(guildSnowflake)
    RoleTable.deleteWhere { (RoleTable.roleSnowflake eq roleSnowflake) and (RoleTable.guildId eq guildId) }
}
