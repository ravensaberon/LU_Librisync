package com.lulibrisync.dto;

import java.io.Serializable;

public class StudentRegistrationForm implements Serializable {

    private String firstName;
    private String middleName;
    private String lastName;
    private String program;
    private String yearLevel;
    private String email;
    private String contactNumber;
    private String birthDate;
    private String province;
    private String cityMunicipality;
    private String barangay;
    private String street;
    private String zipcode;
    private String password;
    private String confirmPassword;
    private boolean agreeChecked;

    public StudentRegistrationForm() {
    }

    public StudentRegistrationForm(String firstName,
                                   String middleName,
                                   String lastName,
                                   String program,
                                   String yearLevel,
                                   String email,
                                   String contactNumber,
                                   String birthDate,
                                   String province,
                                   String cityMunicipality,
                                   String barangay,
                                   String street,
                                   String zipcode,
                                   String password,
                                   String confirmPassword,
                                   boolean agreeChecked) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
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
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.agreeChecked = agreeChecked;
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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isAgreeChecked() {
        return agreeChecked;
    }

    public void setAgreeChecked(boolean agreeChecked) {
        this.agreeChecked = agreeChecked;
    }
}
