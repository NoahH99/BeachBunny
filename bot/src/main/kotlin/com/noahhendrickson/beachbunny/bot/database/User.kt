package com.noahhendrickson.beachbunny.bot.database

import com.noahhendrickson.beachbunny.database.tables.UserTable
import dev.kord.core.behavior.UserBehavior
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

val UserBehavior.isAdmin: Boolean
    get() {
        return transaction {
            UserTable
                .slice(UserTable.admin)
                .select { UserTable.userSnowflake eq this@isAdmin.id.value }
                .firstOrNull()?.get(UserTable.admin) ?: false
        }
    }

val UserBehavior.isStaff: Boolean
    get() {
        return transaction {
            if (isAdmin) true
            else UserTable
                .slice(UserTable.staff)
                .select { UserTable.userSnowflake eq this@isStaff.id.value }
                .firstOrNull()?.get(UserTable.staff) ?: false
        }
    }
