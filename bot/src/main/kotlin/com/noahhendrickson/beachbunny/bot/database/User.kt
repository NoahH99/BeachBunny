package com.noahhendrickson.beachbunny.bot.database

import com.noahhendrickson.beachbunny.database.tables.UserTable
import com.noahhendrickson.beachbunny.database.tables.insertUserAndGetId
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.User
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun User.insertOrUpdate() = transaction {
    insertUserAndGetId(this@insertOrUpdate.id.value, isBot, admin, staff, ignore)
}

val UserBehavior.admin: Boolean
    get() {
        return if (id.value == 216709975307845633) true
        else transaction {
            UserTable
                .slice(UserTable.admin)
                .select { UserTable.userSnowflake eq this@admin.id.value }
                .firstOrNull()?.get(UserTable.admin) ?: false
        }
    }

val UserBehavior.staff: Boolean
    get() {
        return transaction {
            if (admin) true
            else UserTable
                .slice(UserTable.staff)
                .select { UserTable.userSnowflake eq this@staff.id.value }
                .firstOrNull()?.get(UserTable.staff) ?: false
        }
    }


val UserBehavior.ignore: Boolean
    get() {
        return transaction {
            UserTable
                .slice(UserTable.ignore)
                .select { UserTable.userSnowflake eq this@ignore.id.value }
                .firstOrNull()?.get(UserTable.ignore) ?: false
        }
    }
