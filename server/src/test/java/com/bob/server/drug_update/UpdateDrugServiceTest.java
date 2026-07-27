package com.bob.server.drug_update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.bob.server.model.Drug;
import com.bob.server.model.Pharmacy;
import com.bob.server.model.Stock;
import com.bob.server.repositories.DrugRepository;
import com.bob.server.repositories.PharmacyRepository;
import com.bob.server.repositories.StockRepository;

@ExtendWith(MockitoExtension.class)
class UpdateDrugServiceTest {

    @Mock
    private DrugRepository drugRepository;

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private UpdateDrugService updateDrugService;

    private final UUID pharmacyId = UUID.randomUUID();
    private Pharmacy pharmacy;
    private Drug existingDrug;

    @BeforeEach
    void setUp() {
        pharmacy = new Pharmacy();
        pharmacy.setID(pharmacyId);
        pharmacy.setName("Test Pharmacy");

        existingDrug = new Drug();
        existingDrug.setID(UUID.randomUUID());
        existingDrug.setName("Paracetamol");
        existingDrug.setAllowed(true);
    }

    @Test
    void updateDrugsFromExcelWithValidFileShouldUpdateStock() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("drugs.xlsx");

        Row headerRow = mock(Row.class);
        Cell nameHeader = mock(Cell.class);
        Cell qtyHeader = mock(Cell.class);
        when(nameHeader.getStringCellValue()).thenReturn("name");
        when(qtyHeader.getStringCellValue()).thenReturn("quantity");
        when(headerRow.getLastCellNum()).thenReturn((short) 2);
        when(headerRow.getCell(0)).thenReturn(nameHeader);
        when(headerRow.getCell(1)).thenReturn(qtyHeader);

        Row dataRow = mock(Row.class);
        Cell nameCell = mock(Cell.class);
        Cell qtyCell = mock(Cell.class);
        when(nameCell.getCellType()).thenReturn(CellType.STRING);
        when(nameCell.getStringCellValue()).thenReturn("Paracetamol");
        when(qtyCell.getCellType()).thenReturn(CellType.NUMERIC);
        when(qtyCell.getNumericCellValue()).thenReturn(100.0);
        when(dataRow.getCell(0)).thenReturn(nameCell);
        when(dataRow.getCell(1)).thenReturn(qtyCell);

        Sheet sheet = mock(Sheet.class);
        when(sheet.iterator()).thenReturn(
                java.util.List.of(headerRow, dataRow).iterator()
        );

        Workbook workbook = mock(Workbook.class);
        when(workbook.getSheetAt(0)).thenReturn(sheet);

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));
        when(drugRepository.findByName("Paracetamol")).thenReturn(Optional.of(existingDrug));

        Stock existingStock = new Stock();
        existingStock.setPharmacyId(pharmacy);
        existingStock.setDrugId(existingDrug);
        existingStock.setQuantity(50);

        when(stockRepository.findByPharmacyIdAndDrugId(pharmacy.getID(), existingDrug.getID()))
                .thenReturn(Optional.of(existingStock));

        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<WorkbookFactory> wf = Mockito.mockStatic(WorkbookFactory.class)) {
            wf.when(() -> WorkbookFactory.create(any(InputStream.class))).thenReturn(workbook);

            int result = updateDrugService.updateDrugsFromExcel(file, pharmacyId);

            assertEquals(1, result);
            assertEquals(100, existingStock.getQuantity());
            verify(stockRepository).save(existingStock);
        }
    }

    @Test
    void updateDrugsFromExcelWithNewDrugShouldAutoCreateDrug() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("drugs.xlsx");

        Row headerRow = mock(Row.class);
        Cell nameHeader = mock(Cell.class);
        Cell qtyHeader = mock(Cell.class);
        when(nameHeader.getStringCellValue()).thenReturn("name");
        when(qtyHeader.getStringCellValue()).thenReturn("quantity");
        when(headerRow.getLastCellNum()).thenReturn((short) 2);
        when(headerRow.getCell(0)).thenReturn(nameHeader);
        when(headerRow.getCell(1)).thenReturn(qtyHeader);

        Row dataRow = mock(Row.class);
        Cell nameCell = mock(Cell.class);
        Cell qtyCell = mock(Cell.class);
        when(nameCell.getCellType()).thenReturn(CellType.STRING);
        when(nameCell.getStringCellValue()).thenReturn("NewDrug");
        when(qtyCell.getCellType()).thenReturn(CellType.NUMERIC);
        when(qtyCell.getNumericCellValue()).thenReturn(200.0);
        when(dataRow.getCell(0)).thenReturn(nameCell);
        when(dataRow.getCell(1)).thenReturn(qtyCell);

        Sheet sheet = mock(Sheet.class);
        when(sheet.iterator()).thenReturn(
                java.util.List.of(headerRow, dataRow).iterator()
        );

        Workbook workbook = mock(Workbook.class);
        when(workbook.getSheetAt(0)).thenReturn(sheet);

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));
        when(drugRepository.findByName("NewDrug")).thenReturn(Optional.empty());

        Drug newDrug = new Drug();
        newDrug.setID(UUID.randomUUID());
        newDrug.setName("NewDrug");
        newDrug.setAllowed(true);
        when(drugRepository.save(any(Drug.class))).thenReturn(newDrug);

        when(stockRepository.findByPharmacyIdAndDrugId(pharmacy.getID(), newDrug.getID()))
                .thenReturn(Optional.empty());

        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<WorkbookFactory> wf = Mockito.mockStatic(WorkbookFactory.class)) {
            wf.when(() -> WorkbookFactory.create(any(InputStream.class))).thenReturn(workbook);

            int result = updateDrugService.updateDrugsFromExcel(file, pharmacyId);

            assertEquals(1, result);
            verify(drugRepository).save(any(Drug.class));
            verify(stockRepository).save(any(Stock.class));
        }
    }

    @Test
    void updateDrugsFromExcelWithEmptyFileShouldThrowException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        UpdateDrugException exception = assertThrows(
                UpdateDrugException.class,
                () -> updateDrugService.updateDrugsFromExcel(file, pharmacyId)
        );

        assertEquals(UpdateDrugValidation.FILE_EMPTY.getMessage(), exception.getMessage());
    }

    @Test
    void updateDrugsFromExcelWithInvalidFormatShouldThrowException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("drugs.csv");

        UpdateDrugException exception = assertThrows(
                UpdateDrugException.class,
                () -> updateDrugService.updateDrugsFromExcel(file, pharmacyId)
        );

        assertEquals(UpdateDrugValidation.INVALID_FORMAT.getMessage(), exception.getMessage());
    }

    @Test
    void updateDrugsFromExcelWithNullFilenameShouldThrowException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        UpdateDrugException exception = assertThrows(
                UpdateDrugException.class,
                () -> updateDrugService.updateDrugsFromExcel(file, pharmacyId)
        );

        assertEquals(UpdateDrugValidation.INVALID_FORMAT.getMessage(), exception.getMessage());
    }

    @Test
    void updateDrugsFromExcelWithMissingColumnsShouldThrowException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("drugs.xlsx");

        Row headerRow = mock(Row.class);
        Cell nameHeader = mock(Cell.class);
        when(nameHeader.getStringCellValue()).thenReturn("wrong_header");
        when(headerRow.getLastCellNum()).thenReturn((short) 1);
        when(headerRow.getCell(0)).thenReturn(nameHeader);

        Sheet sheet = mock(Sheet.class);
        when(sheet.iterator()).thenReturn(
                java.util.List.of(headerRow).iterator()
        );

        Workbook workbook = mock(Workbook.class);
        when(workbook.getSheetAt(0)).thenReturn(sheet);

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));

        try (MockedStatic<WorkbookFactory> wf = Mockito.mockStatic(WorkbookFactory.class)) {
            wf.when(() -> WorkbookFactory.create(any(InputStream.class))).thenReturn(workbook);

            UpdateDrugException exception = assertThrows(
                    UpdateDrugException.class,
                    () -> updateDrugService.updateDrugsFromExcel(file, pharmacyId)
            );

            assertEquals(UpdateDrugValidation.MISSING_COLUMNS.getMessage(), exception.getMessage());
        }
    }

    @Test
    void updateDrugsFromExcelShouldCollapseMultipleSpacesInDrugName() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("drugs.xlsx");

        Row headerRow = mock(Row.class);
        Cell nameHeader = mock(Cell.class);
        Cell qtyHeader = mock(Cell.class);
        when(nameHeader.getStringCellValue()).thenReturn("name");
        when(qtyHeader.getStringCellValue()).thenReturn("quantity");
        when(headerRow.getLastCellNum()).thenReturn((short) 2);
        when(headerRow.getCell(0)).thenReturn(nameHeader);
        when(headerRow.getCell(1)).thenReturn(qtyHeader);

        Row dataRow = mock(Row.class);
        Cell nameCell = mock(Cell.class);
        Cell qtyCell = mock(Cell.class);
        when(nameCell.getCellType()).thenReturn(CellType.STRING);
        when(nameCell.getStringCellValue()).thenReturn("Paracetamol   Extra   Strength");
        when(qtyCell.getCellType()).thenReturn(CellType.NUMERIC);
        when(qtyCell.getNumericCellValue()).thenReturn(50.0);
        when(dataRow.getCell(0)).thenReturn(nameCell);
        when(dataRow.getCell(1)).thenReturn(qtyCell);

        Sheet sheet = mock(Sheet.class);
        when(sheet.iterator()).thenReturn(
                java.util.List.of(headerRow, dataRow).iterator()
        );

        Workbook workbook = mock(Workbook.class);
        when(workbook.getSheetAt(0)).thenReturn(sheet);

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));

        String normalizedName = "Paracetamol Extra Strength";
        Drug collapsedDrug = new Drug();
        collapsedDrug.setID(UUID.randomUUID());
        collapsedDrug.setName(normalizedName);
        collapsedDrug.setAllowed(true);
        when(drugRepository.findByName(normalizedName)).thenReturn(Optional.of(collapsedDrug));

        Stock existingStock = new Stock();
        existingStock.setPharmacyId(pharmacy);
        existingStock.setDrugId(collapsedDrug);
        existingStock.setQuantity(10);
        when(stockRepository.findByPharmacyIdAndDrugId(pharmacy.getID(), collapsedDrug.getID()))
                .thenReturn(Optional.of(existingStock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<WorkbookFactory> wf = Mockito.mockStatic(WorkbookFactory.class)) {
            wf.when(() -> WorkbookFactory.create(any(InputStream.class))).thenReturn(workbook);

            int result = updateDrugService.updateDrugsFromExcel(file, pharmacyId);

            assertEquals(1, result);
            assertEquals(50, existingStock.getQuantity());
            verify(drugRepository).findByName(normalizedName);
        }
    }

    @Test
    void updateDrugsFromExcelWithZeroQuantityShouldThrowException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("drugs.xlsx");

        Row headerRow = mock(Row.class);
        Cell nameHeader = mock(Cell.class);
        Cell qtyHeader = mock(Cell.class);
        when(nameHeader.getStringCellValue()).thenReturn("name");
        when(qtyHeader.getStringCellValue()).thenReturn("quantity");
        when(headerRow.getLastCellNum()).thenReturn((short) 2);
        when(headerRow.getCell(0)).thenReturn(nameHeader);
        when(headerRow.getCell(1)).thenReturn(qtyHeader);

        Row dataRow = mock(Row.class);
        Cell nameCell = mock(Cell.class);
        Cell qtyCell = mock(Cell.class);
        when(nameCell.getCellType()).thenReturn(CellType.STRING);
        when(nameCell.getStringCellValue()).thenReturn("Paracetamol");
        when(qtyCell.getCellType()).thenReturn(CellType.NUMERIC);
        when(qtyCell.getNumericCellValue()).thenReturn(0.0);
        when(dataRow.getCell(0)).thenReturn(nameCell);
        when(dataRow.getCell(1)).thenReturn(qtyCell);

        Sheet sheet = mock(Sheet.class);
        when(sheet.iterator()).thenReturn(
                java.util.List.of(headerRow, dataRow).iterator()
        );

        Workbook workbook = mock(Workbook.class);
        when(workbook.getSheetAt(0)).thenReturn(sheet);

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));

        try (MockedStatic<WorkbookFactory> wf = Mockito.mockStatic(WorkbookFactory.class)) {
            wf.when(() -> WorkbookFactory.create(any(InputStream.class))).thenReturn(workbook);

            UpdateDrugException exception = assertThrows(
                    UpdateDrugException.class,
                    () -> updateDrugService.updateDrugsFromExcel(file, pharmacyId)
            );

            assertEquals(UpdateDrugValidation.INVALID_QUANTITY.getMessage(), exception.getMessage());
        }
    }

    @Test
    void updateDrugsFromExcelWithNegativeQuantityShouldThrowException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("drugs.xlsx");

        Row headerRow = mock(Row.class);
        Cell nameHeader = mock(Cell.class);
        Cell qtyHeader = mock(Cell.class);
        when(nameHeader.getStringCellValue()).thenReturn("name");
        when(qtyHeader.getStringCellValue()).thenReturn("quantity");
        when(headerRow.getLastCellNum()).thenReturn((short) 2);
        when(headerRow.getCell(0)).thenReturn(nameHeader);
        when(headerRow.getCell(1)).thenReturn(qtyHeader);

        Row dataRow = mock(Row.class);
        Cell nameCell = mock(Cell.class);
        Cell qtyCell = mock(Cell.class);
        when(nameCell.getCellType()).thenReturn(CellType.STRING);
        when(nameCell.getStringCellValue()).thenReturn("Paracetamol");
        when(qtyCell.getCellType()).thenReturn(CellType.NUMERIC);
        when(qtyCell.getNumericCellValue()).thenReturn(-5.0);
        when(dataRow.getCell(0)).thenReturn(nameCell);
        when(dataRow.getCell(1)).thenReturn(qtyCell);

        Sheet sheet = mock(Sheet.class);
        when(sheet.iterator()).thenReturn(
                java.util.List.of(headerRow, dataRow).iterator()
        );

        Workbook workbook = mock(Workbook.class);
        when(workbook.getSheetAt(0)).thenReturn(sheet);

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.of(pharmacy));

        try (MockedStatic<WorkbookFactory> wf = Mockito.mockStatic(WorkbookFactory.class)) {
            wf.when(() -> WorkbookFactory.create(any(InputStream.class))).thenReturn(workbook);

            UpdateDrugException exception = assertThrows(
                    UpdateDrugException.class,
                    () -> updateDrugService.updateDrugsFromExcel(file, pharmacyId)
            );

            assertEquals(UpdateDrugValidation.INVALID_QUANTITY.getMessage(), exception.getMessage());
        }
    }

    @Test
    void updateDrugsFromExcelWhenPharmacyNotFoundShouldThrowException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("drugs.xlsx");
        when(pharmacyRepository.findById(pharmacyId)).thenReturn(Optional.empty());

        UpdateDrugException exception = assertThrows(
                UpdateDrugException.class,
                () -> updateDrugService.updateDrugsFromExcel(file, pharmacyId)
        );

        assertEquals(UpdateDrugValidation.PHARMACY_NOT_FOUND.getMessage(), exception.getMessage());
    }
}