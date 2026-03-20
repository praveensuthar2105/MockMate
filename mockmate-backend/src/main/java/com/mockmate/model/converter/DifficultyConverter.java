package com.mockmate.model.converter;

import com.mockmate.model.Difficulty;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DifficultyConverter implements AttributeConverter<Difficulty, String> {

    @Override
    public String convertToDatabaseColumn(Difficulty attribute) {
        if (attribute == null) {
            return null;
        }
        // Save back as PascalCase to match older rows or just uppercase.
        // Let's store as PascalCase: "Medium", "Easy", "Hard"
        // This ensures the DB values remain consistent.
        String str = attribute.name();
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Override
    public Difficulty convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return Difficulty.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Or throw, but mapping to null prevents total app failure
        }
    }
}
