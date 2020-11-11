package com.bestpricemarket.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bestpricemarket.domain.Criteria;
import com.bestpricemarket.domain.GoodsCommentVO;
import com.bestpricemarket.domain.GoodsVO;
import com.bestpricemarket.domain.PageMaker;
import com.bestpricemarket.domain.PricemonitoringVO;
import com.bestpricemarket.domain.ReportVO;
import com.bestpricemarket.service.GoodsCommentService;
import com.bestpricemarket.service.GoodsService;

@Controller
@RequestMapping(value = "/goods/*")
public class GoodsController {

	private static final Logger log = LoggerFactory.getLogger(GoodsController.class);
	
	// 서비스 의존 주입
	@Inject
	private GoodsService service;
	@Inject
	private GoodsCommentService cmtService;
	
	// 재원 신고하기
	@Autowired
    private JavaMailSender  mailSender;

	// 지은 ***************************************************************************************************************************

	// 상품등록
	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String goodsRegisterGET(Model model, HttpSession session,Criteria cri,
			@ModelAttribute("category") String category) throws Exception {

		System.out.println("@@@@@@@ 상품등록 페이지 이동");

		// id 세션값
		model.addAttribute("id", (String) session.getAttribute("id"));
		
		model.addAttribute("category",category);
		model.addAttribute("cri",cri);
		

		return "/goods/goodsRegister";
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String goodsRegisterPOST(GoodsVO vo, MultipartHttpServletRequest mpRequest,Criteria cri, 
				@ModelAttribute("category") String category) throws Exception {

		System.out.println("C : 뷰페이지에서 전달되는 파라미터 -> " + vo);

		// 상품 등록 서비스
		service.goodsRegister(vo, mpRequest);

		System.out.println("C : 상품등록 완료@@@@");

		return "redirect:/goods/list";
	}

	// 상품목록 + 카테고리별 목록 + 페이징 처리
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String goodsListGET(Model model, HttpSession session, @ModelAttribute("cri") Criteria cri ,String category) throws Exception {
		System.out.println("카테고리 : " + category);
		System.out.println("C : goodsList.jsp 이동");
		
		
		// id 세션값
		model.addAttribute("id", (String) session.getAttribute("id"));
		
		// 페이징 처리된 전체목록
		model.addAttribute("goodsList", service.goodsList(cri));
		
		// 페이징 처리된 카테고리별 목록 
		model.addAttribute("categoryList",service.goodsCategoryList(category, cri));
		
		// 카테고리
		model.addAttribute("category", category);
		
		// 하단부 페이징처리
		PageMaker pm = new PageMaker();
		pm.setCri(cri);
		pm.setTotalCount(service.CategoryCount(category));
		model.addAttribute("pm",pm);

		return "/goods/goodsList";
	}
	

	// 상품 상세페이지
	@RequestMapping(value = "/detail", method = {RequestMethod.GET, RequestMethod.POST})
	public String goodsDetailGET(@ModelAttribute("cri") Criteria cri, @RequestParam("gno") int gno, Model model, 
						HttpSession session, @ModelAttribute("category") String category) throws Exception {
		
		String id = (String)session.getAttribute("id");
		System.out.println("C : goodsDetail.jsp 이동");

		model.addAttribute("goods", service.goodsDetail(gno));
		model.addAttribute("id", (String) session.getAttribute("id"));

		// 파일이미지 출력
		List<Map<String, Object>> fileList = service.selectFileList(gno);
		model.addAttribute("file", fileList);

		//댓글 조회 후 출력
		model.addAttribute("cmtList", cmtService.commentList(gno));
		
		// 입찰하기 목록 출력
		model.addAttribute("bidList",service.getBidding(gno));
		
		// 로그인된 회원정보 출력
		model.addAttribute("memberList",service.myInfo(id));
		
		// 카테고리
		model.addAttribute("category",category);
		
		// 현재입찰가
		//model.addAttribute("finalPrice",service.finalPrice(gno));
		
		return "/goods/goodsDetail";
	}

	// 상품수정
	@RequestMapping(value = "/modify", method = RequestMethod.GET)
	public String goodsModifyGET(@RequestParam("gno") int gno, Model model, HttpSession session, Criteria cri, 
			@ModelAttribute("category") String category) throws Exception {

		System.out.println("C : goodsModify.jsp 이동(GET)");

		// 아이디 세션값
		model.addAttribute("id", (String) session.getAttribute("id"));

		GoodsVO goodsVO = service.goodsDetail(gno);
		
		// 상품 수정 페이지 입력된 값 서비스
		model.addAttribute("goodsVO", goodsVO);

		// 이미지 업로드 수정
		List<Map<String, Object>> fileList = service.selectFileList(goodsVO.getGno());
		model.addAttribute("file", fileList);
		
		// 페이징 정보
		model.addAttribute("cri",cri);
		model.addAttribute("category",category);
		
		System.out.println("################ 수정GET에서 cri : "+cri);
		

		return "/goods/goodsModify";
	}

	@RequestMapping(value = "/modify", method = RequestMethod.POST)
	public String goodsModifyPOST(GoodsVO vo, @RequestParam(value = "fileNoDel[]") String[] files,
			@RequestParam(value = "fileNameDel[]") String[] fileNames, MultipartHttpServletRequest mpRequest, 
			RedirectAttributes rttr, @ModelAttribute("category") String category,
			@ModelAttribute("cri") Criteria cri) throws Exception {
		
		System.out.println("C : 상품 수정 POST");
		
		// 상품 수정 서비스 호출
		service.goodsModify(vo, files, fileNames, mpRequest);
		System.out.println("C : 수정된 정보 -> " + vo);
		
		// 수정 완료된 페이징 정보
		rttr.addAttribute("category",category);
		rttr.addAttribute("page", cri.getPage());
		rttr.addAttribute("pageSize", cri.getPageSize());
		
		System.out.println("C@@@@@@@@@@@@@@@@@ page : " + cri.getPage());
		System.out.println("C@@@@@@@@@@@@@@@@@ pageSize : " + cri.getPageSize());

		return "redirect:/goods/list";
	}

	// 상품삭제
	@RequestMapping(value = "/delete", method = {RequestMethod.GET,RequestMethod.POST})
	public String goodsDeletePOST(@RequestParam("gno") int gno, Model model, Criteria cri, RedirectAttributes rttr, 
			@ModelAttribute("category") String category) throws Exception {

		System.out.println("C : 상품 삭제 POST");

		// 상품 삭제 서비스 호출
		service.goodsDelete(gno);
		
		model.addAttribute("category",category);
		
		// 삭제 완료된 페이징 정보
		rttr.addAttribute("category",category);
		rttr.addAttribute("page", cri.getPage());
		rttr.addAttribute("pageSize", cri.getPageSize());
		

		return "redirect:/goods/list";
	}
	

	 // ck에디터 이미지 업로드
	  @RequestMapping(value="/ckUpload", method = RequestMethod.POST) 
	  public void imageUpload(HttpServletRequest request, HttpServletResponse response,
			  		MultipartHttpServletRequest multiFile , @RequestParam MultipartFile upload)  throws Exception{ 
		  
		  // 랜덤 문자 생성 
		  UUID uid = UUID.randomUUID();
	  
		  OutputStream out = null; 
		  PrintWriter printWriter = null;
	  
		  //인코딩 
		  response.setContentType("text/html;charset=utf-8");
	  
		  try{
	  
			  //파일 이름 가져오기 
			  String fileName = upload.getOriginalFilename(); 
			  byte[] bytes = upload.getBytes();
	  
			  //이미지 경로 생성 
			  String path = "C:\\mp\\file\\";// fileDir는 전역 변수라 그냥 이미지 경로 설정해주면 된다. 
			  String ckUploadPath = path + uid + "_" + fileName; 
			  File folder = new File(path);
	  
			  //해당 디렉토리 확인 
			  if(!folder.exists()){ 
				  try{ folder.mkdirs(); // 폴더 생성
				  }catch(Exception e){ 
					  e.getStackTrace(); 
				  } 
			  }
	  
			  out = new FileOutputStream(new File(ckUploadPath)); 
			  out.write(bytes);
			  out.flush(); // outputStram에 저장된 데이터를 전송하고 초기화
	  
			  String callback = request.getParameter("CKEditorFuncNum"); 
			  printWriter = response.getWriter(); 
			  String fileUrl = "ckImgSubmit?uid=" + uid +"&fileName=" + fileName; // 작성화면
	  
			  // 업로드시 메시지 출력 
			  printWriter.println("{\"filename\" : \""+fileName+"\", \"uploaded\" : 1, \"url\":\""+fileUrl+"\"}");
			  printWriter.flush();
	  
		  }catch(IOException e){ 
			  e.printStackTrace(); 
		  } finally { 
			  try { 
				  if(out != null){ 
					  out.close(); 
				  } if(printWriter != null) { 
					  printWriter.close(); 
				  } 
			  }catch(IOException e) { 
				  e.printStackTrace(); 
			  } 
		  }
		  	return; 
	  	}
	  
			  
		@RequestMapping(value="/ckImgSubmit") 
		public void ckSubmit(@RequestParam(value="uid") String uid, @RequestParam(value="fileName") String fileName , 
					HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
			  
			//서버에 저장된 이미지 경로 
			String path = "C:\\mp\\file\\"; 
			  
			String sDirPath = path + uid + "_" + fileName;
			  
			File imgFile = new File(sDirPath);
			  
			//사진 이미지 찾지 못하는 경우 예외처리로 빈 이미지 파일을 설정한다. 
			if(imgFile.isFile()){ 
			   byte[] buf = new byte[1024]; 
			   int readByte = 0; 
			   int length = 0; 
			   byte[] imgBuf = null;
			  
			  FileInputStream fileInputStream = null;
			  ByteArrayOutputStream outputStream = null; 
			  ServletOutputStream out = null;
			  
			  try{ 
				  fileInputStream = new FileInputStream(imgFile); 
				  outputStream = new ByteArrayOutputStream(); out = response.getOutputStream();
			  
			  while((readByte = fileInputStream.read(buf)) != -1){ 
				  outputStream.write(buf,0, readByte); 
				}
			  
			  imgBuf = outputStream.toByteArray(); 
			  length = imgBuf.length;
			  out.write(imgBuf, 0, length); 
			  out.flush();
			  
			  }catch(IOException e){ 
				  System.out.println(e); 
				  } 
			  } 
			} 
	// ck 이미지 업로드
			  
		
	// 첨부파일 다운로드	
	@RequestMapping(value="/fileDown")
	public void fileDown(@RequestParam Map<String, Object> map, HttpServletResponse response) throws Exception{
		Map<String, Object> resultMap = service.selectFileInfo(map);
		String storedFileName = (String) resultMap.get("f_name");
		String originalFileName = (String) resultMap.get("f_oname");
		System.out.println("C : @@@@@@@ 다운로드 실행");
					
		// 파일을 저장했던 위치에서 첨부파일을 읽어 byte[]형식으로 변환
		byte fileByte[] = org.apache.commons.io.FileUtils.readFileToByteArray(new File("C:\\mp\\file\\"+storedFileName));
					
		response.setContentType("application/octet-stream");
		response.setContentLength(fileByte.length);
		response.setHeader("Content-Disposition",  "attachment; fileName=\""+URLEncoder.encode(originalFileName, "UTF-8")+"\";");
		response.getOutputStream().write(fileByte);
		response.getOutputStream().flush();
		response.getOutputStream().close();
					
	}		  
	// 첨부파일 다운로드		 
	
	// 지은 ***************************************************************************************************************************

	// 내경매
	// *******************************************************************************************************************************
	// 내경매
	@RequestMapping(value = "/myauction", method = RequestMethod.GET)
	public String myAuctionGET() throws Exception {

		return "/goods/myAuction";
	}

	// 내경매
	// *******************************************************************************************************************************

	// 상품신고
			// *******************************************************************************************************************************
			/* 재원 */
			// 상품신고
			// http://localhost:8088/goods/report?gno=1
			@RequestMapping(value = "/report", method = RequestMethod.GET)
			public void reportGET(HttpSession session, @RequestParam("gno") int bno, Model model) throws Exception {
				// public void reportGET( @RequestParam("session") String session,
				// @RequestParam("gno") int bno, Model model) throws Exception{
				log.info("C : /report -> report.jsp ");
				log.info("C : reportGET() 호출 ");
				String id = (String)session.getAttribute("id");
				// session.setAttribute("id", "user1");
				model.addAttribute("reportVO", service.showReportDetail(bno));
				
				model.addAttribute("myInfo",service.myInfo(id));
			}

			// 상품신고
			@RequestMapping(value = "/report", method = RequestMethod.POST)
			public String reportPOST(ReportVO rvo,RedirectAttributes rttr,@RequestParam("content") String text) throws Exception {
				// @RequestParam("content") String text -> 추후 수정(강사님)
				System.out.println("레포트 VO : " + rvo);
						
				// 메일 관련 정보(gmail)
				String setfrom = rvo.getReporterEmail();
				String tomail = "bestpricemarketnoreply@gmail.com"; // 받는 사람 이메일
				String title = rvo.getGname() + "(" + rvo.getGno() + ") 게시글 신고"; // 제목
				String content = "신고자 : " + rvo.getReporter() + "\n"; // 내용
				content += "신고 게시글 : http://localhost:8088/goods/detail?gno=" + rvo.getGno() + "\n";
				if(rvo.getRepo() == 1) {
					content += "사유 : 위법성 상품 \n";
				} else if(rvo.getRepo() == 0) {
					content += "사유 : 반복적인 상품게시(도배) \n";
				} else if(rvo.getRepo() == -1) {
					content += "사유 : " + text + "\n";				
				}

				try {
					MimeMessage message = mailSender.createMimeMessage();
					MimeMessageHelper messageHelper = new MimeMessageHelper(message,
							true, "UTF-8");

					messageHelper.setFrom(setfrom); // 보내는사람 생략하면 정상작동을 안함
					messageHelper.setTo(tomail); // 받는사람 이메일
					messageHelper.setSubject(title); // 메일제목은 생략이 가능하다
					messageHelper.setText(content); // 메일 내용

					mailSender.send(message);
			
				} catch (Exception e) {
					System.out.println(e);
				}
				
				return "redirect:/goods/detail?gno="+rvo.getGno();
			}
			/* 재원 끝 */
			// *******************************************************************************************************************************

		// 재원 입찰하기 소스코드
		// *******************************************************************************************************************************
		
		@RequestMapping(value = "/bidding", method = RequestMethod.POST)
		@ResponseBody
		public PricemonitoringVO biddingPOST(PricemonitoringVO prvo,Model model,HttpServletResponse resp) throws Exception {			
			System.out.println("객체 : " + prvo);	
			List<PricemonitoringVO> getDB = service.getBidding(prvo.getPm_g_gno());
			System.out.println("객체리스트 : " + getDB);
			/*DB에서 가져온값이 없을때 db에 저장후 prvo 리턴*/
			if(getDB.size() == 0) {
				service.insertBidding(prvo);
				return prvo;
			} else if(getDB.size() != 0) {
				int maxPrice = service.getMaxPrice(prvo.getPm_g_gno());
				if(prvo.getPm_g_bidprice() > maxPrice) {
					service.insertBidding(prvo);
					return prvo;
				}
			}
			
			return null;
		
		}
		
		// *******************************************************************************************************************************
	
	
	
}