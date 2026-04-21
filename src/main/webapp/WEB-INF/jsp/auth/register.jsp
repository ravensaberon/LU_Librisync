<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>LU Librisync Registration</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    <style>
        .register-card {
            max-width: 1160px;
        }

        .register-card .auth-card {
            grid-template-columns: 0.92fr 1.08fr;
        }

        .register-story {
            position: relative;
            overflow: hidden;
        }

        .register-story::after {
            content: "";
            position: absolute;
            right: -80px;
            bottom: -80px;
            width: 220px;
            height: 220px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.10);
        }

        .register-note {
            margin-top: 24px;
            padding: 20px;
            border-radius: 22px;
            background: rgba(255, 255, 255, 0.10);
            border: 1px solid rgba(255, 255, 255, 0.12);
        }

        .register-form-row {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 16px;
        }

        .field-hint {
            margin-top: 8px;
            margin-bottom: 0;
            color: var(--muted);
            font-size: 0.86rem;
            line-height: 1.5;
        }

        .field-error {
            min-height: 22px;
            margin-top: 8px;
            margin-bottom: 0;
            color: #9d2f2a;
            font-size: 0.84rem;
            line-height: 1.45;
        }

        .register-form-wrap .form-control.input-invalid,
        .register-form-wrap .form-select.input-invalid {
            border-color: rgba(157, 47, 42, 0.42);
            box-shadow: 0 0 0 0.22rem rgba(157, 47, 42, 0.10);
        }

        .register-form-wrap .form-control.input-valid,
        .register-form-wrap .form-select.input-valid {
            border-color: rgba(15, 127, 52, 0.38);
            box-shadow: 0 0 0 0.22rem rgba(15, 127, 52, 0.08);
        }

        .register-check {
            padding: 16px 18px;
            border-radius: 18px;
            border: 1px solid var(--line);
            background: var(--primary-050);
        }

        .register-check label {
            color: var(--ink);
            font-weight: 600;
        }

        @media (max-width: 900px) {
            .register-card .auth-card,
            .register-form-row {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<div class="auth-shell">
    <div class="register-card w-100">
        <div class="auth-card hero-card">
            <section class="auth-story register-story">
                <span class="tag-chip warn">Student Registration</span>
                <h1 class="mt-3 mb-3 fw-bold">Create your LU Librisync account</h1>
                <p class="fs-5 mb-0">Complete the form below to create your account.</p>
            </section>

            <section class="auth-form-wrap register-form-wrap">
                <h2 class="fw-bold mb-2">Create account</h2>
                <p class="muted-text mb-4">Complete all required fields below. Your student ID will be assigned by the system after registration.</p>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>

                <form id="registerForm" method="post" action="${pageContext.request.contextPath}/register" novalidate>
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                    <div class="register-form-row">
                        <div class="mb-3">
                            <label class="form-label" for="firstName">First name</label>
                            <input class="form-control form-control-lg" id="firstName" name="firstName" type="text" value="${firstNameValue}" maxlength="50" required>
                            <p class="field-error" id="firstNameError"></p>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="middleName">Middle name</label>
                            <input class="form-control form-control-lg" id="middleName" name="middleName" type="text" value="${middleNameValue}" maxlength="50">
                            <p class="field-error" id="middleNameError"></p>
                        </div>
                    </div>

                    <div class="register-form-row">
                        <div class="mb-3">
                            <label class="form-label" for="lastName">Last name</label>
                            <input class="form-control form-control-lg" id="lastName" name="lastName" type="text" value="${lastNameValue}" maxlength="50" required>
                            <p class="field-error" id="lastNameError"></p>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="program">Program</label>
                            <select class="form-select form-select-lg" id="program" name="program" required>
                                <option value="">Select program</option>
                                <c:if test="${not empty programValue and !programOptionLookup[programValue]}">
                                    <option value="${programValue}" selected>${programValue}</option>
                                </c:if>
                                <c:forEach items="${programOptionsByCollege}" var="collegeEntry">
                                    <optgroup label="${collegeEntry.key}">
                                        <c:forEach items="${collegeEntry.value}" var="programOption">
                                            <option value="${programOption}" <c:if test="${programValue == programOption}">selected</c:if>>${programOption}</option>
                                        </c:forEach>
                                    </optgroup>
                                </c:forEach>
                            </select>
                            <p class="field-error" id="programError"></p>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="yearLevel">Year level</label>
                        <select class="form-select form-select-lg" id="yearLevel" name="yearLevel" required>
                            <option value="">Select year level</option>
                            <c:if test="${not empty yearLevelValue and yearLevelValue != 'Not set' and !yearLevelOptionLookup[yearLevelValue]}">
                                <option value="${yearLevelValue}" selected>${yearLevelValue}</option>
                            </c:if>
                            <c:forEach items="${yearLevelOptions}" var="yearLevelOption">
                                <option value="${yearLevelOption}" <c:if test="${yearLevelValue == yearLevelOption}">selected</c:if>>${yearLevelOption}</option>
                            </c:forEach>
                        </select>
                        <p class="field-error" id="yearLevelError"></p>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="email">Email address</label>
                        <input class="form-control form-control-lg" id="email" name="email" type="email" value="${emailValue}" maxlength="120" autocomplete="email" inputmode="email" required>
                        <p class="field-hint">Use a supported provider like `gmail.com`, `outlook.com`, `yahoo.com`, or a school email ending in `.edu` or `.edu.ph`.</p>
                        <p class="field-error" id="emailError"><c:out value="${emailFieldError}"/></p>
                    </div>

                    <div class="register-form-row">
                        <div class="mb-3">
                            <label class="form-label" for="contactNumber">Contact number</label>
                            <input class="form-control form-control-lg" id="contactNumber" name="contactNumber" type="text" value="${contactNumberValue}" maxlength="20" autocomplete="tel" inputmode="tel" required>
                            <p class="field-hint">Use 10 to 15 digits. A leading `+` is allowed.</p>
                            <p class="field-error" id="contactNumberError"><c:out value="${contactNumberFieldError}"/></p>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="birthDate">Birthday</label>
                            <input class="form-control form-control-lg" id="birthDate" name="birthDate" type="date" value="${birthDateValue}" required>
                            <p class="field-error" id="birthDateError"></p>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label" for="age">Age (auto-computed)</label>
                        <input class="form-control form-control-lg" id="age" type="text" placeholder="Auto-computed from birthday" readonly>
                    </div>

                    <div class="register-form-row">
                        <div class="mb-3">
                            <label class="form-label" for="province">Province</label>
                            <select class="form-select form-select-lg" id="province" name="province" required>
                                <option value="Laguna" <c:if test="${empty provinceValue or provinceValue == 'Laguna'}">selected</c:if>>Laguna</option>
                            </select>
                            <p class="field-error" id="provinceError"></p>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="cityMunicipality">City / Municipality</label>
                            <select class="form-select form-select-lg" id="cityMunicipality" name="cityMunicipality" required>
                                <option value="">Select city / municipality</option>
                                <c:forEach items="${registrationCityZipCodes}" var="entry">
                                    <option value="${entry.key}" <c:if test="${cityMunicipalityValue == entry.key}">selected</c:if>>${entry.key}</option>
                                </c:forEach>
                            </select>
                            <p class="field-error" id="cityMunicipalityError"></p>
                        </div>
                    </div>

                    <div class="register-form-row">
                        <div class="mb-3">
                            <label class="form-label" for="barangay">Barangay</label>
                            <select class="form-select form-select-lg" id="barangay" name="barangay" data-selected-barangay="<c:out value='${barangayValue}'/>" <c:if test="${empty cityMunicipalityValue}">disabled</c:if> required>
                                <option value="">
                                    <c:choose>
                                        <c:when test="${not empty cityMunicipalityValue and not empty barangayValue}">${barangayValue}</c:when>
                                        <c:when test="${not empty cityMunicipalityValue}">Loading barangays...</c:when>
                                        <c:otherwise>Select city / municipality first</c:otherwise>
                                    </c:choose>
                                </option>
                                <c:if test="${not empty barangayValue}">
                                    <option value="${barangayValue}" selected>${barangayValue}</option>
                                </c:if>
                            </select>
                            <p class="field-hint">The barangay list updates automatically based on your selected city or municipality.</p>
                            <p class="field-error" id="barangayError"></p>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="street">Street / House No.</label>
                            <input class="form-control form-control-lg" id="street" name="street" type="text" value="${streetValue}" maxlength="180" placeholder="Example: Blk 3 Lot 5, Purok 2" required>
                            <p class="field-error" id="streetError"></p>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label" for="zipcode">Zip Code</label>
                        <input class="form-control form-control-lg" id="zipcode" name="zipcode" type="text" value="${zipcodeValue}" maxlength="4" inputmode="numeric" readonly required>
                        <p class="field-hint">Automatically filled based on the selected city or municipality.</p>
                        <p class="field-error" id="zipcodeError"></p>
                    </div>

                    <div class="register-form-row">
                        <div class="mb-3">
                            <label class="form-label" for="password">Password</label>
                            <input class="form-control form-control-lg" id="password" name="password" type="password" maxlength="100" autocomplete="new-password" required>
                            <p class="field-hint">Minimum 12 characters with uppercase, lowercase, number, and special character.</p>
                            <p class="field-error" id="passwordError"></p>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="confirmPassword">Confirm password</label>
                            <input class="form-control form-control-lg" id="confirmPassword" name="confirmPassword" type="password" maxlength="100" autocomplete="new-password" required>
                            <p class="field-error" id="confirmPasswordError"></p>
                        </div>
                    </div>

                    <div class="register-check mb-4">
                        <div class="form-check">
                            <input class="form-check-input" id="agree" name="agree" type="checkbox" value="yes" <c:if test="${agreeChecked}">checked</c:if>>
                            <label class="form-check-label" for="agree">
                                I confirm that the information above is correct and may be used for my student library account.
                            </label>
                        </div>
                        <p class="field-error mb-0" id="agreeError"></p>
                    </div>

                    <button class="btn btn-brand w-100 mb-3" type="submit">Create student account</button>
                </form>

                <p class="mb-0">Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in here</a>.</p>
            </section>
        </div>
    </div>
</div>

<script>
    (function () {
        var form = document.getElementById("registerForm");
        if (!form) {
            return;
        }

        var firstName = document.getElementById("firstName");
        var middleName = document.getElementById("middleName");
        var lastName = document.getElementById("lastName");
        var program = document.getElementById("program");
        var yearLevel = document.getElementById("yearLevel");
        var email = document.getElementById("email");
        var contactNumber = document.getElementById("contactNumber");
        var birthDate = document.getElementById("birthDate");
        var age = document.getElementById("age");
        var province = document.getElementById("province");
        var cityMunicipality = document.getElementById("cityMunicipality");
        var barangay = document.getElementById("barangay");
        var street = document.getElementById("street");
        var zipcode = document.getElementById("zipcode");
        var password = document.getElementById("password");
        var confirmPassword = document.getElementById("confirmPassword");
        var agree = document.getElementById("agree");
        var submitButton = form.querySelector("button[type='submit']");
        var initialBarangay = barangay.dataset.selectedBarangay || "";
        var barangayRequestToken = 0;
        var registerAvailabilityEndpoint = "${pageContext.request.contextPath}/register/availability";
        var availabilityState = {
            email: {
                requestToken: 0,
                lastCheckedValue: "",
                available: null,
                pending: false
            },
            contactNumber: {
                requestToken: 0,
                lastCheckedValue: "",
                available: null,
                pending: false
            }
        };

        var NAME_ALLOWED_REGEX = /^[A-Za-z](?:[A-Za-z .'-]{0,48}[A-Za-z])?$/;
        var MIN_NAME_LETTERS = 2;
        var MAX_NAME_TOKEN_LENGTH = 12;
        var EMAIL_REGEX = /^[a-z0-9+_.-]+@[a-z0-9.-]+\.[a-z]{2,}$/;
        var CONTACT_REGEX = /^\+?\d{10,15}$/;
        var PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d\s]).{12,100}$/;
        var YEAR_LEVEL_OPTIONS = {
            <c:forEach items="${yearLevelOptions}" var="yearLevelOption" varStatus="status">
            "${yearLevelOption}": true<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };
        var ALLOWED_EMAIL_DOMAINS = {
            "gmail.com": true,
            "yahoo.com": true,
            "yahoo.com.ph": true,
            "outlook.com": true,
            "hotmail.com": true,
            "live.com": true,
            "icloud.com": true,
            "proton.me": true,
            "protonmail.com": true,
            "aol.com": true,
            "gmx.com": true,
            "mail.com": true
        };
        var COMMON_PASSWORDS = {
            "password": true,
            "password123": true,
            "12345678": true,
            "123456789": true,
            "qwerty123": true,
            "admin123": true,
            "welcome123": true,
            "letmein123": true,
            "iloveyou": true,
            "abc12345": true,
            "passw0rd": true,
            "student123": true,
            "adminadmin": true,
            "11111111": true,
            "12341234": true
        };
        var CITY_ZIP_CODES = {
            <c:forEach items="${registrationCityZipCodes}" var="entry" varStatus="status">
            "${entry.key}": "${entry.value}"<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };
        var BARANGAY_ENDPOINT = "${pageContext.request.contextPath}/register/barangays";

        var errors = {
            firstName: document.getElementById("firstNameError"),
            middleName: document.getElementById("middleNameError"),
            lastName: document.getElementById("lastNameError"),
            program: document.getElementById("programError"),
            yearLevel: document.getElementById("yearLevelError"),
            email: document.getElementById("emailError"),
            contactNumber: document.getElementById("contactNumberError"),
            birthDate: document.getElementById("birthDateError"),
            province: document.getElementById("provinceError"),
            cityMunicipality: document.getElementById("cityMunicipalityError"),
            barangay: document.getElementById("barangayError"),
            street: document.getElementById("streetError"),
            zipcode: document.getElementById("zipcodeError"),
            password: document.getElementById("passwordError"),
            confirmPassword: document.getElementById("confirmPasswordError"),
            agree: document.getElementById("agreeError")
        };

        function setFieldState(input, errorElement, message) {
            if (message) {
                input.classList.add("input-invalid");
                input.classList.remove("input-valid");
                input.setCustomValidity(message);
                errorElement.textContent = message;
                return;
            }

            input.setCustomValidity("");
            errorElement.textContent = "";

            if (input.value && !input.readOnly) {
                input.classList.remove("input-invalid");
                input.classList.add("input-valid");
            } else {
                input.classList.remove("input-invalid");
                input.classList.remove("input-valid");
            }
        }

        function applyInitialServerError(input, errorElement) {
            var message = (errorElement.textContent || "").trim();
            if (!message) {
                return;
            }
            input.classList.add("input-invalid");
            input.classList.remove("input-valid");
            input.setCustomValidity(message);
        }

        function clearSelectOptions(selectElement) {
            while (selectElement.options.length > 0) {
                selectElement.remove(0);
            }
        }

        function normalizeLookupValue(value) {
            return (value || "")
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "")
                .toLowerCase()
                .replace(/[^a-z0-9]+/g, " ")
                .trim();
        }

        function normalizeName(value) {
            return (value || "").trim().replace(/\s+/g, " ");
        }

        function countLetters(text) {
            var matches = (text || "").match(/[A-Za-z]/g);
            return matches ? matches.length : 0;
        }

        function hasTooLongNameToken(text) {
            var tokens = (text || "").split(/[ .'-]+/);
            for (var i = 0; i < tokens.length; i++) {
                if (tokens[i] && tokens[i].length > MAX_NAME_TOKEN_LENGTH) {
                    return true;
                }
            }
            return false;
        }

        function hasInvalidEmailDots(value) {
            var at = value.indexOf("@");
            if (at <= 0 || at >= value.length - 1) {
                return true;
            }

            var local = value.substring(0, at);
            var domain = value.substring(at + 1);

            return local.startsWith(".")
                || local.endsWith(".")
                || local.indexOf("..") !== -1
                || domain.startsWith(".")
                || domain.endsWith(".")
                || domain.indexOf("..") !== -1;
        }

        function getEmailDomain(value) {
            var at = value.indexOf("@");
            return at > 0 ? value.substring(at + 1) : "";
        }

        function normalizeContact(value) {
            return (value || "").replace(/[ .\-()]/g, "");
        }

        function normalizeToken(value) {
            return (value || "").toLowerCase().replace(/[^a-z0-9]/g, "");
        }

        function getEmailLocalPart(value) {
            var at = value.indexOf("@");
            return at > 0 ? value.substring(0, at) : value;
        }

        function resetAvailabilityState(fieldKey) {
            availabilityState[fieldKey].lastCheckedValue = "";
            availabilityState[fieldKey].available = null;
            availabilityState[fieldKey].pending = false;
            availabilityState[fieldKey].requestToken++;
        }

        function getAvailabilityConfig(fieldKey) {
            if (fieldKey === "email") {
                return {
                    input: email,
                    errorElement: errors.email,
                    validator: validateEmailField,
                    normalizeValue: function () {
                        return (email.value || "").trim();
                    },
                    fieldName: "email"
                };
            }

            return {
                input: contactNumber,
                errorElement: errors.contactNumber,
                validator: validateContactField,
                normalizeValue: function () {
                    return normalizeContact(contactNumber.value);
                },
                fieldName: "contactNumber"
            };
        }

        function checkFieldAvailability(fieldKey) {
            var config = getAvailabilityConfig(fieldKey);
            var state = availabilityState[fieldKey];

            if (!config.validator()) {
                state.lastCheckedValue = "";
                state.available = null;
                state.pending = false;
                return Promise.resolve(false);
            }

            var normalizedValue = config.normalizeValue();
            if (!normalizedValue) {
                return Promise.resolve(false);
            }

            if (state.lastCheckedValue === normalizedValue && state.available !== null) {
                if (!state.available) {
                    setFieldState(config.input, config.errorElement, config.errorElement.textContent || "This value is already used.");
                }
                return Promise.resolve(state.available);
            }

            var requestToken = ++state.requestToken;
            state.pending = true;
            config.errorElement.textContent = "Checking availability...";
            config.input.classList.remove("input-valid");

            return fetch(registerAvailabilityEndpoint + "?field=" + encodeURIComponent(config.fieldName) + "&value=" + encodeURIComponent(normalizedValue), {
                headers: {
                    "Accept": "application/json"
                }
            })
                .then(function (response) {
                    if (!response.ok) {
                        throw new Error("Unable to check availability.");
                    }
                    return response.json();
                })
                .then(function (result) {
                    if (requestToken !== state.requestToken) {
                        return false;
                    }

                    state.pending = false;
                    state.lastCheckedValue = normalizedValue;
                    state.available = !!result.available;

                    if (!result.valid) {
                        state.available = false;
                        setFieldState(config.input, config.errorElement, result.message || "Please enter a valid value.");
                        return false;
                    }

                    if (!result.available) {
                        setFieldState(config.input, config.errorElement, result.message || "This value is already used.");
                        return false;
                    }

                    setFieldState(config.input, config.errorElement, "");
                    return true;
                })
                .catch(function () {
                    if (requestToken !== state.requestToken) {
                        return false;
                    }

                    state.pending = false;
                    state.available = null;
                    state.lastCheckedValue = "";
                    setFieldState(config.input, config.errorElement, "Unable to check availability right now. Please try again.");
                    return false;
                });
        }

        function hasRepeatedSequence(text, minLen) {
            var repeats = 1;
            for (var i = 1; i < text.length; i++) {
                if (text.charAt(i) === text.charAt(i - 1)) {
                    repeats++;
                    if (repeats >= minLen) {
                        return true;
                    }
                } else {
                    repeats = 1;
                }
            }
            return false;
        }

        function containsPersonalInfo(passwordText) {
            var lowerPassword = (passwordText || "").toLowerCase();
            var tokens = [
                normalizeToken(firstName.value),
                normalizeToken(middleName.value),
                normalizeToken(lastName.value),
                normalizeToken(getEmailLocalPart(email.value)),
                normalizeToken(normalizeContact(contactNumber.value)),
                normalizeToken(birthDate.value)
            ];

            for (var i = 0; i < tokens.length; i++) {
                if (tokens[i].length >= 3 && lowerPassword.indexOf(tokens[i]) !== -1) {
                    return true;
                }
            }
            return false;
        }

        function validateNameValue(value, label, optional) {
            if (!value) {
                return optional ? "" : label + " is required.";
            }
            if (!NAME_ALLOWED_REGEX.test(value)) {
                return "Use letters only. Spaces, apostrophe, hyphen, and period are allowed.";
            }
            if (countLetters(value) < MIN_NAME_LETTERS) {
                return label + " must be at least 2 letters.";
            }
            if (/([A-Za-z])\1{2,}/i.test(value)) {
                return "Avoid triple repeated letters in names.";
            }
            if (hasTooLongNameToken(value)) {
                return "Please avoid random long letter sequences in names.";
            }
            return "";
        }

        function validateNameField(input, errorElement, label, optional) {
            var value = normalizeName(input.value);
            var message = validateNameValue(value, label, optional);
            setFieldState(input, errorElement, message);
            return !message;
        }

        function validateProgramField() {
            var value = (program.value || "").trim().replace(/\s+/g, " ");
            if (!value) {
                setFieldState(program, errors.program, "Program is required.");
                return false;
            }
            if (value.length < 3) {
                setFieldState(program, errors.program, "Program must be at least 3 characters.");
                return false;
            }
            if (value.length > 120) {
                setFieldState(program, errors.program, "Program is too long.");
                return false;
            }

            setFieldState(program, errors.program, "");
            return true;
        }

        function validateYearLevelField() {
            var value = (yearLevel.value || "").trim().replace(/\s+/g, " ");
            if (!value) {
                setFieldState(yearLevel, errors.yearLevel, "Year level is required.");
                return false;
            }
            if (!YEAR_LEVEL_OPTIONS[value]) {
                setFieldState(yearLevel, errors.yearLevel, "Select a valid year level.");
                return false;
            }

            setFieldState(yearLevel, errors.yearLevel, "");
            return true;
        }

        function validateEmailField() {
            var value = (email.value || "").trim();
            if (!value) {
                setFieldState(email, errors.email, "Email address is required.");
                return false;
            }
            if (value !== value.toLowerCase()) {
                setFieldState(email, errors.email, "Use lowercase email only.");
                return false;
            }
            if (!EMAIL_REGEX.test(value) || hasInvalidEmailDots(value)) {
                setFieldState(email, errors.email, "Enter a valid email address.");
                return false;
            }
            var domain = getEmailDomain(value);
            if (!ALLOWED_EMAIL_DOMAINS[domain] && !domain.endsWith(".edu") && !domain.endsWith(".edu.ph")) {
                setFieldState(email, errors.email, "Use a supported provider domain or school email.");
                return false;
            }

            setFieldState(email, errors.email, "");
            return true;
        }

        function validateContactField() {
            var value = normalizeContact(contactNumber.value);
            if (!value) {
                setFieldState(contactNumber, errors.contactNumber, "Contact number is required.");
                return false;
            }
            if (!CONTACT_REGEX.test(value)) {
                setFieldState(contactNumber, errors.contactNumber, "Use 10 to 15 digits for the contact number.");
                return false;
            }

            setFieldState(contactNumber, errors.contactNumber, "");
            return true;
        }

        function syncZipCode() {
            var selectedCity = cityMunicipality.value || "";
            zipcode.value = CITY_ZIP_CODES[selectedCity] || "";
        }

        function setBarangaySelectState(placeholderText, state, disabled) {
            clearSelectOptions(barangay);

            var placeholder = document.createElement("option");
            placeholder.value = "";
            placeholder.textContent = placeholderText;
            placeholder.selected = true;
            barangay.appendChild(placeholder);

            barangay.dataset.state = state;
            barangay.disabled = !!disabled;
            if (!barangay.value) {
                barangay.classList.remove("input-valid");
            }
        }

        function populateBarangayOptions(items, selectedBarangay) {
            clearSelectOptions(barangay);

            var placeholder = document.createElement("option");
            placeholder.value = "";
            placeholder.textContent = "Select barangay";
            barangay.appendChild(placeholder);

            var selectedLookupValue = normalizeLookupValue(selectedBarangay);
            var matched = false;

            (items || []).forEach(function (item) {
                var option = document.createElement("option");
                option.value = item;
                option.textContent = item;
                if (selectedLookupValue && normalizeLookupValue(item) === selectedLookupValue) {
                    option.selected = true;
                    matched = true;
                }
                barangay.appendChild(option);
            });

            if (!matched) {
                placeholder.selected = true;
            }

            barangay.dataset.state = "ready";
            barangay.disabled = false;
        }

        function loadBarangaysForSelectedCity(selectedBarangay) {
            syncZipCode();

            if (!cityMunicipality.value) {
                setBarangaySelectState("Select city / municipality first", "idle", true);
                setFieldState(barangay, errors.barangay, "");
                return Promise.resolve(false);
            }

            var requestToken = ++barangayRequestToken;
            setBarangaySelectState("Loading barangays...", "loading", true);

            return fetch(BARANGAY_ENDPOINT + "?cityMunicipality=" + encodeURIComponent(cityMunicipality.value), {
                headers: {
                    "Accept": "application/json"
                }
            })
                .then(function (response) {
                    if (!response.ok) {
                        throw new Error("Unable to load barangays.");
                    }
                    return response.json();
                })
                .then(function (items) {
                    if (requestToken !== barangayRequestToken) {
                        return false;
                    }
                    populateBarangayOptions(items, selectedBarangay || "");
                    setFieldState(barangay, errors.barangay, "");
                    return true;
                })
                .catch(function () {
                    if (requestToken !== barangayRequestToken) {
                        return false;
                    }
                    setBarangaySelectState("Unable to load barangays. Re-select the city to try again.", "error", true);
                    setFieldState(barangay, errors.barangay, "Unable to load barangays right now. Please re-select the city or try again later.");
                    return false;
                });
        }

        function validateProvinceField() {
            if (!province.value) {
                setFieldState(province, errors.province, "Province is required.");
                return false;
            }

            setFieldState(province, errors.province, "");
            return true;
        }

        function validateCityMunicipalityField() {
            syncZipCode();
            if (!cityMunicipality.value) {
                setFieldState(cityMunicipality, errors.cityMunicipality, "City or municipality is required.");
                setFieldState(zipcode, errors.zipcode, "Select a city or municipality first.");
                return false;
            }

            setFieldState(cityMunicipality, errors.cityMunicipality, "");
            setFieldState(zipcode, errors.zipcode, "");
            return true;
        }

        function validateBarangayField() {
            if (!cityMunicipality.value) {
                setFieldState(barangay, errors.barangay, "Select a city or municipality first.");
                return false;
            }
            if (barangay.dataset.state === "loading") {
                setFieldState(barangay, errors.barangay, "Please wait for the barangay list to finish loading.");
                return false;
            }
            if (barangay.dataset.state === "error") {
                setFieldState(barangay, errors.barangay, "Unable to load barangays right now. Please re-select the city or try again later.");
                return false;
            }
            if (!barangay.value) {
                setFieldState(barangay, errors.barangay, "Barangay is required.");
                return false;
            }

            setFieldState(barangay, errors.barangay, "");
            return true;
        }

        function validateAddressTextField(input, errorElement, label, minimumLength) {
            var value = (input.value || "").trim().replace(/\s+/g, " ");
            if (!value) {
                setFieldState(input, errorElement, label + " is required.");
                return false;
            }
            if (value.length < minimumLength) {
                setFieldState(input, errorElement, label + " must be at least " + minimumLength + " characters.");
                return false;
            }

            setFieldState(input, errorElement, "");
            return true;
        }

        function computeAge(showRequiredError) {
            if (!birthDate.value) {
                age.value = "";
                if (showRequiredError) {
                    setFieldState(birthDate, errors.birthDate, "Birthday is required.");
                    return false;
                }
                setFieldState(birthDate, errors.birthDate, "");
                return true;
            }

            var selectedDate = new Date(birthDate.value + "T00:00:00");
            if (Number.isNaN(selectedDate.getTime())) {
                age.value = "";
                setFieldState(birthDate, errors.birthDate, "Enter a valid birth date.");
                return false;
            }

            var today = new Date();
            var years = today.getFullYear() - selectedDate.getFullYear();
            var monthDiff = today.getMonth() - selectedDate.getMonth();
            var dayDiff = today.getDate() - selectedDate.getDate();

            if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
                years--;
            }

            if (years < 0) {
                age.value = "";
                setFieldState(birthDate, errors.birthDate, "Birth date cannot be in the future.");
                return false;
            }

            if (years < 5 || years > 120) {
                age.value = String(years);
                setFieldState(birthDate, errors.birthDate, "Age must be between 5 and 120.");
                return false;
            }

            age.value = String(years);
            setFieldState(birthDate, errors.birthDate, "");
            return true;
        }

        function validatePasswordField() {
            var value = password.value || "";
            if (!value) {
                setFieldState(password, errors.password, "Password is required.");
                return false;
            }
            if (value.length < 12) {
                setFieldState(password, errors.password, "Password must be at least 12 characters.");
                return false;
            }
            if (!PASSWORD_REGEX.test(value)) {
                setFieldState(password, errors.password, "Include uppercase, lowercase, number, and special character.");
                return false;
            }
            if (COMMON_PASSWORDS[value.toLowerCase()]) {
                setFieldState(password, errors.password, "That password is too common.");
                return false;
            }
            if (hasRepeatedSequence(value, 4)) {
                setFieldState(password, errors.password, "Avoid repeated characters in your password.");
                return false;
            }
            if (containsPersonalInfo(value)) {
                setFieldState(password, errors.password, "Password must not contain your personal details.");
                return false;
            }

            setFieldState(password, errors.password, "");
            return true;
        }

        function validatePasswordMatch() {
            var confirmValue = confirmPassword.value || "";
            if (!confirmValue) {
                setFieldState(confirmPassword, errors.confirmPassword, "Confirm password is required.");
                return false;
            }
            if (password.value !== confirmValue) {
                setFieldState(confirmPassword, errors.confirmPassword, "Passwords do not match.");
                return false;
            }

            setFieldState(confirmPassword, errors.confirmPassword, "");
            return true;
        }

        function validateAgreement() {
            if (!agree.checked) {
                errors.agree.textContent = "Please confirm your registration details before submitting.";
                agree.setCustomValidity("Please confirm your details.");
                return false;
            }

            errors.agree.textContent = "";
            agree.setCustomValidity("");
            return true;
        }

        function normalizeFormValues() {
            firstName.value = normalizeName(firstName.value);
            middleName.value = normalizeName(middleName.value);
            lastName.value = normalizeName(lastName.value);
            program.value = (program.value || "").trim().replace(/\s+/g, " ");
            yearLevel.value = (yearLevel.value || "").trim().replace(/\s+/g, " ");
            email.value = (email.value || "").trim();
            contactNumber.value = normalizeContact(contactNumber.value);
            street.value = (street.value || "").trim().replace(/\s+/g, " ");
            syncZipCode();
        }

        function validateAll() {
            var valid = true;
            valid = validateNameField(firstName, errors.firstName, "First name", false) && valid;
            valid = validateNameField(middleName, errors.middleName, "Middle name", true) && valid;
            valid = validateNameField(lastName, errors.lastName, "Last name", false) && valid;
            valid = validateProgramField() && valid;
            valid = validateYearLevelField() && valid;
            valid = validateEmailField() && valid;
            valid = validateContactField() && valid;
            valid = computeAge(true) && valid;
            valid = validateProvinceField() && valid;
            valid = validateCityMunicipalityField() && valid;
            valid = validateBarangayField() && valid;
            valid = validateAddressTextField(street, errors.street, "Street", 2) && valid;
            valid = validatePasswordField() && valid;
            valid = validatePasswordMatch() && valid;
            valid = validateAgreement() && valid;
            return valid;
        }

        firstName.addEventListener("input", function () {
            validateNameField(firstName, errors.firstName, "First name", false);
            if (password.value) {
                validatePasswordField();
            }
        });

        middleName.addEventListener("input", function () {
            validateNameField(middleName, errors.middleName, "Middle name", true);
            if (password.value) {
                validatePasswordField();
            }
        });

        lastName.addEventListener("input", function () {
            validateNameField(lastName, errors.lastName, "Last name", false);
            if (password.value) {
                validatePasswordField();
            }
        });

        program.addEventListener("change", validateProgramField);
        yearLevel.addEventListener("change", validateYearLevelField);

        email.addEventListener("input", function () {
            validateEmailField();
            resetAvailabilityState("email");
            if (password.value) {
                validatePasswordField();
            }
        });
        email.addEventListener("blur", function () {
            checkFieldAvailability("email");
        });

        contactNumber.addEventListener("input", function () {
            validateContactField();
            resetAvailabilityState("contactNumber");
            if (password.value) {
                validatePasswordField();
            }
        });
        contactNumber.addEventListener("blur", function () {
            checkFieldAvailability("contactNumber");
        });

        birthDate.addEventListener("change", function () {
            computeAge(true);
            if (password.value) {
                validatePasswordField();
            }
        });

        province.addEventListener("change", validateProvinceField);
        cityMunicipality.addEventListener("change", function () {
            validateCityMunicipalityField();
            loadBarangaysForSelectedCity("");
        });
        barangay.addEventListener("change", function () {
            validateBarangayField();
        });
        street.addEventListener("input", function () {
            validateAddressTextField(street, errors.street, "Street", 2);
        });

        password.addEventListener("input", function () {
            validatePasswordField();
            if (confirmPassword.value) {
                validatePasswordMatch();
            }
        });

        confirmPassword.addEventListener("input", validatePasswordMatch);
        agree.addEventListener("change", validateAgreement);

        form.addEventListener("submit", function (event) {
            event.preventDefault();
            normalizeFormValues();
            if (!validateAll()) {
                return;
            }

            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = "Checking details...";
            }

            Promise.all([
                checkFieldAvailability("email"),
                checkFieldAvailability("contactNumber")
            ]).then(function (results) {
                if (results[0] && results[1]) {
                    form.submit();
                }
            }).finally(function () {
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.textContent = "Create student account";
                }
            });
        });

        applyInitialServerError(email, errors.email);
        applyInitialServerError(contactNumber, errors.contactNumber);
        computeAge(false);
        loadBarangaysForSelectedCity(initialBarangay);
    })();
</script>
</body>
</html>
