package com.example.fileupload.controller;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fileupload.FileuploadApplication;
import com.example.fileupload.dto.BoardForm;
import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class BoardController {
	@Autowired BoardRepository boardRepository;
	@Autowired BoardfileRepository boardfileRepository;
	
	@GetMapping("/boardOne")
	public String boardOne(Model model, @RequestParam int bno) {
		BoardMapping boardMapping = boardRepository.findByBno(bno);

		List<Boardfile> fileList = boardfileRepository.findByBno(bno);
		log.debug("size:"+fileList.size());
		
		model.addAttribute("boardMapping", boardMapping);
		model.addAttribute("fileList", fileList);
		return "boardOne";
	}
	
	@GetMapping({"/","/boardList"})
	public String boardList(Model model) {
		// 페이징
		// Sort : bno DESC
		// PageRequest : 0, 10
		// Page<BoardMapping>
		List<BoardMapping> list = boardRepository.findAllBy();
		model.addAttribute("list", list);
		return "boardList";
	}
	
	// 입력폼
	@GetMapping("/addBoard")
	public String addBoard() {
		return "addBoard";
	}	
	// 입력액션
	@PostMapping("/addBoard")
	public String addBaord(BoardForm boardForm) {
		log.debug(boardForm.toString());
		// 이슈: 파일을 첨부하지 않아도 fileSize는 1 이다
		log.debug("MultipartFile Size: " + boardForm.getFileList().size());
		
		Board board = new Board();
		board.setTitle(boardForm.getTitle());
		board.setPw(boardForm.getPw());
		boardRepository.save(board); // board 저장
		int bno = board.getBno(); //board insert 후 bno 변경되었는지
		log.debug("bno: "+bno);
		
		// 파일 분리
		List<MultipartFile> fileList = boardForm.getFileList();
		long firtFileSize = fileList.get(0).getSize();
		log.debug("firtFileSize: " + firtFileSize);
		
		// 이슈: 파일을 첨부하지 않아도 fileSize는 1 이다
		if(firtFileSize > 0) { // 첫번째 파일사이즈가 0이상이다 -> 첨부된 파일이 있다 
			// 업로드 파일 유효성 검사 코드 구현
			for(MultipartFile f : fileList) {
				if(f.getContentType().equals("application/octet-stream") || f.getSize() > 10*1024*1024) { // 1Kbyte = 1024byte, 1Mbyte = 1024Kbyte 
					log.debug("실행파일이나 10M이상파일은 업로드가 안됩니다");
					return "redirect:/addBoard"; // Msg추가
				}
			}
			
			// 파일 업로드 진행 코드
			for(MultipartFile f : fileList) {
				log.debug("파일타입: "+f.getContentType());
				log.debug("원본이름: "+f.getOriginalFilename());
				log.debug("파일용량: "+f.getSize());
				// 확장자 추출
				String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".")+1);
				log.debug("확장자: "+ext);
				String fname = UUID.randomUUID().toString().replace("-", "");
				log.debug("저장파일이름: "+fname);
				
				File emptyFile = new File("c:/project/upload/"+fname+"."+ext);
				// f의 byte -> emptyFile 복사
				try {
					f.transferTo(emptyFile);
				} catch (Exception e) {
					log.error("파일저장실패!");
					e.printStackTrace();
				}
				
				// boardfile테이블에도 파일정보 저장
				Boardfile boardfile = new Boardfile();
				boardfile.setBno(board.getBno());
				boardfile.setFext(ext);
				boardfile.setFname(fname);
				boardfile.setForiginname(f.getOriginalFilename());
				boardfile.setFsize(f.getSize());
				boardfile.setFtype(f.getContentType());
				boardfileRepository.save(boardfile);
			}
		}
		return "redirect:/boardList";
	}
	@GetMapping("/modifyBoard")
	public String modifyBoard(Model model, @RequestParam int bno) {
	    Board board = boardRepository.findById(bno).orElse(null);
	    model.addAttribute("board", board);
	    return "modifyBoard"; // modifyBoard.jsp or .html
	}
	// 게시글 수정 처리 (POST)
		@PostMapping("/modifyBoard")
		public String modifyBoard(@ModelAttribute BoardForm boardForm, RedirectAttributes redirect) {
		    Board board = boardRepository.findById(boardForm.getBno()).orElse(null);
		    if (board == null) {
		        redirect.addFlashAttribute("error", "수정할 게시글이 없습니다.");
		        return "redirect:/";
		    }

		    // 게시글 내용 수정
		    board.setTitle(boardForm.getTitle());
		    board.setPw(boardForm.getPw());
		    boardRepository.save(board); // 수정 반영

		    return "redirect:/boardOne?bno=" + board.getBno();
		}
	@GetMapping("/removeBoard")
	public String removeBoard(@RequestParam int bno) {
	    // 1. 첨부파일 목록 조회
	    List<Boardfile> fileList = boardfileRepository.findByBno(bno);
	    
	    // 2. 서버에서 실제 파일 삭제
	    for (Boardfile f : fileList) {
	        File file = new File("c:/project/upload/" + f.getFname() + "." + f.getFext());
	        if (file.exists()) {
	            file.delete();
	        }
	    }
	    
	    // 3. DB에서 파일 삭제
	    boardfileRepository.deleteByBno(bno);

	    // 4. 게시글 삭제
	    boardRepository.deleteById(bno);

	    return "redirect:/";
	}
	@PostMapping("/removeBoard")
	@Transactional
	public String removeBoardPost(@RequestParam int bno) {
	    // 첨부파일 목록 조회
	    List<Boardfile> fileList = boardfileRepository.findByBno(bno);

	    // 실제 파일 삭제
	    for (Boardfile f : fileList) {
	        File file = new File("c:/project/upload/" + f.getFname() + "." + f.getFext());
	        if (file.exists()) file.delete();
	    }

	    // 파일 DB 삭제
	    boardfileRepository.deleteByBno(bno);

	    // 게시글 삭제
	    boardRepository.deleteById(bno);

	    return "redirect:/";
	}
	
	@GetMapping("/removeBoardFile")
	public String removeBoardFile(@RequestParam int fno, @RequestParam int bno) {
	    Boardfile f = boardfileRepository.findById(fno).orElse(null);
	    if (f != null) {
	        File file = new File("c:/project/upload/" + f.getFname() + "." + f.getFext());
	        if (file.exists()) {
	            file.delete();
	        }
	        boardfileRepository.deleteById(fno);
	    }
	    return "redirect:/boardOne?bno=" + bno;
	}


	

	

	

	

	
	

}
