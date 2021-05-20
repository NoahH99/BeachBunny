package com.noahhendrickson.beachbunny.database.util

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import java.sql.ResultSet

fun ResultSet.format(): String {
    val columnCount = metaData.columnCount

    val table = table {
        cellStyle {
            borderRight = true
            borderLeft = true
            alignment = TextAlignment.MiddleLeft
            paddingRight = 1
            paddingLeft = 1
        }

        val columns: Array<String?> = arrayOfNulls(columnCount)

        var rowIndex = 0
        while (next()) {
            val row: Array<String?> = arrayOfNulls(columnCount)

            for (i in 1..columnCount) {
                if (rowIndex == 0) columns[i - 1] = metaData.getColumnName(i)
                row[i - 1] = getString(i) ?: "null"
            }

            row(*row)
            rowIndex++
        }

        header {
            cellStyle { borderBottom = true }
            row(*columns)
        }
    }.toString()

    return if (table.length > 1960) table.substring(0, 1955) + "\n(...)" else table
}
