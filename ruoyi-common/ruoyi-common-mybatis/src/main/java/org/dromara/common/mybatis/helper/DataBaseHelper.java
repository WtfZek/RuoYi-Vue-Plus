package org.dromara.common.mybatis.helper;

import cn.hutool.core.convert.Convert;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.mybatis.enums.DataBaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库助手
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataBaseHelper {

    private static final DynamicRoutingDataSource DS = SpringUtils.getBean(DynamicRoutingDataSource.class);

    /**
     * 获取当前数据库类型
     */
    public static DataBaseType getDataBaseType() {
        DataSource dataSource = DS.determineDataSource();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            return DataBaseType.find(databaseProductName);
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 判断当前数据源是否为 MySQL 数据库。
     *
     */
    public static boolean isMySql() {
        return DataBaseType.MY_SQL == getDataBaseType();
    }

    public static boolean isOracle() {
        return DataBaseType.ORACLE == getDataBaseType();
    }

    public static boolean isPostgerSql() {
        return DataBaseType.POSTGRE_SQL == getDataBaseType();
    }

    public static boolean isSqlServer() {
        return DataBaseType.SQL_SERVER == getDataBaseType();
    }

    /**
     * 根据数据库类型生成适配的 SQL 语句，用于判断指定值是否在逗号分隔的字符串中。
     * <p>
     * 该方法根据当前使用的数据库类型（MySQL、SQL Server、PostgreSQL、Oracle），
     * 动态生成相应的 SQL 语法，用于模拟或直接实现 MySQL 的 {@code FIND_IN_SET} 功能。
     * 主要用于检查某个值是否出现在一个逗号分隔的字符串列表中，例如检查某部门是否属于其祖先部门之一。
     * 在组织架构中，字段 {@code ancestors} 通常用于记录当前部门的所有祖先部门，
     * 格式为一个以逗号分隔的字符串，例如：{@code "1,2,3"} 表示当前部门的祖先包括 ID 为 1、2 和 3 的部门。
     * </p>
     *
     * @param var1 要匹配的值，例如一个部门 ID（如 {@code 100}）。
     * @param var2 逗号分隔的字符串列，或者直接就是 varchar 类型的属性列名，例如数据库中的 {@code "ancestors"} 字段，
     *             用于表示部门的祖先关系（如 {@code "1,2,100"}）。
     * @return 生成的 SQL 条件语句字符串。
     *         <ul>
     *             <li>对于 SQL Server：使用 {@code CHARINDEX} 函数。</li>
     *             <li>对于 PostgreSQL：使用 {@code STRPOS} 函数。</li>
     *             <li>对于 Oracle：使用 {@code INSTR} 函数。</li>
     *             <li>对于 MySQL：直接使用 {@code FIND_IN_SET} 函数。</li>
     *         </ul>
     *
     * <h3>示例</h3>
     * <pre>
     * // 假设 var1 = 100, var2 = "ancestors"
     * // 数据库中的 ancestors 字段值为 "0,100,101"
     *
     * // 对应 MySQL 数据库:
     * find_in_set('100', ancestors) <> 0
     *
     * // 对应 SQL Server 数据库:
     * charindex(',100,' , ',' + ancestors + ',') <> 0
     *
     * // 对应 PostgreSQL 数据库:
     * (select strpos(',' || ancestors || ',' , ',100,')) <> 0
     *
     * // 对应 Oracle 数据库:
     * instr(',' || ancestors || ',' , ',100,') <> 0
     * </pre>
     *
     * <h3>应用场景</h3>
     * <p>
     * 该方法常用于组织架构或多层级分类场景中，例如：
     * <ul>
     *     <li>判断某用户所属的部门是否在某一祖先部门的层级内。</li>
     *     <li>基于祖先关系动态查询某层级下的所有子部门或用户。</li>
     * </ul>
     * </p>
     *
     * @see DataBaseType
     * @see #getDataBaseType()
     */
    public static String findInSet(Object var1, String var2) {
        DataBaseType dataBasyType = getDataBaseType();
        String var = Convert.toStr(var1);
        if (dataBasyType == DataBaseType.SQL_SERVER) {
            // charindex(',100,' , ',0,100,101,') <> 0
            return "charindex(',%s,' , ','+%s+',') <> 0".formatted(var, var2);
        } else if (dataBasyType == DataBaseType.POSTGRE_SQL) {
            // (select strpos(',0,100,101,' , ',100,')) <> 0
            return "(select strpos(','||%s||',' , ',%s,')) <> 0".formatted(var2, var);
        } else if (dataBasyType == DataBaseType.ORACLE) {
            // instr(',0,100,101,' , ',100,') <> 0
            return "instr(','||%s||',' , ',%s,') <> 0".formatted(var2, var);
        }
        // find_in_set(100 , '0,100,101')
        return "find_in_set('%s' , %s) <> 0".formatted(var, var2);
    }

    /**
     * 获取当前加载的数据库名
     */
    public static List<String> getDataSourceNameList() {
        return new ArrayList<>(DS.getDataSources().keySet());
    }
}
