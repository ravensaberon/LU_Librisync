package com.lulibrisync.controller;

import com.lulibrisync.service.AcademicProgramService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {

    private final AcademicProgramService academicProgramService;

    public GlobalModelAttributes(AcademicProgramService academicProgramService) {
        this.academicProgramService = academicProgramService;
    }

    @ModelAttribute
    public void populateSharedSelections(Model model) {
        model.addAttribute("programOptionsByCollege", academicProgramService.getProgramsByCollege());
        model.addAttribute("programOptionLookup", academicProgramService.getProgramOptionLookup());
    }
}
