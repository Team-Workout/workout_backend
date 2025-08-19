package com.workout.utils.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.utils.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/common")
public class FileController {

    @Autowired
    FileService fileService;

    @GetMapping("/files")
    public void getFile(@RequestParam String fileCategory,
                        @AuthenticationPrincipal UserPrincipal userPrincipal){
        Long memberId = userPrincipal.getUserId();


        fileService.findFiles(fileCategory, memberId);
    }

}
