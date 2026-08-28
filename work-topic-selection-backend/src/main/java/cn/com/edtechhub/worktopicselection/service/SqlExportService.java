package cn.com.edtechhub.worktopicselection.service;

import java.util.List;
import java.util.Map;

public interface SqlExportService {

    /**
     * 执行原生 SQL 查询, 并生成 List<Map<String,Object>>
     */
    List<Map<String, Object>> executeQuery(String sql);

    /**
     * 执行原生 SQL 查询, 并生成 Excel byte[]
     */
    byte[] exportQueryToExcel(String sql);

}
