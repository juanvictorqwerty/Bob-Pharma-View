package com.bob.server.drug_update;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bob.server.model.Drug;
import com.bob.server.model.Pharmacy;
import com.bob.server.model.Stock;
import com.bob.server.repositories.DrugRepository;
import com.bob.server.repositories.PharmacyRepository;
import com.bob.server.repositories.StockRepository;

@Service
public class UpdateDrugService {

    private final DrugRepository drugRepository;
    private final PharmacyRepository pharmacyRepository;
    private final StockRepository stockRepository;

    public UpdateDrugService(DrugRepository drugRepository,
                             PharmacyRepository pharmacyRepository,
                             StockRepository stockRepository) {
        this.drugRepository = drugRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public int updateDrugsFromExcel(MultipartFile file, UUID pharmacyId) {
        if (file == null || file.isEmpty()) {
            throw new UpdateDrugException(UpdateDrugValidation.FILE_EMPTY);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new UpdateDrugException(UpdateDrugValidation.INVALID_FORMAT);
        }

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new UpdateDrugException(UpdateDrugValidation.PHARMACY_NOT_FOUND));

        int rowsProcessed;

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new UpdateDrugException(UpdateDrugValidation.FILE_EMPTY);
            }

            Row headerRow = rowIterator.next();
            int nameColIdx = -1;
            int quantityColIdx = -1;

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell == null) continue;
                String headerValue = cell.getStringCellValue().trim().toLowerCase();
                if ("name".equals(headerValue)) {
                    nameColIdx = i;
                } else if ("quantity".equals(headerValue)) {
                    quantityColIdx = i;
                }
            }

            if (nameColIdx == -1 || quantityColIdx == -1) {
                throw new UpdateDrugException(UpdateDrugValidation.MISSING_COLUMNS);
            }

            rowsProcessed = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String drugName = getStringCellValue(row.getCell(nameColIdx));
                if (drugName == null || drugName.isBlank()) {
                    continue;
                }
                drugName = drugName.trim();

                int quantity = getNumericCellValue(row.getCell(quantityColIdx));

                Drug drug = drugRepository.findByName(drugName)
                        .orElseGet(() -> {
                            Drug newDrug = new Drug();
                            newDrug.setName(drugName);
                            newDrug.setAllowed(true);
                            newDrug.setCreatedAt(Instant.now().toString());
                            newDrug.setUpdatedAt(Instant.now().toString());
                            return drugRepository.save(newDrug);
                        });

                Optional<Stock> existingStock = stockRepository.findByPharmacyIdAndDrugId(pharmacy.getID(), drug.getID());

                Stock stock;
                if (existingStock.isPresent()) {
                    stock = existingStock.get();
                } else {
                    stock = new Stock();
                    stock.setPharmacyId(pharmacy);
                    stock.setDrugId(drug);
                    stock.setCreatedAt(Instant.now());
                }

                stock.setQuantity(quantity);
                stock.setUpdatedAt(Instant.now());
                stockRepository.save(stock);

                rowsProcessed++;
            }

        } catch (UpdateDrugException e) {
            throw e;
        } catch (IOException e) {
            throw new UpdateDrugException(UpdateDrugValidation.PARSE_ERROR);
        } catch (Exception e) {
            throw new UpdateDrugException(UpdateDrugValidation.BATCH_FAILED);
        }

        return rowsProcessed;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return null;
    }

    private int getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}