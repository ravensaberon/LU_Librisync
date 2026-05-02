package com.lulibrisync.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class PreparedStudentRegistration implements Serializable {

    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String program;
    private String yearLevel;
    private String email;
    private String contactNumber;
    private LocalDate birthDate;
    private String province;
    private String cityMunicipality;
    private String barangay;
    private String street;
    private String zipcode;
    private String address;
    private String passwordHash;

    public PreparedStudentRegistration() {
    }

    public PreparedStudentRegistration(String firstName,
                                       String middleName,
                                       String lastName,
                                       String fullName,
                                       String program,
                                       String yearLevel,
                                       String email,
                                       String contactNumber,
                                       LocalDate birthDate,
                                       String province,
                                       String cityMunicipality,
                                       String barangay,
                                       String street,
                                       String zipcode,
                                       String address,
                                       String passwordHash) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.program = program;
        this.yearLevel = yearLevel;
        this.email = email;
        this.contactNumber = contactNumber;
        this.birthDate = birthDate;
        this.province = province;
        this.cityMunicipality = cityMunicipality;
        this.barangay = barangay;
        this.street = street;
        this.zipcode = zipcode;
        this.address = address;
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCityMunicipality() {
        return cityMunicipality;
    }

    public void setCityMunicipality(String cityMunicipality) {
        this.cityMunicipality = cityMunicipality;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
