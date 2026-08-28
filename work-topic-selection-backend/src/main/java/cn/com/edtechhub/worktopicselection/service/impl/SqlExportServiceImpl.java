package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.service.SqlExportService;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.*;

@Service
public class SqlExportServiceImpl implements SqlExportService {

    /**
     * 注入数据源依赖
     */
    @Resource
    private DataSource dataSource;

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            List<Map<String, Object>> result = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }
            return result;

        } catch (SQLException e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "执行查询失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public byte[] exportQueryToExcel(String sql) {
        List<Map<String, Object>> rows = executeQuery(sql);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("result");

            // 定义文本格式
            CellStyle textStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            textStyle.setDataFormat(format.getFormat("@")); // @ 表示文本格式

            if (!rows.isEmpty()) {
                // 写表头
                Row header = sheet.createRow(0);
                int colIdx = 0;
                for (String col : rows.get(0).keySet()) {
                    Cell cell = header.createCell(colIdx++);
                    cell.setCellStyle(textStyle);
                    cell.setCellValue(col);
                }

                // 写数据
                int rowIdx = 1;
                for (Map<String, Object> rowData : rows) {
                    Row row = sheet.createRow(rowIdx++);
                    int c = 0;
                    for (Object value : rowData.values()) {
                        Cell cell = row.createCell(c++);
                        cell.setCellStyle(textStyle); // 全部当文本
                        cell.setCellValue(value == null ? "" : value.toString());
                    }
                }
            } else {
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                cell.setCellStyle(textStyle);
                cell.setCellValue("无数据");
            }

            workbook.write(baos);
            workbook.dispose();
            return baos.toByteArray();
        } catch (Exception e) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "生成 Excel 失败: " + e.getMessage());
            return null;
        }
    }

}
