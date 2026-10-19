package com.tenco.spring_blog.admin;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // IoC + 싱글톤 패턴으로 관리 됨
public class AdminController {

    // GET http://localhost:8080/admin/dashboard
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (!sessionUserDto.isAdmin()) {
            throw new Exception403("권한이 없습니다");
        }
        model.addAttribute("adminName", sessionUserDto.getUsername());

        return "admin/dashboard";
    }
}
