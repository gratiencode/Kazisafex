/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services.dialect;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 *
 * @author endeleya
 */
public class SqliteStatementInspector implements StatementInspector {
    @Override
    public String inspect(String sql) {
        // Hibernate 6 traduit setMaxResults/setFirstResult en syntaxe ANSI
        // "offset ? rows fetch first ? rows only" que SQLite rejette. SQLite
        // accepte "limit <offset>, <count>" (même ordre de binding : offset puis
        // count) et "limit <count>". L'ordre des remplacements est important :
        // la forme combinée d'abord, puis les cas isolés.
        sql = sql.replaceAll(" offset \\? rows fetch first \\? rows only", " limit ?, ?");
        sql = sql.replaceAll(" offset \\? rows", " limit -1 offset ?");
        sql = sql.replaceAll(" fetch first \\? rows only", " limit ?");

        // Remplace "extract(month from ...)" par strftime
        sql = sql.replaceAll("extract\\(month from ([^)]+)\\)", "CAST(strftime('%m',$1/1000,'unixepoch') AS INTEGER)");
        sql = sql.replaceAll("extract\\(year from ([^)]+)\\)", "CAST(strftime('%Y',$1/1000,'unixepoch') AS INTEGER)");
        return sql;
    }
}


