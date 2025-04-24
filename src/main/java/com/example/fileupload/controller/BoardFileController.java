package com.example.fileupload.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardfileRepository;

@Controller
public class BoardFileController {
	@Autowired
	BoardfileRepository boardfileRepository;
	
	@GetMapping("/removeBoardfile")
	public String removeBoardfile(@RequestParam(value = "fno") int fno
									, @RequestParam(value = "bno") int bno) {
		// 파일 삭제 후
		Boardfile boardfile = boardfileRepository.findById(fno).orElse(null);
		File f = new File("c:/project/upload/"+boardfile.getFname()+"."+boardfile.getFext());
		if(f.exists()) {
			f.delete();
		}
		// 테이블 Row 삭제
		boardfileRepository.deleteById(fno);
		return "redirect:/boardOne?bno="+bno;
	}
}
