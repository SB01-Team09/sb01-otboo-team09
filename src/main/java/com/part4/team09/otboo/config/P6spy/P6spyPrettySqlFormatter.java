package com.part4.team09.otboo.config.P6spy;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.hibernate.engine.jdbc.internal.FormatStyle;

public class P6spyPrettySqlFormatter implements MessageFormattingStrategy {

  private static MeterRegistry meterRegistry;

  public static void setMeterRegistry(MeterRegistry registry) {
    meterRegistry = registry;
  }

  @Override
  public String formatMessage(int connectionId, String now, long elapsed,
    String category, String prepared, String sql, String url) {

    sql = formatSql(category, sql);
    Date currentDate = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yy.MM.dd HH:mm:ss");

    return format.format(currentDate) + " | OperationTime : " + elapsed + "ms | " + sql;
  }

  private String formatSql(String category, String sql) {
    if (sql == null || sql.trim().isEmpty()) {
      return sql;
    }

    if (category.contains("statement") && sql.trim().toLowerCase(Locale.ROOT)
      .startsWith("create")) {
      return sql;
    }

    if (category.equals("statement")) {
      String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);

      if (meterRegistry != null) {
        String operation = null;
        if (trimmedSQL.startsWith("select")) {
          operation = "SELECT";
        } else if (trimmedSQL.startsWith("insert")) {
          operation = "INSERT";
        } else if (trimmedSQL.startsWith("update")) {
          operation = "UPDATE";
        } else if (trimmedSQL.startsWith("delete")) {
          operation = "DELETE";
        }

        if (operation != null) {
          meterRegistry.counter("batch_sql_count", "type", operation).increment();
        }
      }

      if (trimmedSQL.startsWith("select") ||
        trimmedSQL.startsWith("insert") ||
        trimmedSQL.startsWith("update") ||
        trimmedSQL.startsWith("delete")) {
        sql = FormatStyle.BASIC.getFormatter().format(sql);
        return "\nHeFormatSql(P6Spy sql,Hibernate format):\n" + sql;
      }
    }

    return "\nP6Spy sql:\n" + sql;
  }

}
