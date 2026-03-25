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
        // Remplace "extract(month from ...)" par strftime
        sql = sql.replaceAll("extract\\(month from ([^)]+)\\)", "CAST(strftime('%m',$1/1000,'unixepoch') AS INTEGER)");
        sql = sql.replaceAll("extract\\(year from ([^)]+)\\)", "CAST(strftime('%Y',$1/1000,'unixepoch') AS INTEGER)");
        return sql;
    }
}


