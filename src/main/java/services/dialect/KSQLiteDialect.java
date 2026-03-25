/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services.dialect;

/**
 *
 * @author endeleya
 */

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.hibernate.dialect.Dialect;

import java.sql.Types;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;
import org.hibernate.type.StandardBasicTypes;

public class KSQLiteDialect extends Dialect {
      private final LimitHandler limitHandler = new LimitOffsetLimitHandler();
    public KSQLiteDialect() {
        super();
    }
    
      @Override
    public String columnType(int jdbcTypeCode) {
        return switch (jdbcTypeCode) {
            case Types.INTEGER -> "integer";
            case Types.BIGINT -> "bigint";
            case Types.VARCHAR -> "varchar($l)";
            case Types.CLOB -> "text";
            case Types.BLOB -> "blob";
            case Types.REAL -> "real";
            case Types.FLOAT, Types.DOUBLE -> "double";
            case Types.NUMERIC -> "numeric";
            default -> super.columnType(jdbcTypeCode);
        }; 
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public boolean hasAlterTable() {
        return false;
    }

    @Override
    public boolean qualifyIndexName() {
        return false;
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public String getForUpdateString() {
        return "";
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }
     // Pagination support
    @Override
    public LimitHandler getLimitHandler() {
        return limitHandler;
    }

  
//  
//    @Override
//    public boolean supportsPartitionBy() {
//        return super.supportsPartitionBy();
//    }
//
//    @Override
//    public boolean supportsOrderByInSubquery() {
//        return super.supportsOrderByInSubquery(); 
//    }
//
//   
//
//    @Override
//    public int getParameterCountLimit() {
//        return super.getParameterCountLimit(); 
//    }


  
  
    
}

