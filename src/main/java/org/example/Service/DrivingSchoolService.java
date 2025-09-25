package org.example.Services;

import org.example.Entities.DrivingSchoolInfo;
import org.example.Rep.DrivingSchoolRepository;

import java.sql.SQLException;

public class DrivingSchoolService {
    private final DrivingSchoolRepository repository;
    private static final long SCHOOL_ID = 1L;

    public DrivingSchoolService() {
        this.repository = new DrivingSchoolRepository();
    }

    public DrivingSchoolInfo getDrivingSchool() throws SQLException {
        DrivingSchoolInfo school = repository.findById(SCHOOL_ID);
        if (school == null) {
            throw new SQLException("Driving school not found");
        }
        return school;
    }

    public void updateDrivingSchool(DrivingSchoolInfo school) throws SQLException {
        school.setId(SCHOOL_ID);
        if (!repository.update(school)) {
            throw new SQLException("Failed to update driving school");
        }
    }

    public void saveLogoPath(String logoPath) throws SQLException {
        DrivingSchoolInfo school = getDrivingSchool();
        school.setLogoPath(logoPath);
        updateDrivingSchool(school);
    }
}