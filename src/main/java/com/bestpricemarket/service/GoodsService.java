package com.bestpricemarket.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.bestpricemarket.domain.AnotherGoodsVO;
import com.bestpricemarket.domain.Criteria;
import com.bestpricemarket.domain.GoodsVO;
import com.bestpricemarket.domain.LikesVO;
import com.bestpricemarket.domain.MemberVO;
import com.bestpricemarket.domain.PricemonitoringVO;
import com.bestpricemarket.domain.ReportVO;
import com.bestpricemarket.domain.finalBidVO;

public interface GoodsService {

/* 지은 */
	// 상품등록
	public void goodsRegister(GoodsVO vo, MultipartHttpServletRequest mpRequest) throws Exception;

	// 카테고리별 상품 목록 + 페이징처리
	public List<GoodsVO> goodsCategoryList(String category, Criteria cri) throws Exception;

	// 카테고리별 글 개수 가져오는 처리 
	public int CategoryCount(String category) throws Exception;

	// 상품조회(상품상세페이지)
	public GoodsVO goodsDetail(int gno) throws Exception;

	// 상품수정
	public void goodsModify(GoodsVO vo, String[] files, String[] fileNames, MultipartHttpServletRequest mpRequest) throws Exception;

	// 상품삭제
	public void goodsDelete(int gno) throws Exception;

	// 첨부파일 조회
	public List<Map<String, Object>> selectFileList(int gno) throws Exception;

	// 첨부파일 다운로드
	public Map<String, Object> selectFileInfo(Map<String, Object> map) throws Exception;
	
	// 현재 입찰가
	public int finalPrice(int gno) throws Exception;
	
	// 상품 테이블 현재 입찰가 업데이트
	public void finalpriceupdate(int gno) throws Exception;
	
	// 블락된 회원 가져오기 
	public MemberVO blockMember(String id) throws Exception;
	
/* 지은 끝 */
	
/* 재원 */
	// 상품신고
	public MemberVO myInfo(String id) throws Exception;

	public ReportVO showReportDetail(int gno) throws Exception;
	
	// 입찰정보 가져오기
	public List<PricemonitoringVO> getBidding(int pm_g_gno) throws Exception;

	// 입찰정보(가장큰 입찰가) 가져오기
	public int getMaxPrice(int pm_g_gno) throws Exception;
	
	// 입찰시작 후 그 상품에 대한 상태 업데이트
	public void upStatus(int gno) throws Exception;
		
	// 마감시간 후 actionstatus 0 초기화
	public void endStatus(int gno) throws Exception;

	public void insertBidding(PricemonitoringVO prvo) throws Exception;
	
	public int getTotalCount(Criteria cri) throws Exception;

	// *************** 2020/11/16/월요일 낙찰정보 **************************
		public finalBidVO finalBid(int gno) throws Exception;
		
		// 최종 입찰 성공한 사람의 정보를 myaction테이블에 정보넣기
		public void insertMyAction(finalBidVO fivo) throws Exception;
		// *************** 2020/11/16/월요일 낙찰정보끝 **************************
	/* 재원 끝 */
	
/* 태준 */ 
	// 판매자의 다른상품보기
	public List<AnotherGoodsVO> anothergoods(GoodsVO vo) throws Exception;
/* 태준 끝 */
	
/* 정현 */
	// 좋아요 입력 -> 제품상세페이지(likes 컬럼)
	public int like(LikesVO vo) throws Exception;

	// 좋아요 입력 -> 제품상세페이지(goods테이블 like 컬럼)
	public void goodsLike(int gno) throws Exception;

	// 상품의 좋아요 번호가 있는지 카운트
	public int countbyLike(String l_m_id, int gno);
  
	// 조회
	public LikesVO read(LikesVO vo);
  
	// 좋아요 삭제 (goods테이블 glike컬럼)
	public void deletebyGoods(int gno);
  
	// 좋아요 삭제 (likes테이블)
	public void deletebyLikes(String l_m_id, int l_g_gno);
	
	// 메인페이지 옵션바 (신규등록순)
	public List<GoodsVO> orderbyNew(Criteria cri) throws Exception;
		
	// 메인페이지 옵션바 (마감임박순)
	public List<GoodsVO> orderbyDuedate(Criteria cri) throws Exception;
		
	// 메인페이지 옵션바 (인기경매순)
	public List<GoodsVO> orderbyBest(Criteria cri) throws Exception;
/* 정현 끝 */
	
/* 소원 */
	// 상품목록 + 페이징처리
	public List<GoodsVO> goodsList(Criteria cri) throws Exception;
	
	// 전체 글 개수 가져오는 처리	
	public int listTotalCount() throws Exception;
	
	// 입찰자수 가져오기
	public int gd_bidCount(int gno) throws Exception;
	
	//입찰수 높은 상품 3가지 슬라이드로 출력
	public List<GoodsVO> top3goods(Criteria cri) throws Exception;
	
	//좋아요버튼유지
	public int isClickedLikeBtn(int gno, String id) throws Exception;
	
/* 소원 끝*/
	
}