package cn.com.edtechhub.worktopicselection.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileControllerTest {

    @Test
    void csvCellsThatCouldExecuteFormulasAreEscaped() {
        assertEquals("'=SUM(A1:A2)", FileController.sanitizeCsvCell("=SUM(A1:A2)"));
        assertEquals("'  @command", FileController.sanitizeCsvCell("  @command"));
        assertEquals("普通文本", FileController.sanitizeCsvCell("普通文本"));
        assertEquals(42, FileController.sanitizeCsvCell(42));
    }
}
